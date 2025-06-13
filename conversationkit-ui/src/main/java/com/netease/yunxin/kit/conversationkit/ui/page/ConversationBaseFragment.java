// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page;

import static com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant.LIB_TAG;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.dialog.ListAlertDialog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
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
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationUtils;
import com.netease.yunxin.kit.conversationkit.ui.model.AIUserBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.page.interfaces.IConversationCallback;
import com.netease.yunxin.kit.conversationkit.ui.page.interfaces.ILoadListener;
import com.netease.yunxin.kit.conversationkit.ui.page.viewmodel.ConversationViewModel;
import com.netease.yunxin.kit.conversationkit.ui.view.ConversationView;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 会话列表基类,包含会话列表的获取和UI事件，UI层面分为两个子类分别代表不同的UI风格 1. ConversationFragment: 普通版会话列表 2.
 * FunConversationFragment: 娱乐版会话列
 */
public abstract class ConversationBaseFragment extends BaseFragment implements ILoadListener {

  private final String TAG = "ConversationBaseFragment";
  // 会话列表ViewModel，处理业务逻辑
  protected ConversationViewModel viewModel;
  // 会话列表回调，用于更新未读数
  private IConversationCallback conversationCallback;

  // 会话列表数据变化观察者
  private Observer<FetchResult<List<ConversationBean>>> changeObserver;
  // 会话列表@消息变化观察者
  private Observer<FetchResult<List<String>>> aitObserver;
  // 回话更新筛选
  private Observer<FetchResult<List<String>>> updateObserver;

  // 会话列表删除观察者
  private Observer<FetchResult<List<String>>> deleteObserver;
  // 会话列表未读数变化观察者
  private Observer<FetchResult<Integer>> unreadCountObserver;
  // AI数字人员数据变化观察者
  private Observer<FetchResult<List<AIUserBean>>> aiRobotObserver;
  // 会话外部定制接口，用于创建ViewHolder
  protected IConversationFactory conversationFactory;
  // 会话列表封装View
  protected ConversationView conversationView;
  // 会话页面顶部TitleBar
  protected TitleBarView titleBarView;
  // 网络错误View，断网情况下设置显示。子类可个性化定制，父类值根据业务数据控制是否展示
  protected View networkErrorView;
  // 空数据View，当会话列表为空时显示。子类可个性化定制，父类值根据业务数据控制是否展示
  protected View emptyView;
  protected Comparator<ConversationBean> conversationComparator;
  // 初始化View 子类重新去实现
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
    if (networkErrorView != null) {
      NetworkUtils.registerNetworkStatusChangedListener(networkStateListener);
    }
    initData();
    bindView();
    registerObserver();
    // 获取会话数据
    viewModel.getConversationData();
  }

  // 绑定View
  public void bindView() {
    //设置会话排序Comparator，默认按照置顶和时间优先级进行排序
    if (conversationView != null) {
      // 设置会话排序规则
      conversationView.setComparator(conversationComparator);
      conversationView.setLoadMoreListener(this);
      // 设置会话点击事件
      conversationView.setItemClickListener(getViewHolderClickListener());
    }
  }

  protected ViewHolderClickListener getViewHolderClickListener() {
    return new ViewHolderClickListener() {
      @Override
      public boolean onClick(View v, BaseBean data, int position) {
        boolean result = false;
        // 点击事件，如果外部有定制点击事件，则触发外部点击事件，返回true则外部拦截，不需要内部处理
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
        // 内部逻辑，跳转到聊天页面
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
        // 会话头像点击事件，如果外部有定制点击事件，则触发外部点击事件，返回true则外部拦截，不需要内部处理
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
        // 内部逻辑，跳转到聊天页面
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
        // 会话长按事件，如果外部有定制点击事件，则触发外部点击事件，返回true则外部拦截，不需要内部处理
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
        // 内部逻辑，显示置顶和删除对话框
        if (!result) {
          showStickDialog(data);
        }
        return true;
      }

      @Override
      public boolean onAvatarLongClick(View v, BaseBean data, int position) {
        // 会话头像长按事件，如果外部有定制点击事件，则触发外部点击事件，返回true则外部拦截，不需要内部处理
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
        // 内部逻辑，显示置顶和删除对话框
        if (!result) {
          showStickDialog(data);
        }
        return true;
      }
    };
  };

  // 设置外部定制的ViewHolderFactory
  public void setViewHolderFactory(IConversationFactory factory) {
    conversationFactory = factory;
    if (viewModel != null) {
      viewModel.setConversationFactory(factory);
    }
    if (conversationView != null) {
      conversationView.setViewHolderFactory(factory);
    }
  }

  // 初始化观察者
  protected void initData() {
    viewModel = new ViewModelProvider(this).get(ConversationViewModel.class);
    conversationComparator = ConversationUtils.getConversationComparator();
    viewModel.setComparator(conversationComparator);
    if (conversationFactory != null) {
      viewModel.setConversationFactory(conversationFactory);
    }
    // 会话列表查询数据变化观察者
    viewModel
        .getQueryLiveData()
        .observe(
            this.getViewLifecycleOwner(),
            result -> {
              if (conversationView != null) {
                if (result.getLoadStatus() == LoadStatus.Success) {
                  if (result.getType() == FetchResult.FetchType.Init) {
                    conversationView.setData(result.getData());
                  } else if (result.getType() == FetchResult.FetchType.Add) {
                    conversationView.addData(result.getData());
                  }
                  loadData(result.getType(), result.getData());
                  updateEmptyView();
                }
              }
            });
    // 会话列表数据变化观察者
    changeObserver =
        result -> {
          if (conversationView != null) {
            if (result.getLoadStatus() == LoadStatus.Success) {
              ALog.d(LIB_TAG, TAG, "ChangeLiveData");
              conversationView.update(result.getData());
            }
            updateEmptyView();
          }
        };

    // 会话列表@消息变化观察者
    aitObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Finish) {
            if (result.getType() == FetchResult.FetchType.Add && conversationView != null) {
              ALog.d(LIB_TAG, TAG, "aitObserver add, Success");
              ConversationHelper.updateAitInfo(result.getData(), true);
              conversationView.updateAit(result.getData());
            } else if (result.getType() == FetchResult.FetchType.Remove
                && conversationView != null) {
              ALog.d(LIB_TAG, TAG, "aitObserver remove, Success");
              ConversationHelper.updateAitInfo(result.getData(), false);
              conversationView.updateAit(result.getData());
            }
          }
        };

    // 会话列表未读数变化观察者
    unreadCountObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success) {
            ALog.d(
                LIB_TAG,
                TAG,
                "unreadCount, Success:"
                    + result.getData()
                    + "  conversationCallback"
                    + ":"
                    + (conversationCallback == null));
            if (conversationCallback != null) {
              conversationCallback.updateUnreadCount(
                  result.getData() == null ? 0 : result.getData());
            }
          }
        };

    // 会话列表删除观察者
    deleteObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Success) {
            ALog.d(LIB_TAG, TAG, "deleteLiveData, Success");
            if (conversationView != null) {
              conversationView.remove(result.getData());
            }
            updateEmptyView();
          }
        };
    // AI数字人员数据变化观察者
    aiRobotObserver = result -> loadAIUserData(result);

    updateObserver =
        result -> {
          if (result.getLoadStatus() == LoadStatus.Finish) {
            if (result.getType() == FetchResult.FetchType.Update && conversationView != null) {
              ALog.d(LIB_TAG, TAG, "updateObserver add, Success");
              conversationView.updateConversation(result.getData());
            }
          }
        };
  }
  /**
   * 加载数据, 用于加载会话列表数据
   *
   * @param type 加载类型 {@link FetchResult.FetchType} init 初始加载，add 加载更多,update 更新
   * @param data 会话列表数据
   */
  public void loadData(FetchResult.FetchType type, List<ConversationBean> data) {}

  /**
   * 加载AI数字人员数据
   *
   * @param result
   */
  public void loadAIUserData(FetchResult<List<AIUserBean>> result) {}

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

  @Override
  public void onResume() {
    super.onResume();
    checkNetwork();
  }

  private void registerObserver() {
    viewModel.getChangeLiveData().observeForever(changeObserver);
    viewModel.getAitLiveData().observeForever(aitObserver);
    viewModel.getUpdateLiveData().observeForever(updateObserver);
    viewModel.getUnreadCountLiveData().observeForever(unreadCountObserver);
    viewModel.getDeleteLiveData().observeForever(deleteObserver);
    viewModel.getAiRobotLiveData().observeForever(aiRobotObserver);
  }

  private void unregisterObserver() {
    viewModel.getChangeLiveData().removeObserver(changeObserver);
    viewModel.getAitLiveData().removeObserver(aitObserver);
    viewModel.getUpdateLiveData().removeObserver(updateObserver);
    viewModel.getUnreadCountLiveData().removeObserver(unreadCountObserver);
    viewModel.getDeleteLiveData().removeObserver(deleteObserver);
    viewModel.getAiRobotLiveData().removeObserver(aiRobotObserver);
  }

  public void setConversationCallback(IConversationCallback callback) {
    this.conversationCallback = callback;
    if (viewModel != null) {
      viewModel.getUnreadCount();
    }
  }

  public void updateEmptyView() {
    if (emptyView != null) {
      if (conversationView.getContentDataSize() > 0) {
        emptyView.setVisibility(View.GONE);
      } else {
        emptyView.setVisibility(View.VISIBLE);
      }
    }
  }

  public ConversationViewModel getViewModel() {
    return viewModel;
  }

  // 显示置顶和删除对话框
  private void showStickDialog(BaseBean data) {
    if (data instanceof ConversationBean) {
      ConversationBean dataBean = (ConversationBean) data;
      ListAlertDialog alertDialog = new ListAlertDialog();
      alertDialog.setContent(generateDialogContent(dataBean.infoData.isStickTop()));
      alertDialog.setTitleVisibility(View.GONE);
      alertDialog.setDialogWidth(getResources().getDimension(R.dimen.alert_dialog_width));
      alertDialog.setItemClickListener(
          action -> {
            if (!NetworkUtils.isConnected()) {
              ToastX.showShortToast(R.string.conversation_network_error_tip);
            } else {
              if (TextUtils.equals(action, ConversationConstant.Action.ACTION_DELETE)) {
                viewModel.deleteConversation(dataBean.getConversationId(), true);
              } else if (TextUtils.equals(action, ConversationConstant.Action.ACTION_STICK)) {
                if (dataBean.infoData.isStickTop()) {
                  viewModel.removeStick((ConversationBean) data);
                } else {
                  viewModel.addStickTop((ConversationBean) data);
                }
              }
            }
            alertDialog.dismiss();
          });
      alertDialog.show(getParentFragmentManager());
    }
  }

  // 生成置顶和删除对话框内容
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
        public void onConnected(NetworkUtils.NetworkType networkType) {
          if (networkErrorView == null) {
            return;
          }
          networkErrorView.setVisibility(View.GONE);
        }

        @Override
        public void onDisconnected() {
          if (networkErrorView == null) {
            return;
          }
          networkErrorView.setVisibility(View.VISIBLE);
        }
      };

  private void checkNetwork() {
    if (networkErrorView == null) {
      return;
    }
    if (NetworkUtils.isConnected()) {
      networkErrorView.setVisibility(View.GONE);
    } else {
      networkErrorView.setVisibility(View.VISIBLE);
    }
  }

  // 是否有更多数据
  @Override
  public boolean hasMore() {
    return viewModel.hasMore();
  }

  // 加载下一页数据
  @Override
  public void loadMore(Object last) {
    viewModel.loadMore();
  }

  @Override
  public void onScrollStateIdle(int first, int end) {
    subscribeConversation(first, end);
  }

  private void subscribeConversation(int first, int end) {
    if (conversationView != null) {
      viewModel.dynamicSubscribeConversation(first, end, conversationView.getDataList());
    }
  }
  // 获取会话View
  public ConversationView getConversationView() {
    return conversationView;
  }
}
