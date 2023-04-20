// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page;

import static com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant.LIB_TAG;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.sdk.friend.model.MuteListChangedNotify;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.dialog.ListAlertDialog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.ContentListPopView;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConstant;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationHelper;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationFragmentBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.page.interfaces.IConversationCallback;
import com.netease.yunxin.kit.conversationkit.ui.page.interfaces.ILoadListener;
import com.netease.yunxin.kit.conversationkit.ui.page.viewmodel.ConversationViewModel;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** conversation list fragment show your recent conversation */
public class ConversationFragment extends BaseFragment implements ILoadListener {

  private final String TAG = "ConversationFragment";
  private ConversationFragmentBinding viewBinding;
  private ConversationViewModel viewModel;
  private IConversationCallback conversationCallback;

  private Observer<FetchResult<List<ConversationBean>>> changeObserver;
  private Observer<FetchResult<ConversationBean>> stickObserver;
  private Observer<FetchResult<List<UserInfo>>> userInfoObserver;
  private Observer<FetchResult<List<FriendInfo>>> friendInfoObserver;
  private Observer<FetchResult<List<Team>>> teamInfoObserver;
  private Observer<FetchResult<MuteListChangedNotify>> muteObserver;
  private Observer<FetchResult<String>> addRemoveStickObserver;
  private Observer<FetchResult<List<String>>> aitObserver;
  private Observer<FetchResult<Integer>> unreadCountObserver;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    viewBinding = ConversationFragmentBinding.inflate(inflater, container, false);
    return viewBinding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(this).get(ConversationViewModel.class);
    viewModel.setComparator(conversationComparator);
    viewModel
        .getQueryLiveData()
        .observe(
            this.getViewLifecycleOwner(),
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                viewBinding.conversationViewLayout.getConversationView().setData(result.getData());
              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                viewBinding.conversationViewLayout.getConversationView().addData(result.getData());
              }

              if (viewBinding.conversationViewLayout.getConversationView().getDataSize() > 0) {
                viewBinding.conversationViewLayout.setEmptyViewVisible(View.GONE);
              } else {
                viewBinding.conversationViewLayout.setEmptyViewVisible(View.VISIBLE);
              }
              doCallback();
            });
    initObserver();
    initView();
    loadUIConfig();
    NetworkUtils.registerNetworkStatusChangedListener(networkStateListener);
    registerObserver();
    viewModel.fetchConversation();
  }

  private void initView() {
    //设置会话排序Comparator，默认按照置顶和时间优先级进行排序
    viewBinding.conversationViewLayout.getConversationView().setComparator(conversationComparator);
    viewBinding.conversationViewLayout.getConversationView().setLoadMoreListener(this);
    viewBinding
        .conversationViewLayout
        .getConversationView()
        .setItemClickListener(
            new ViewHolderClickListener() {
              @Override
              public boolean onClick(BaseBean data, int position) {
                boolean result = false;
                if (ConversationKitClient.getConversationUIConfig() != null
                    && ConversationKitClient.getConversationUIConfig().itemClickListener != null
                    && data instanceof ConversationBean) {
                  result =
                      ConversationKitClient.getConversationUIConfig()
                          .itemClickListener
                          .onClick(
                              ConversationFragment.this.getContext(),
                              (ConversationBean) data,
                              position);
                }
                if (!result) {
                  XKitRouter.withKey(data.router)
                      .withParam(data.paramKey, data.param)
                      .withContext(ConversationFragment.this.requireContext())
                      .navigate();
                }
                return true;
              }

              @Override
              public boolean onAvatarClick(BaseBean data, int position) {
                boolean result = false;
                if (ConversationKitClient.getConversationUIConfig() != null
                    && ConversationKitClient.getConversationUIConfig().itemClickListener != null
                    && data instanceof ConversationBean) {
                  result =
                      ConversationKitClient.getConversationUIConfig()
                          .itemClickListener
                          .onAvatarClick(
                              ConversationFragment.this.getContext(),
                              (ConversationBean) data,
                              position);
                }
                if (!result) {
                  XKitRouter.withKey(data.router)
                      .withParam(data.paramKey, data.param)
                      .withContext(ConversationFragment.this.requireContext())
                      .navigate();
                }
                return true;
              }

              @Override
              public boolean onLongClick(BaseBean data, int position) {
                boolean result = false;
                if (ConversationKitClient.getConversationUIConfig() != null
                    && ConversationKitClient.getConversationUIConfig().itemClickListener != null
                    && data instanceof ConversationBean) {
                  result =
                      ConversationKitClient.getConversationUIConfig()
                          .itemClickListener
                          .onLongClick(
                              ConversationFragment.this.getContext(),
                              (ConversationBean) data,
                              position);
                }
                if (!result) {
                  showStickDialog(data);
                }
                return true;
              }

              @Override
              public boolean onAvatarLongClick(BaseBean data, int position) {
                boolean result = false;
                if (ConversationKitClient.getConversationUIConfig() != null
                    && ConversationKitClient.getConversationUIConfig().itemClickListener != null
                    && data instanceof ConversationBean) {
                  result =
                      ConversationKitClient.getConversationUIConfig()
                          .itemClickListener
                          .onAvatarLongClick(
                              ConversationFragment.this.getContext(),
                              (ConversationBean) data,
                              position);
                }
                if (!result) {
                  showStickDialog(data);
                }
                return true;
              }
            });
    viewBinding
        .conversationViewLayout
        .getTitleBar()
        .setRightImageClick(
            v -> {
              if (ConversationKitClient.getConversationUIConfig() != null
                  && ConversationKitClient.getConversationUIConfig().titleBarRightClick != null) {
                ConversationKitClient.getConversationUIConfig().titleBarRightClick.onClick(v);
                return;
              }
              Context context = getContext();
              int memberLimit = ConversationUIConstant.MAX_TEAM_MEMBER;
              if (ConversationKitClient.getConversationUIConfig() != null
                  && ConversationKitClient.getConversationUIConfig().teamMemberLimit
                      != ConversationUIConfig.INT_DEFAULT_NULL) {
                memberLimit = ConversationKitClient.getConversationUIConfig().teamMemberLimit;
              }
              ContentListPopView contentListPopView =
                  new ContentListPopView.Builder(context)
                      .addItem(PopItemFactory.getAddFriendItem(context))
                      .addItem(PopItemFactory.getCreateGroupTeamItem(context, memberLimit))
                      .addItem(PopItemFactory.getCreateAdvancedTeamItem(context, memberLimit))
                      .build();
              contentListPopView.showAsDropDown(
                  v,
                  (int) requireContext().getResources().getDimension(R.dimen.pop_margin_right),
                  0);
            });

    viewBinding
        .conversationViewLayout
        .getTitleBar()
        .setRight2ImageClick(
            v -> {
              if (ConversationKitClient.getConversationUIConfig() != null
                  && ConversationKitClient.getConversationUIConfig().titleBarRight2Click != null) {
                ConversationKitClient.getConversationUIConfig().titleBarRight2Click.onClick(v);
                return;
              }
              XKitRouter.withKey(RouterConstant.PATH_GLOBAL_SEARCH_PAGE)
                  .withContext(requireContext())
                  .navigate();
            });

    viewBinding
        .conversationViewLayout
        .getTitleBar()
        .setLeftImageClick(
            v -> {
              if (ConversationKitClient.getConversationUIConfig() != null
                  && ConversationKitClient.getConversationUIConfig().titleBarLeftClick != null) {
                ConversationKitClient.getConversationUIConfig().titleBarLeftClick.onClick(v);
              }
            });
  }

  private void loadUIConfig() {
    if (ConversationKitClient.getConversationUIConfig() != null
        && ConversationKitClient.getConversationUIConfig().conversationComparator != null) {
      viewModel.setComparator(
          ConversationKitClient.getConversationUIConfig().conversationComparator);
      viewBinding
          .conversationViewLayout
          .getConversationView()
          .setComparator(ConversationKitClient.getConversationUIConfig().conversationComparator);
    }

    if (ConversationKitClient.getConversationUIConfig() != null
        && ConversationKitClient.getConversationUIConfig().conversationFactory != null) {
      viewModel.setConversationFactory(
          ConversationKitClient.getConversationUIConfig().conversationFactory);
      viewBinding
          .conversationViewLayout
          .getConversationView()
          .setViewHolderFactory(
              ConversationKitClient.getConversationUIConfig().conversationFactory);
    }

    if (ConversationKitClient.getConversationUIConfig() != null) {
      ConversationUIConfig config = ConversationKitClient.getConversationUIConfig();
      if (!config.showTitleBar) {
        viewBinding.conversationViewLayout.getTitleBar().setVisibility(View.GONE);
      } else {
        viewBinding.conversationViewLayout.getTitleBar().setVisibility(View.VISIBLE);
        viewBinding
            .conversationViewLayout
            .getTitleBar()
            .setHeadImageVisible(config.showTitleBarLeftIcon ? View.VISIBLE : View.GONE);
        viewBinding
            .conversationViewLayout
            .getTitleBar()
            .showRight2ImageView(config.showTitleBarRight2Icon);
        viewBinding
            .conversationViewLayout
            .getTitleBar()
            .showRightImageView(config.showTitleBarRightIcon);

        if (config.titleBarTitle != null) {
          viewBinding.conversationViewLayout.getTitleBar().setTitle(config.titleBarTitle);
        }

        if (config.titleBarTitleColor != ConversationUIConfig.INT_DEFAULT_NULL) {
          viewBinding.conversationViewLayout.getTitleBar().setTitleColor(config.titleBarTitleColor);
        }

        if (config.titleBarLeftRes != ConversationUIConfig.INT_DEFAULT_NULL) {
          viewBinding.conversationViewLayout.getTitleBar().setLeftImageRes(config.titleBarLeftRes);
        }

        if (config.titleBarLeftRes != ConversationUIConfig.INT_DEFAULT_NULL) {
          viewBinding.conversationViewLayout.getTitleBar().setLeftImageRes(config.titleBarLeftRes);
        }

        if (config.titleBarRightRes != ConversationUIConfig.INT_DEFAULT_NULL) {
          viewBinding
              .conversationViewLayout
              .getTitleBar()
              .setRightImageRes(config.titleBarRightRes);
        }

        if (config.titleBarRight2Res != ConversationUIConfig.INT_DEFAULT_NULL) {
          viewBinding
              .conversationViewLayout
              .getTitleBar()
              .setRight2ImageRes(config.titleBarRight2Res);
        }
      }
      if (config.customLayout != null) {
        config.customLayout.customizeConversationLayout(viewBinding.conversationViewLayout);
      }
    }
  }

  private void initObserver() {
    changeObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success) {
            ALog.d(LIB_TAG, TAG, "ChangeLiveData, Success");
            viewBinding.conversationViewLayout.getConversationView().update(result.getData());
          } else if (result.getLoadStatus() == LoadStatus.Finish
              && result.getType() == FetchResult.FetchType.Remove) {
            ALog.d(LIB_TAG, TAG, "DeleteLiveData, Success");
            if (result.getData() == null || result.getData().size() < 1) {
              viewBinding.conversationViewLayout.getConversationView().removeAll();
            } else {
              viewBinding.conversationViewLayout.getConversationView().remove(result.getData());
            }
          }
          if (viewBinding.conversationViewLayout.getConversationView().getDataSize() > 0) {
            viewBinding.conversationViewLayout.setEmptyViewVisible(View.GONE);
          } else {
            viewBinding.conversationViewLayout.setEmptyViewVisible(View.VISIBLE);
          }
          doCallback();
        };

    stickObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success) {
            ALog.d(LIB_TAG, TAG, "StickLiveData, Success");
            viewBinding.conversationViewLayout.getConversationView().update(result.getData());
          }
          doCallback();
        };

    userInfoObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success) {
            ALog.d(LIB_TAG, TAG, "UserInfoLiveData, Success");
            viewBinding
                .conversationViewLayout
                .getConversationView()
                .updateUserInfo(result.getData());
          }
        };

    friendInfoObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success) {
            ALog.d(LIB_TAG, TAG, "FriendInfoLiveData, Success");
            viewBinding
                .conversationViewLayout
                .getConversationView()
                .updateFriendInfo(result.getData());
          }
        };

    teamInfoObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success) {
            ALog.d(LIB_TAG, TAG, "TeamInfoLiveData, Success");
            viewBinding
                .conversationViewLayout
                .getConversationView()
                .updateTeamInfo(result.getData());
          }
        };

    muteObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success) {
            ALog.d(LIB_TAG, TAG, "MuteInfoLiveData, Success");
            viewBinding
                .conversationViewLayout
                .getConversationView()
                .updateMuteInfo(result.getData());
          }
        };

    aitObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Finish) {
            if (result.getType() == FetchResult.FetchType.Add) {
              ALog.d(LIB_TAG, TAG, "AddStickLiveData, Success");
              ConversationHelper.updateAitInfo(result.getData(), true);
              viewBinding.conversationViewLayout.getConversationView().updateAit(result.getData());
            } else if (result.getType() == FetchResult.FetchType.Remove) {
              ALog.d(LIB_TAG, TAG, "RemoveStickLiveData, Success");
              ConversationHelper.updateAitInfo(result.getData(), false);
              viewBinding.conversationViewLayout.getConversationView().updateAit(result.getData());
            }
          }
        };

    addRemoveStickObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Finish) {
            if (result.getType() == FetchResult.FetchType.Add) {
              ALog.d(LIB_TAG, TAG, "AddStickLiveData, Success");
              viewBinding
                  .conversationViewLayout
                  .getConversationView()
                  .addStickTop(result.getData());
            } else if (result.getType() == FetchResult.FetchType.Remove) {
              ALog.d(LIB_TAG, TAG, "RemoveStickLiveData, Success");
              viewBinding
                  .conversationViewLayout
                  .getConversationView()
                  .removeStickTop(result.getData());
            }
          }
        };

    unreadCountObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success) {
            ALog.d(LIB_TAG, TAG, "unreadCount, Success");
            if (conversationCallback != null) {
              conversationCallback.updateUnreadCount(
                  result.getData() == null ? 0 : result.getData());
            }
          }
        };
  }

  @Override
  public void onStop() {
    super.onStop();
    viewBinding.conversationViewLayout.getConversationView().setShowTag(false);
  }

  @Override
  public void onStart() {
    super.onStart();
    viewBinding.conversationViewLayout.getConversationView().setShowTag(true);
  }

  private void registerObserver() {
    viewModel.getChangeLiveData().observeForever(changeObserver);
    viewModel.getStickLiveData().observeForever(stickObserver);
    viewModel.getUserInfoLiveData().observeForever(userInfoObserver);
    viewModel.getFriendInfoLiveData().observeForever(friendInfoObserver);
    viewModel.getTeamInfoLiveData().observeForever(teamInfoObserver);
    viewModel.getMuteInfoLiveData().observeForever(muteObserver);
    viewModel.getAddRemoveStickLiveData().observeForever(addRemoveStickObserver);
    viewModel.getAitLiveData().observeForever(aitObserver);
    viewModel.getUnreadCountLiveData().observeForever(unreadCountObserver);
  }

  private void unregisterObserver() {
    viewModel.getChangeLiveData().removeObserver(changeObserver);
    viewModel.getStickLiveData().removeObserver(stickObserver);
    viewModel.getUserInfoLiveData().removeObserver(userInfoObserver);
    viewModel.getFriendInfoLiveData().removeObserver(friendInfoObserver);
    viewModel.getTeamInfoLiveData().removeObserver(teamInfoObserver);
    viewModel.getMuteInfoLiveData().removeObserver(muteObserver);
    viewModel.getAddRemoveStickLiveData().removeObserver(addRemoveStickObserver);
    viewModel.getAitLiveData().removeObserver(aitObserver);
    viewModel.getUnreadCountLiveData().removeObserver(unreadCountObserver);
  }

  public void setConversationCallback(IConversationCallback callback) {
    this.conversationCallback = callback;
    doCallback();
  }

  private void showStickDialog(BaseBean data) {
    if (data instanceof ConversationBean) {
      ConversationBean dataBean = (ConversationBean) data;
      ListAlertDialog alertDialog = new ListAlertDialog();
      alertDialog.setContent(generateDialogContent(dataBean.infoData.isStickTop()));
      alertDialog.setTitleVisibility(View.GONE);
      alertDialog.setDialogWidth(getResources().getDimension(R.dimen.alert_dialog_width));
      alertDialog.setItemClickListener(
          action -> {
            if (TextUtils.equals(action, ConversationConstant.Action.ACTION_DELETE)) {
              viewModel.deleteConversation(dataBean);
            } else if (TextUtils.equals(action, ConversationConstant.Action.ACTION_STICK)) {
              if (dataBean.infoData.isStickTop()) {
                viewModel.removeStick((ConversationBean) data);
              } else {
                viewModel.addStickTop((ConversationBean) data);
              }
            }
            alertDialog.dismiss();
          });
      alertDialog.show(getParentFragmentManager());
    }
  }

  private final Comparator<ConversationInfo> conversationComparator =
      (bean1, bean2) -> {
        int result;
        if (bean1 == null) {
          result = 1;
        } else if (bean2 == null) {
          result = -1;
        } else if (bean1.isStickTop() == bean2.isStickTop()) {
          long time = bean1.getTime() - bean2.getTime();
          result = time == 0L ? 0 : (time > 0 ? -1 : 1);

        } else {
          result = bean1.isStickTop() ? -1 : 1;
        }
        ALog.d(LIB_TAG, TAG, "conversationComparator, result:" + result);
        return result;
      };

  private List<ActionItem> generateDialogContent(boolean isStick) {
    List<ActionItem> contentList = new ArrayList<>();
    ActionItem stick =
        new ActionItem(
            ConversationConstant.Action.ACTION_STICK,
            0,
            (isStick ? R.string.cancel_stick_title : R.string.stick_title));
    ActionItem delete =
        new ActionItem(ConversationConstant.Action.ACTION_DELETE, 0, R.string.delete_title);
    contentList.add(stick);
    contentList.add(delete);
    return contentList;
  }

  private void doCallback() {
    if (viewModel != null) {
      viewModel.getUnreadCount();
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    NetworkUtils.unregisterNetworkStatusChangedListener(networkStateListener);
    unregisterObserver();
  }

  private final NetworkUtils.NetworkStateListener networkStateListener =
      new NetworkUtils.NetworkStateListener() {
        @Override
        public void onAvailable(NetworkInfo network) {
          if (viewBinding == null) {
            return;
          }
          viewBinding.conversationViewLayout.getErrorTextView().setVisibility(View.GONE);
        }

        @Override
        public void onLost(NetworkInfo network) {
          if (viewBinding == null) {
            return;
          }
          viewBinding.conversationViewLayout.getErrorTextView().setVisibility(View.VISIBLE);
        }
      };

  @Override
  public boolean hasMore() {
    return viewModel.hasMore();
  }

  @Override
  public void loadMore(Object last) {
    if (last instanceof ConversationBean) {
      viewModel.loadMore((ConversationBean) last);
    }
  }
}
