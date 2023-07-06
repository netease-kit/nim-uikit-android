// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page;

import static com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant.LIB_TAG;

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
import com.netease.yunxin.kit.chatkit.model.ConversationInfo;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.dialog.ListAlertDialog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.TitleBarView;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;
import com.netease.yunxin.kit.conversationkit.ui.IConversationFactory;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationHelper;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.page.interfaces.IConversationCallback;
import com.netease.yunxin.kit.conversationkit.ui.page.interfaces.ILoadListener;
import com.netease.yunxin.kit.conversationkit.ui.page.viewmodel.ConversationViewModel;
import com.netease.yunxin.kit.conversationkit.ui.view.ConversationView;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** conversation list fragment show your recent conversation */
public abstract class ConversationBaseFragment extends BaseFragment implements ILoadListener {

  private final String TAG = "ConversationFragment";
  protected ConversationViewModel viewModel;
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

  protected IConversationFactory conversationFactory;

  protected ConversationView conversationView;

  protected TitleBarView titleBarView;

  protected View networkErrorView;

  protected View emptyView;

  public abstract View initViewAndGetRootView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState);

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return initViewAndGetRootView(inflater, container, savedInstanceState);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(this).get(ConversationViewModel.class);
    viewModel.setComparator(conversationComparator);
    if (conversationFactory != null) {
      viewModel.setConversationFactory(conversationFactory);
    }
    viewModel
        .getQueryLiveData()
        .observe(
            this.getViewLifecycleOwner(),
            result -> {
              if (conversationView != null) {
                if (result.getLoadStatus() == LoadStatus.Success) {
                  conversationView.setData(result.getData());
                } else if (result.getLoadStatus() == LoadStatus.Finish) {
                  conversationView.addData(result.getData());
                }

                if (emptyView != null) {
                  if (conversationView.getDataSize() > 0) {
                    emptyView.setVisibility(View.GONE);
                  } else {
                    emptyView.setVisibility(View.VISIBLE);
                  }
                }
              }
              doCallback();
            });
    initObserver();
    bindView();
    if (networkErrorView != null) {
      NetworkUtils.registerNetworkStatusChangedListener(networkStateListener);
    }
    registerObserver();
    viewModel.fetchConversation();
  }

  public void bindView() {
    //设置会话排序Comparator，默认按照置顶和时间优先级进行排序
    if (conversationView != null) {
      conversationView.setComparator(conversationComparator);
      conversationView.setLoadMoreListener(this);
      conversationView.setItemClickListener(
          new ViewHolderClickListener() {
            @Override
            public boolean onClick(View v, BaseBean data, int position) {
              boolean result = false;
              if (ConversationKitClient.getConversationUIConfig() != null
                  && ConversationKitClient.getConversationUIConfig().itemClickListener != null
                  && data instanceof ConversationBean) {
                result =
                    ConversationKitClient.getConversationUIConfig()
                        .itemClickListener
                        .onClick(
                            ConversationBaseFragment.this.getContext(),
                            (ConversationBean) data,
                            position);
              }
              if (!result) {
                XKitRouter.withKey(data.router)
                    .withParam(data.paramKey, data.param)
                    .withContext(ConversationBaseFragment.this.requireContext())
                    .navigate();
              }
              return true;
            }

            @Override
            public boolean onAvatarClick(View v, BaseBean data, int position) {
              boolean result = false;
              if (ConversationKitClient.getConversationUIConfig() != null
                  && ConversationKitClient.getConversationUIConfig().itemClickListener != null
                  && data instanceof ConversationBean) {
                result =
                    ConversationKitClient.getConversationUIConfig()
                        .itemClickListener
                        .onAvatarClick(
                            ConversationBaseFragment.this.getContext(),
                            (ConversationBean) data,
                            position);
              }
              if (!result) {
                XKitRouter.withKey(data.router)
                    .withParam(data.paramKey, data.param)
                    .withContext(ConversationBaseFragment.this.requireContext())
                    .navigate();
              }
              return true;
            }

            @Override
            public boolean onLongClick(View v, BaseBean data, int position) {
              boolean result = false;
              if (ConversationKitClient.getConversationUIConfig() != null
                  && ConversationKitClient.getConversationUIConfig().itemClickListener != null
                  && data instanceof ConversationBean) {
                result =
                    ConversationKitClient.getConversationUIConfig()
                        .itemClickListener
                        .onLongClick(
                            ConversationBaseFragment.this.getContext(),
                            (ConversationBean) data,
                            position);
              }
              if (!result) {
                showStickDialog(data);
              }
              return true;
            }

            @Override
            public boolean onAvatarLongClick(View v, BaseBean data, int position) {
              boolean result = false;
              if (ConversationKitClient.getConversationUIConfig() != null
                  && ConversationKitClient.getConversationUIConfig().itemClickListener != null
                  && data instanceof ConversationBean) {
                result =
                    ConversationKitClient.getConversationUIConfig()
                        .itemClickListener
                        .onAvatarLongClick(
                            ConversationBaseFragment.this.getContext(),
                            (ConversationBean) data,
                            position);
              }
              if (!result) {
                showStickDialog(data);
              }
              return true;
            }
          });
    }
  }

  public void setViewHolderFactory(IConversationFactory factory) {
    conversationFactory = factory;
    if (viewModel != null) {
      viewModel.setConversationFactory(factory);
    }
    if (conversationView != null) {
      conversationView.setViewHolderFactory(factory);
    }
  }

  public void setComparator(Comparator<ConversationInfo> comparator) {
    if (comparator != null) {
      conversationComparator = comparator;
      if (viewModel != null) {
        viewModel.setComparator(
            ConversationKitClient.getConversationUIConfig().conversationComparator);
      }

      if (conversationView != null) {
        conversationView.setComparator(
            ConversationKitClient.getConversationUIConfig().conversationComparator);
      }
    }
  }

  private void initObserver() {
    changeObserver =
        result -> {
          if (conversationView != null) {
            if (result.getLoadStatus() == LoadStatus.Success) {
              ALog.d(LIB_TAG, TAG, "ChangeLiveData, Success");
              conversationView.update(result.getData());
            } else if (result.getLoadStatus() == LoadStatus.Finish
                && result.getType() == FetchResult.FetchType.Remove) {
              ALog.d(LIB_TAG, TAG, "DeleteLiveData, Success");
              if (result.getData() == null || result.getData().size() < 1) {
                conversationView.removeAll();
              } else {
                conversationView.remove(result.getData());
              }
            }
            if (emptyView != null) {
              if (conversationView.getDataSize() > 0) {
                emptyView.setVisibility(View.GONE);
              } else {
                emptyView.setVisibility(View.VISIBLE);
              }
            }
          }
          doCallback();
        };

    stickObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success && conversationView != null) {
            ALog.d(LIB_TAG, TAG, "StickLiveData, Success");
            conversationView.update(result.getData());
          }
          doCallback();
        };

    userInfoObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success && conversationView != null) {
            ALog.d(LIB_TAG, TAG, "UserInfoLiveData, Success");
            conversationView.updateUserInfo(result.getData());
          }
        };

    friendInfoObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success && conversationView != null) {
            ALog.d(LIB_TAG, TAG, "FriendInfoLiveData, Success");
            conversationView.updateFriendInfo(result.getData());
          }
        };

    teamInfoObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success && conversationView != null) {
            ALog.d(LIB_TAG, TAG, "TeamInfoLiveData, Success");
            conversationView.updateTeamInfo(result.getData());
          }
        };

    muteObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success && conversationView != null) {
            ALog.d(LIB_TAG, TAG, "MuteInfoLiveData, Success");
            conversationView.updateMuteInfo(result.getData());
          }
        };

    aitObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Finish) {
            if (result.getType() == FetchResult.FetchType.Add && conversationView != null) {
              ALog.d(LIB_TAG, TAG, "AddStickLiveData, Success");
              ConversationHelper.updateAitInfo(result.getData(), true);
              conversationView.updateAit(result.getData());
            } else if (result.getType() == FetchResult.FetchType.Remove
                && conversationView != null) {
              ALog.d(LIB_TAG, TAG, "RemoveStickLiveData, Success");
              ConversationHelper.updateAitInfo(result.getData(), false);
              conversationView.updateAit(result.getData());
            }
          }
        };

    addRemoveStickObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Finish) {
            if (result.getType() == FetchResult.FetchType.Add && conversationView != null) {
              ALog.d(LIB_TAG, TAG, "AddStickLiveData, Success");
              conversationView.addStickTop(result.getData());
            } else if (result.getType() == FetchResult.FetchType.Remove
                && conversationView != null) {
              ALog.d(LIB_TAG, TAG, "RemoveStickLiveData, Success");
              conversationView.removeStickTop(result.getData());
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
    if (conversationView != null) {
      conversationView.setShowTag(false);
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    if (conversationView != null) {
      conversationView.setShowTag(true);
    }
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

  public ConversationViewModel getViewModel() {
    return viewModel;
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

  private Comparator<ConversationInfo> conversationComparator =
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

  protected List<ActionItem> generateDialogContent(boolean isStick) {
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
    if (networkErrorView != null) {
      NetworkUtils.unregisterNetworkStatusChangedListener(networkStateListener);
    }
    unregisterObserver();
  }

  private final NetworkUtils.NetworkStateListener networkStateListener =
      new NetworkUtils.NetworkStateListener() {
        @Override
        public void onAvailable(NetworkInfo network) {
          if (networkErrorView == null) {
            return;
          }
          networkErrorView.setVisibility(View.GONE);
        }

        @Override
        public void onLost(NetworkInfo network) {
          if (networkErrorView == null) {
            return;
          }
          networkErrorView.setVisibility(View.VISIBLE);
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

  public ConversationView getConversationView() {
    return conversationView;
  }
}
