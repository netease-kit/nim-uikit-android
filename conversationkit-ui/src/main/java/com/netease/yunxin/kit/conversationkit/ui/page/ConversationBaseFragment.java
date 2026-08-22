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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMUserAIBot;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.manager.UserAIBotEventListener;
import com.netease.yunxin.kit.chatkit.manager.UserAIBotManager;
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
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationGroupBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationGroupType;
import com.netease.yunxin.kit.conversationkit.ui.page.interfaces.IConversationCallback;
import com.netease.yunxin.kit.conversationkit.ui.page.interfaces.ILoadListener;
import com.netease.yunxin.kit.conversationkit.ui.page.viewmodel.ConversationGroupViewModel;
import com.netease.yunxin.kit.conversationkit.ui.page.viewmodel.ConversationViewModel;
import com.netease.yunxin.kit.conversationkit.ui.view.ConversationGroupBar;
import com.netease.yunxin.kit.conversationkit.ui.view.ConversationView;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 会话列表基类,包含会话列表的获取和UI事件，UI层面分为两个子类分别代表不同的UI风格 1. ConversationFragment: 普通版会话列表 2.
 * FunConversationFragment: 娱乐版会话列
 */
public abstract class ConversationBaseFragment extends BaseFragment implements ILoadListener {

  private static final String UNREAD_TAG = "unread";
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
  protected ConversationGroupViewModel conversationGroupViewModel;
  private boolean conversationGroupBarBound;
  private boolean conversationGroupEnabled;
  private boolean conversationGroupStateInitialized;
  protected List<ConversationBean> sourceConversationList = new ArrayList<>();
  // 会话页面顶部TitleBar
  protected TitleBarView titleBarView;
  // 网络错误View，断网情况下设置显示。子类可个性化定制，父类值根据业务数据控制是否展示
  protected View networkErrorView;
  private boolean networkListenerRegistered;
  private boolean networkDisconnected;
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
    if (networkErrorView != null || isConversationGroupEnabled()) {
      networkDisconnected = !NetworkUtils.isConnected();
      NetworkUtils.registerNetworkStatusChangedListener(networkStateListener);
      networkListenerRegistered = true;
    }
    initData();
    bindView();
    registerObserver();
    UserAIBotManager.addEventListener(userAIBotEventListener);
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
    applyConversationGroupConfig();
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
          navigateConversation(data);
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
          navigateConversation(data);
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

  protected void navigateConversation(BaseBean data) {
    if (data instanceof ConversationBean) {
      ConversationBean conversation = (ConversationBean) data;
      String targetId = conversation.getTargetId();
      if (conversation.getConversationType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P
          && UserAIBotManager.isUserAIBot(targetId)) {
        boolean funStyle =
            RouterConstant.PATH_FUN_CHAT_P2P_PAGE.equals(data.router)
                || RouterConstant.PATH_FUN_CHAT_BOT_SUB_SESSION_LIST_PAGE.equals(data.router);
        String router =
            funStyle
                ? RouterConstant.PATH_FUN_CHAT_BOT_SUB_SESSION_LIST_PAGE
                : RouterConstant.PATH_CHAT_BOT_SUB_SESSION_LIST_PAGE;
        XKitRouter.withKey(router)
            .withParam(RouterConstant.CHAT_ID_KRY, targetId)
            .withParam(RouterConstant.KEY_SESSION_NAME, conversation.getConversationName())
            .withParam(
                RouterConstant.KEY_BOT_SUB_SESSION_CONVERSATION_ID,
                conversation.getConversationId())
            .withContext(requireContext())
            .navigate();
        return;
      }
    }
    XKitRouter.withKey(data.router)
        .withParam(data.paramKey, data.param)
        .withContext(requireContext())
        .navigate();
  }

  // 设置外部定制的ViewHolderFactory
  public void setViewHolderFactory(IConversationFactory factory) {
    conversationFactory = factory;
    if (viewModel != null) {
      viewModel.setConversationFactory(factory);
    }
    if (conversationGroupViewModel != null) {
      conversationGroupViewModel.setConversationFactory(factory);
    }
    if (conversationView != null) {
      conversationView.setViewHolderFactory(factory);
    }
  }

  // 初始化观察者
  protected void initData() {
    viewModel = new ViewModelProvider(this).get(ConversationViewModel.class);
    conversationGroupViewModel = new ViewModelProvider(this).get(ConversationGroupViewModel.class);
    conversationComparator = ConversationUtils.getConversationSortOrderComparator();
    viewModel.setComparator(conversationComparator);
    conversationGroupViewModel.setComparator(conversationComparator);
    if (conversationFactory != null) {
      viewModel.setConversationFactory(conversationFactory);
      conversationGroupViewModel.setConversationFactory(conversationFactory);
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
                    sourceConversationList = copyConversationList(result.getData());
                  } else if (result.getType() == FetchResult.FetchType.Add) {
                    appendSourceConversations(result.getData());
                  }
                  renderConversationGroupList();
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
              updateSourceConversations(result.getData());
              renderConversationGroupList();
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
              renderConversationGroupList();
              conversationView.updateAit(result.getData());
            } else if (result.getType() == FetchResult.FetchType.Remove
                && conversationView != null) {
              ALog.d(LIB_TAG, TAG, "aitObserver remove, Success");
              ConversationHelper.updateAitInfo(result.getData(), false);
              renderConversationGroupList();
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
              removeSourceConversations(result.getData());
              if (conversationGroupViewModel != null) {
                conversationGroupViewModel.removeCachedConversations(result.getData());
              }
              renderConversationGroupList();
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

  private void bindConversationGroupBar() {
    if (!isConversationGroupEnabled()
        || conversationGroupViewModel == null
        || conversationGroupBarBound) {
      return;
    }
    if (conversationView == null) {
      return;
    }
    conversationGroupBarBound = true;
    conversationGroupViewModel
        .getGroupLiveData()
        .observe(
            getViewLifecycleOwner(),
            groups -> {
              if (!isConversationGroupEnabled()) {
                return;
              }
              ALog.d(
                  UNREAD_TAG,
                  "fragment groupLiveData observe groups="
                      + describeGroups(groups)
                      + " selected="
                      + (conversationGroupViewModel.getSelectedGroup() == null
                          ? "null"
                          : conversationGroupViewModel.getSelectedGroup().getId()));
              conversationView.setConversationGroups(
                  groups,
                  conversationGroupViewModel.getSelectedGroup() == null
                      ? ConversationGroupBean.ID_ALL
                      : conversationGroupViewModel.getSelectedGroup().getId(),
                  groupClickListener);
            });
    conversationGroupViewModel
        .getSelectedGroupLiveData()
        .observe(
            getViewLifecycleOwner(),
            group -> {
              if (!isConversationGroupEnabled()) {
                return;
              }
              if (group != null) {
                conversationView.setSelectedConversationGroup(group.getId());
                if (group.getType() == ConversationGroupType.CUSTOM) {
                  if (emptyView != null) {
                    emptyView.setVisibility(View.GONE);
                  }
                  conversationGroupViewModel.loadSelectedGroupConversations(true);
                }
              }
              renderConversationGroupList();
            });
    conversationGroupViewModel
        .getGroupConversationLiveData()
        .observe(
            getViewLifecycleOwner(),
            data -> {
              if (isConversationGroupEnabled()
                  && conversationGroupViewModel.isSelectedCustomGroup()) {
                conversationView.setData(data);
                updateEmptyView();
              }
            });
    conversationGroupViewModel.loadGroups();
  }

  private void applyConversationGroupConfig() {
    boolean enabled = isConversationGroupEnabled();
    if (conversationGroupStateInitialized && conversationGroupEnabled == enabled) {
      return;
    }
    conversationGroupStateInitialized = true;
    conversationGroupEnabled = enabled;
    if (conversationView == null) {
      return;
    }
    if (!enabled) {
      conversationView.clearConversationGroups();
      renderConversationGroupList();
      return;
    }
    boolean wasBound = conversationGroupBarBound;
    bindConversationGroupBar();
    if (wasBound && conversationGroupViewModel != null) {
      conversationGroupViewModel.loadGroups();
    }
  }

  private boolean isConversationGroupEnabled() {
    return IMKitClient.enableV2CloudConversation()
        && IMKitConfigCenter.getEnableConversationGroup();
  }

  private final ConversationGroupBar.OnGroupClickListener groupClickListener =
      new ConversationGroupBar.OnGroupClickListener() {
        @Override
        public void onGroupClick(ConversationGroupBean group) {
          conversationGroupViewModel.selectGroup(group);
        }

        @Override
        public void onSettingClick() {
          XKitRouter.withKey(getConversationGroupManagePagePath())
              .withContext(requireContext())
              .navigate();
        }
      };

  protected String getConversationGroupManagePagePath() {
    return RouterConstant.PATH_CONVERSATION_GROUP_MANAGE_PAGE;
  }

  private void renderConversationGroupList() {
    renderConversationGroupList(true);
  }

  private void renderConversationGroupList(boolean syncConversations) {
    ALog.d(
        UNREAD_TAG,
        "fragment renderConversationGroupList sync="
            + syncConversations
            + " sourceSize="
            + (sourceConversationList == null ? 0 : sourceConversationList.size())
            + " selected="
            + (conversationGroupViewModel == null
                    || conversationGroupViewModel.getSelectedGroup() == null
                ? "null"
                : conversationGroupViewModel.getSelectedGroup().getId()));
    if (conversationView == null) {
      return;
    }
    if (!isConversationGroupEnabled() || conversationGroupViewModel == null) {
      conversationView.setData(sourceConversationList);
      updateEmptyView();
      return;
    }
    List<ConversationBean> source =
        syncConversations
            ? sourceConversationList
            : conversationGroupViewModel.getCurrentConversations();
    if (syncConversations) {
      conversationGroupViewModel.setCurrentConversations(sourceConversationList);
    }
    if (conversationGroupViewModel.isSelectedCustomGroup()) {
      List<ConversationBean> customGroupConversations =
          conversationGroupViewModel.getSelectedGroupConversations();
      conversationView.setData(customGroupConversations);
      if (customGroupConversations.isEmpty()
          && conversationGroupViewModel.hasMoreSelectedGroupConversations()) {
        conversationGroupViewModel.loadSelectedGroupConversations(false);
      }
      return;
    } else {
      List<ConversationBean> filteredConversations =
          conversationGroupViewModel.filterCurrentGroup(source);
      ALog.d(
          UNREAD_TAG,
          "fragment render filtered size="
              + filteredConversations.size()
              + " sourceSize="
              + (source == null ? 0 : source.size()));
      conversationView.setData(filteredConversations);
      if (filteredConversations.isEmpty()
          && conversationGroupViewModel.isSelectedVirtualFilterGroup()
          && viewModel.hasMore()) {
        viewModel.loadMore();
      }
    }
    updateEmptyView();
  }

  private String describeGroups(List<ConversationGroupBean> groups) {
    if (groups == null) {
      return "null";
    }
    StringBuilder builder = new StringBuilder("[");
    for (int index = 0; index < groups.size(); index++) {
      ConversationGroupBean group = groups.get(index);
      if (index > 0) {
        builder.append(", ");
      }
      if (group == null) {
        builder.append("null");
      } else {
        builder
            .append("{id=")
            .append(group.getId())
            .append(", type=")
            .append(group.getType())
            .append(", visible=")
            .append(group.isVisible())
            .append(", unread=")
            .append(group.getUnreadOrCount())
            .append("}");
      }
    }
    builder.append("]");
    return builder.toString();
  }

  private List<ConversationBean> copyConversationList(List<ConversationBean> data) {
    return data == null ? new ArrayList<>() : new ArrayList<>(data);
  }

  private void appendSourceConversations(List<ConversationBean> data) {
    if (data == null || data.isEmpty()) {
      return;
    }
    sourceConversationList.addAll(data);
    sortSourceConversations();
  }

  private void updateSourceConversations(List<ConversationBean> data) {
    if (data == null || data.isEmpty()) {
      return;
    }
    for (ConversationBean bean : data) {
      int index = indexOfSourceConversation(bean.getConversationId());
      if (index >= 0) {
        sourceConversationList.set(index, bean);
      } else {
        sourceConversationList.add(bean);
      }
    }
    sortSourceConversations();
  }

  private void removeSourceConversations(List<String> conversationIds) {
    if (conversationIds == null || conversationIds.isEmpty()) {
      return;
    }
    Iterator<ConversationBean> iterator = sourceConversationList.iterator();
    while (iterator.hasNext()) {
      ConversationBean bean = iterator.next();
      if (conversationIds.contains(bean.getConversationId())) {
        iterator.remove();
      }
    }
  }

  private int indexOfSourceConversation(String conversationId) {
    for (int i = 0; i < sourceConversationList.size(); i++) {
      if (TextUtils.equals(sourceConversationList.get(i).getConversationId(), conversationId)) {
        return i;
      }
    }
    return -1;
  }

  private void sortSourceConversations() {
    if (conversationComparator != null) {
      Collections.sort(sourceConversationList, conversationComparator);
    }
  }

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
    applyConversationGroupConfig();
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
    UserAIBotManager.removeEventListener(userAIBotEventListener);
    conversationGroupBarBound = false;
    conversationGroupStateInitialized = false;
    super.onDestroyView();
    if (networkListenerRegistered) {
      NetworkUtils.unregisterNetworkStatusChangedListener(networkStateListener);
      networkListenerRegistered = false;
    }
    unregisterObserver();
  }

  private final UserAIBotEventListener userAIBotEventListener =
      new UserAIBotEventListener() {
        @Override
        public void onUserAIBotChanged(List<? extends V2NIMUserAIBot> bots) {
          ConversationView currentView = conversationView;
          if (currentView != null) {
            currentView.post(currentView::refreshConversations);
          }
        }

        @Override
        public void onUserAIBotRemoved(String accountId) {
          ConversationView currentView = conversationView;
          if (currentView != null) {
            currentView.post(
                () ->
                    currentView.resetBotConversationRouter(
                        V2NIMConversationIdUtil.p2pConversationId(accountId)));
          }
        }
      };

  private final NetworkUtils.NetworkStateListener networkStateListener =
      new NetworkUtils.NetworkStateListener() {

        @Override
        public void onConnected(NetworkUtils.NetworkType networkType) {
          boolean shouldReloadGroups = networkDisconnected;
          networkDisconnected = false;
          if (networkErrorView != null) {
            networkErrorView.setVisibility(View.GONE);
          }
          if (shouldReloadGroups
              && isConversationGroupEnabled()
              && conversationGroupViewModel != null) {
            ALog.d(UNREAD_TAG, "network reconnected, reload conversation groups");
            conversationGroupViewModel.loadGroups();
          }
        }

        @Override
        public void onDisconnected() {
          networkDisconnected = true;
          if (networkErrorView != null) {
            networkErrorView.setVisibility(View.VISIBLE);
          }
        }
      };

  private void checkNetwork() {
    boolean connected = NetworkUtils.isConnected();
    if (!connected) {
      networkDisconnected = true;
    }
    if (networkErrorView == null) {
      return;
    }
    if (connected) {
      networkErrorView.setVisibility(View.GONE);
    } else {
      networkErrorView.setVisibility(View.VISIBLE);
    }
  }

  // 是否有更多数据
  @Override
  public boolean hasMore() {
    if (isConversationGroupEnabled()
        && conversationGroupViewModel != null
        && conversationGroupViewModel.isSelectedCustomGroup()) {
      return conversationGroupViewModel.hasMoreSelectedGroupConversations();
    }
    return viewModel.hasMore();
  }

  // 加载下一页数据
  @Override
  public void loadMore(Object last) {
    if (isConversationGroupEnabled()
        && conversationGroupViewModel != null
        && conversationGroupViewModel.isSelectedCustomGroup()) {
      conversationGroupViewModel.loadSelectedGroupConversations(false);
    } else {
      viewModel.loadMore();
    }
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

  /**
   * 获取标题栏
   *
   * @return 标题栏
   */
  public TitleBarView getTitleBar() {
    return null;
  }

  /**
   * 获取顶部布局
   *
   * @return 顶部布局
   */
  public LinearLayout getTopLayout() {
    return null;
  }

  /**
   * 获取主体布局
   *
   * @return 主体布局
   */
  public LinearLayout getBodyLayout() {
    return null;
  }

  /**
   * 获取底部布局
   *
   * @return 底部布局
   */
  public FrameLayout getBottomLayout() {
    return null;
  }

  /**
   * 获取主体顶部布局的顶部布局 如果需要再会话列表和标题之间添加一些UI，可以使用此布局
   *
   * @return 主体顶部布局
   */
  public FrameLayout getBodyTopLayout() {
    return null;
  }

  /**
   * 获取展示错误信息的TextView 当前断网的错误信息展示在此TextView上
   *
   * @return 主体底部布局
   */
  public TextView getErrorTextView() {
    return null;
  }

  /** 设置空布局是否可见 */
  public void setEmptyViewVisible(int visible) {}

  /**
   * 获取空布局 当会话列表为空时，展示此布局。当前空布局包含文本和图片
   *
   * @return 空布局
   */
  public View getEmptyView() {
    return null;
  }
}
