// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page.viewmodel;

import android.os.SystemClock;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncState;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncType;
import com.netease.nimlib.sdk.v2.conversation.V2NIMConversationListener;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.sdk.v2.conversation.result.V2NIMConversationResult;
import com.netease.nimlib.sdk.v2.subscription.V2NIMSubscribeListener;
import com.netease.nimlib.sdk.v2.subscription.model.V2NIMUserStatus;
import com.netease.nimlib.sdk.v2.team.V2NIMTeamListener;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.user.V2NIMUser;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ChatConstants;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.OnlineStatusManager;
import com.netease.yunxin.kit.chatkit.impl.ConversationListenerImpl;
import com.netease.yunxin.kit.chatkit.impl.LoginDetailListenerImpl;
import com.netease.yunxin.kit.chatkit.impl.TeamListenerImpl;
import com.netease.yunxin.kit.chatkit.manager.AIUserChangeListener;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.chatkit.utils.ConversationIdUtils;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.conversationkit.ui.IConversationFactory;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationUtils;
import com.netease.yunxin.kit.conversationkit.ui.model.AIUserBean;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.page.DefaultViewHolderFactory;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.custom.AitEvent;
import com.netease.yunxin.kit.corekit.im2.custom.AitInfo;
import com.netease.yunxin.kit.corekit.im2.custom.TeamEvent;
import com.netease.yunxin.kit.corekit.im2.custom.TeamEventAction;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.listener.ContactChangeType;
import com.netease.yunxin.kit.corekit.im2.listener.ContactListener;
import com.netease.yunxin.kit.corekit.im2.model.FriendAddApplicationInfo;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** 会话列表逻辑层ViewModel */
public class ConversationViewModel extends BaseViewModel {

  private final String TAG = "ConversationViewModel";
  private final String LIB_TAG = "ConversationKit-UI";

  // 未读数LiveData，用于通知未读数变化
  private final MutableLiveData<FetchResult<Integer>> unreadCountLiveData = new MutableLiveData<>();
  // 会话列表LiveData，用于通知会话列表查询结果
  private final MutableLiveData<FetchResult<List<ConversationBean>>> queryLiveData =
      new MutableLiveData<>();
  // 会话变化LiveData，用于通知会话信息变化变更
  private final MutableLiveData<FetchResult<List<ConversationBean>>> changeLiveData =
      new MutableLiveData<>();
  // @信息LiveData，用于通知@信息变更
  private final MutableLiveData<FetchResult<List<String>>> aitLiveData = new MutableLiveData<>();
  // 删除会话LiveData，用于通知会话删除结果
  private final MutableLiveData<FetchResult<List<String>>> deleteLiveData = new MutableLiveData<>();
  // 更新会话
  private final MutableLiveData<FetchResult<List<String>>> updateLiveData = new MutableLiveData<>();

  // 置顶AI机器人列表
  private final MutableLiveData<FetchResult<List<AIUserBean>>> aiRobotLiveData =
      new MutableLiveData<>();

  // 会话列表排序比较器
  private Comparator<ConversationBean> comparator;
  private IConversationFactory conversationFactory = new DefaultViewHolderFactory();
  // 分页加载，每页加载数量
  private static final int PAGE_LIMIT = 100;
  // 分页加载，当前加载偏移量
  private long mOffset = 0;
  // 分页加载，是否还有更多数据
  private boolean hasMore = true;
  // 数据查询是否已经开始
  private boolean hasStart = false;
  private final int onlineDiff = 20;

  private final List<String> onlineScrollAccountList = new ArrayList<>();

  public ConversationViewModel() {
    // 注册会话监听
    ConversationRepo.addConversationListener(conversationListener);
    // 注册群组监听,用于监听群解散和退出
    TeamRepo.addTeamListener(teamListener);
    // 注册@信息监听,业务层逻辑
    EventCenter.registerEventNotify(aitNotify);
    EventCenter.registerEventNotify(dismissTeamEvent);
    // 注册登录监听,用于同步数据完成后拉取数据，避免会话中会话名称和头像为空
    IMKitClient.addLoginDetailListener(loginDetailListener);
    if (IMKitConfigCenter.getEnableAIUser()) {
      ContactRepo.addContactListener(contactListener);
      AIUserManager.addAIUserChangeListener(aiUserChangeListener);
    }
    // 如果开启在线状态
    if (IMKitConfigCenter.getEnableOnlineStatus()) {
      OnlineStatusManager.addUserOnlineListener(onlineListener);
    }
  }

  // @信息监听
  private final EventNotify<AitEvent> aitNotify =
      new EventNotify<AitEvent>() {
        @Override
        public void onNotify(@NonNull AitEvent aitEvent) {
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Finish);
          if (aitEvent.getAitInfoList() == null) {
            return;
          }
          if (aitEvent.getEventType() == AitEvent.AitEventType.Arrive
              || aitEvent.getEventType() == AitEvent.AitEventType.Load) {
            result.setFetchType(FetchResult.FetchType.Add);
          } else {
            result.setFetchType(FetchResult.FetchType.Remove);
          }
          List<AitInfo> aitInfoList = aitEvent.getAitInfoList();
          List<String> sessionIdList = new ArrayList<>();
          for (AitInfo info : aitInfoList) {
            sessionIdList.add(info.getConversationId());
          }
          result.setData(sessionIdList);
          aitLiveData.setValue(result);
        }

        @NonNull
        @Override
        public String getEventType() {
          return "AitEvent";
        }
      };

  private final EventNotify<TeamEvent> dismissTeamEvent =
      new EventNotify<TeamEvent>() {
        @Override
        public void onNotify(@NonNull TeamEvent teamEvent) {
          if (teamEvent.getAction().equals(TeamEventAction.ACTION_DISMISS)) {
            ALog.d(LIB_TAG, TAG, "dismissTeamEvent,teamId:" + teamEvent.getTeamId());
            String id = teamEvent.getTeamId();
            if (TextUtils.isEmpty(id)
                || !IMKitConfigCenter.getEnableDismissTeamDeleteConversation()) {
              return;
            }
            deleteConversation(
                V2NIMConversationIdUtil.conversationId(
                    id, V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM),
                false);
          }
        }

        @NonNull
        @Override
        public String getEventType() {
          return "TeamEvent";
        }
      };

  private final LoginDetailListenerImpl loginDetailListener =
      new LoginDetailListenerImpl() {
        @Override
        public void onDataSync(V2NIMDataSyncType type, V2NIMDataSyncState state, V2NIMError error) {
          if (type == V2NIMDataSyncType.V2NIM_DATA_SYNC_MAIN
              && state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED) {
            ALog.d(
                LIB_TAG,
                TAG,
                "Performance loginDetailListener V2NIM_DATA_SYNC_MAIN COMPLETED timestamp:"
                    + SystemClock.elapsedRealtime());
            getConversationData();
          }
        }
      };

  private final ContactListener contactListener =
      new ContactListener() {
        @Override
        public void onContactChange(
            @NonNull ContactChangeType changeType,
            @NonNull List<? extends UserWithFriend> contactList) {
          if (changeType == ContactChangeType.Update) {
            for (UserWithFriend user : contactList) {
              if (TextUtils.equals(user.getAccount(), IMKitClient.account())) {
                getAiRobotUserList();
              }
            }
          }
        }

        @Override
        public void onFriendAddApplication(@NonNull FriendAddApplicationInfo friendApplication) {}

        @Override
        public void onFriendAddRejected(@NonNull FriendAddApplicationInfo rejectionInfo) {}
      };

  // 设置会话列表排序比较器
  public void setComparator(Comparator<ConversationBean> comparator) {
    this.comparator = comparator;
  }

  // 设置会话列表ViewHolder工厂
  public void setConversationFactory(IConversationFactory factory) {
    this.conversationFactory = factory;
  }

  public MutableLiveData<FetchResult<Integer>> getUnreadCountLiveData() {
    return unreadCountLiveData;
  }

  public MutableLiveData<FetchResult<List<ConversationBean>>> getQueryLiveData() {
    return queryLiveData;
  }

  public MutableLiveData<FetchResult<List<ConversationBean>>> getChangeLiveData() {
    return changeLiveData;
  }

  public MutableLiveData<FetchResult<List<String>>> getDeleteLiveData() {
    return deleteLiveData;
  }

  public MutableLiveData<FetchResult<List<String>>> getAitLiveData() {
    return aitLiveData;
  }

  public MutableLiveData<FetchResult<List<AIUserBean>>> getAiRobotLiveData() {
    return aiRobotLiveData;
  }

  public MutableLiveData<FetchResult<List<String>>> getUpdateLiveData() {
    return updateLiveData;
  }

  /** 获取未读数 */
  public void getUnreadCount() {
    int unreadCount = ConversationRepo.getTotalUnreadCount();
    ALog.d(LIB_TAG, TAG, "getUnreadCount,onSuccess:" + unreadCount);
    FetchResult<Integer> fetchResult = new FetchResult<>(LoadStatus.Success);
    fetchResult.setData(unreadCount);
    unreadCountLiveData.setValue(fetchResult);
  }

  /** 获取会话列表相关数据，包括会话列表数据和未读数 */
  public void getConversationData() {
    XKitRouter.withKey(RouterConstant.PATH_CHAT_AIT_NOTIFY_ACTION).navigate();
    getConversationByPage(0);
    getUnreadCount();
    getAiRobotUserList();
  }

  /** 加载更多会话列表数据 */
  public void loadMore() {
    ALog.d(LIB_TAG, TAG, "loadMore:");
    getConversationByPage(mOffset);
  }

  /**
   * 查询会话列表
   *
   * @param offSet 偏移量
   */
  private void getConversationByPage(long offSet) {
    ALog.d(LIB_TAG, TAG, "queryConversation:" + offSet);
    if (hasStart) {
      ALog.d(LIB_TAG, TAG, "queryConversation,has Started return");
      return;
    }
    hasStart = true;
    ALog.d(
        LIB_TAG,
        TAG,
        "Performance queryConversation start timestamp:" + SystemClock.elapsedRealtime());
    ConversationRepo.getConversationList(
        offSet,
        PAGE_LIMIT,
        new FetchCallback<V2NIMConversationResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(LIB_TAG, TAG, "queryConversation,onError:" + errorCode + "," + errorMsg);
            hasStart = false;
          }

          @Override
          public void onSuccess(@Nullable V2NIMConversationResult data) {
            ALog.d(
                LIB_TAG,
                TAG,
                "queryConversation,onSuccess:"
                    + ((data != null && data.getConversationList() != null)
                        ? data.getConversationList().size()
                        : 0));
            ALog.d(
                LIB_TAG,
                TAG,
                "Performance queryConversation onSuccess timestamp:"
                    + SystemClock.elapsedRealtime());
            FetchResult<List<ConversationBean>> result = new FetchResult<>(LoadStatus.Success);
            result.setType(offSet > 0 ? FetchResult.FetchType.Add : FetchResult.FetchType.Init);

            if (data != null && data.getConversationList() != null) {
              checkConversationAndRemove(data.getConversationList());
              List<ConversationBean> resultData =
                  createConversationBean(data.getConversationList());
              if (comparator != null) {
                Collections.sort(resultData, comparator);
              }
              result.setData(resultData);
              hasMore = resultData.size() == PAGE_LIMIT;
              mOffset = data.getOffset();
              checkConversationNameAndRequest(resultData);
              if (IMKitConfigCenter.getEnableOnlineStatus() && offSet == 0) {
                subscribeOnlineStatus(resultData);
              }
            }
            queryLiveData.setValue(result);
            hasStart = false;
          }
        });
  }

  private void checkConversationNameAndRequest(List<ConversationBean> resultData) {
    List<String> userIdList = new ArrayList<>();
    if (resultData != null) {
      for (int index = 0; index < resultData.size(); index++) {
        ConversationBean conversationBean = resultData.get(index);
        if (TextUtils.equals(conversationBean.getConversationName(), conversationBean.getTargetId())
            && conversationBean.infoData.getType()
                == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
          userIdList.add(conversationBean.getTargetId());
        }
      }
      if (!userIdList.isEmpty()) {
        ALog.d(LIB_TAG, TAG, "getUserInfo start:" + userIdList.size());
        ContactRepo.getUserInfo(userIdList, null);
      }
    }
  }

  public void getAiRobotUserList() {
    if (!IMKitConfigCenter.getEnableAIUser()) {
      return;
    }
    List<V2NIMAIUser> aiUserList = AIUserManager.getPinDefaultUserList();
    if (aiUserList.isEmpty()) {
      return;
    }
    ContactRepo.getUserInfo(
        Collections.singletonList(IMKitClient.account()),
        new FetchCallback<List<V2NIMUser>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {}

          @Override
          public void onSuccess(@Nullable List<V2NIMUser> data) {
            if (data != null && !data.isEmpty()) {
              V2NIMUser user = data.get(0);
              if (user != null) {
                ALog.d(LIB_TAG, TAG, "getAiRobotUserList,user:" + user.getServerExtension());
                try {
                  String userExtStr = user.getServerExtension();
                  Set<String> userUnpinSet = null;
                  if (!TextUtils.isEmpty(userExtStr)) {
                    JSONObject userExtJson = new JSONObject(userExtStr);
                    JSONArray userUnpinArray =
                        userExtJson.optJSONArray(ChatConstants.KEY_UNPIN_AI_USERS);
                    if (userUnpinArray != null) {
                      userUnpinSet = ConversationUtils.toHashSet(userUnpinArray);
                    }
                  }
                  List<AIUserBean> aiUserBeanList = new ArrayList<>();
                  for (V2NIMAIUser aiUser : aiUserList) {
                    if (userUnpinSet == null || !userUnpinSet.contains(aiUser.getAccountId())) {
                      aiUserBeanList.add(new AIUserBean(aiUser));
                    }
                  }
                  FetchResult<List<AIUserBean>> result = new FetchResult<>(LoadStatus.Success);
                  result.setData(aiUserBeanList);
                  aiRobotLiveData.setValue(result);

                } catch (JSONException e) {
                  ALog.e(LIB_TAG, TAG, "ServerExtension format error");
                }
              }
            }
          }
        });
  }

  /**
   * 删除会话
   *
   * @param conversationId 会话ID
   */
  public void deleteConversation(String conversationId, boolean showErrorToast) {
    ConversationRepo.deleteConversation(
        conversationId,
        false,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "deleteConversation,onError:" + errorCode + "," + errorMsg);
            if (showErrorToast) {
              ErrorUtils.showErrorCodeToast(IMKitClient.getApplicationContext(), errorCode);
            }
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            // 删除回调会走conversationListener.onConversationDeleted方法
            ALog.d(LIB_TAG, TAG, "deleteConversation,onSuccess:" + conversationId);
          }
        });
  }

  /** 置顶会话 */
  public void addStickTop(ConversationBean param) {
    ALog.d(LIB_TAG, TAG, "addStickTop:" + param.getConversationId());
    ConversationRepo.setStickTop(
        param.getConversationId(),
        true,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "addStickTop,onFailed:" + errorCode);
            ErrorUtils.showErrorCodeToast(IMKitClient.getApplicationContext(), errorCode);
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ALog.d(LIB_TAG, TAG, "addStickTop,onSuccess:" + param.getConversationId());
          }
        });
  }

  /**
   * 取消置顶会话
   *
   * @param conversationBean 会话信息
   */
  public void removeStick(ConversationBean conversationBean) {
    ALog.d(LIB_TAG, TAG, "removeStick:" + conversationBean.getConversationId());
    ConversationRepo.setStickTop(
        conversationBean.getConversationId(),
        false,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "removeStick,onFailed:" + errorCode);
            if (errorCode == ConversationConstant.ERROR_CODE_NETWORK) {
              ToastX.showShortToast(R.string.conversation_network_error_tip);
            }
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ALog.d(LIB_TAG, TAG, "removeStick,onSuccess:" + conversationBean.getConversationId());
          }
        });
  }

  // 会话监听Listener
  private final V2NIMConversationListener conversationListener =
      new ConversationListenerImpl() {

        @Override
        public void onSyncFinished() {
          ALog.d(
              LIB_TAG,
              TAG,
              "Performance conversationListener onSyncFinished timestamp:"
                  + SystemClock.elapsedRealtime());
          getConversationData();
        }

        @Override
        public void onSyncFailed(V2NIMError error) {
          ALog.d(LIB_TAG, TAG, "conversationListener onSyncFailed:");
        }

        @Override
        public void onConversationCreated(V2NIMConversation conversation) {
          ALog.d(
              LIB_TAG,
              TAG,
              "conversationListener onConversationCreated,conversation:"
                  + (conversation != null ? conversation.getConversationId() : "id is null"));
          FetchResult<List<ConversationBean>> result = new FetchResult<>(LoadStatus.Success);
          result.setType(FetchResult.FetchType.Add);
          List<V2NIMConversation> data = new ArrayList<>();
          data.add(conversation);
          List<ConversationBean> resultData = createConversationBean(data);
          // 先更新在线状态数据
          if (IMKitConfigCenter.getEnableOnlineStatus()) {
            subscribeOnlineStatus(resultData);
          }
          result.setData(resultData);
          changeLiveData.setValue(result);
        }

        @Override
        public void onConversationDeleted(List<String> conversationIds) {
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success);
          result.setData(conversationIds);
          ALog.d(LIB_TAG, TAG, "conversationListener onConversationDeleted,onSuccess");
          deleteLiveData.setValue(result);
        }

        @Override
        public void onConversationChanged(List<V2NIMConversation> conversationList) {
          ALog.d(
              LIB_TAG,
              TAG,
              "conversationListener onConversationChanged,conversation:"
                  + (conversationList != null ? conversationList.size() : "0"));
          if (conversationList == null || conversationList.isEmpty()) {
            return;
          }
          List<V2NIMConversation> changeList = new ArrayList<>();
          List<String> deleteList = new ArrayList<>();
          for (V2NIMConversation conversation : conversationList) {
            ALog.d(
                LIB_TAG,
                TAG,
                "conversationListener onConversationChanged,conversation:"
                    + (conversation != null ? conversation.getConversationId() : "id is null"));
            if (IMKitConfigCenter.getEnableDismissTeamDeleteConversation()
                && conversation != null
                && ConversationUtils.isDismissTeamMsg(conversation.getLastMessage())) {
              deleteList.add(conversation.getConversationId());
              deleteConversation(conversation.getConversationId(), false);
            } else {
              changeList.add(conversation);
            }
          }
          if (!changeList.isEmpty()) {
            ALog.d(
                LIB_TAG,
                TAG,
                "conversationListener onConversationChanged,changeList:" + changeList.size());
            FetchResult<List<ConversationBean>> result = new FetchResult<>(LoadStatus.Success);
            result.setType(FetchResult.FetchType.Update);
            convertAndNotify(result, changeList);
          }
          if (!deleteList.isEmpty()) {
            ALog.d(
                LIB_TAG,
                TAG,
                "conversationListener onConversationChanged,deleteList:" + deleteList.size());
            FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success);
            result.setData(deleteList);
            deleteLiveData.setValue(result);
          }
        }

        @Override
        public void onTotalUnreadCountChanged(int unreadCount) {
          ALog.d(LIB_TAG, TAG, "conversationListener onTotalUnreadCountChanged:" + unreadCount);
          FetchResult<Integer> result = new FetchResult<>(LoadStatus.Success);
          result.setData(unreadCount);
          unreadCountLiveData.setValue(result);
        }
      };

  // 群组监听Listener
  private final V2NIMTeamListener teamListener =
      new TeamListenerImpl() {

        @Override
        public void onTeamDismissed(@Nullable V2NIMTeam team) {
          if (team == null || !IMKitConfigCenter.getEnableDismissTeamDeleteConversation()) {
            return;
          }
          ALog.d(LIB_TAG, TAG, "teamListener onTeamDismissed:" + team.getTeamId());
          String conversationId =
              V2NIMConversationIdUtil.conversationId(
                  team.getTeamId(), V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM);
          deleteConversation(conversationId, false);
        }

        @Override
        public void onTeamLeft(@Nullable V2NIMTeam team, boolean isKicked) {
          if (team == null || !IMKitConfigCenter.getEnableDismissTeamDeleteConversation()) {
            return;
          }
          ALog.d(LIB_TAG, TAG, "teamListener onTeamLeft:" + team.getTeamId());
          String conversationId =
              V2NIMConversationIdUtil.conversationId(
                  team.getTeamId(), V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM);
          deleteConversation(conversationId, false);
        }
      };

  protected V2NIMSubscribeListener onlineListener =
      new V2NIMSubscribeListener() {
        @Override
        public void onUserStatusChanged(List<V2NIMUserStatus> userStatusList) {
          if (userStatusList == null || userStatusList.isEmpty()) {
            return;
          }
          List<String> conversationIdList = new ArrayList<>();
          for (V2NIMUserStatus userStatus : userStatusList) {
            if (userStatus != null && userStatus.getAccountId() != null) {
              String conversationId =
                  ConversationIdUtils.conversationId(
                      userStatus.getAccountId(), V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P);
              conversationIdList.add(conversationId);
            }
          }
          if (!conversationIdList.isEmpty()) {
            FetchResult<List<String>> updateResult =
                new FetchResult<>(FetchResult.FetchType.Update);
            updateResult.setData(conversationIdList);
            updateResult.setLoadStatus(LoadStatus.Finish);
            updateLiveData.setValue(updateResult);
          }
        }
      };

  protected AIUserChangeListener aiUserChangeListener =
      new AIUserChangeListener() {
        @Override
        public void onAIUserChanged(@NonNull List<? extends V2NIMAIUser> aiUsers) {
          ALog.d(LIB_TAG, TAG, "onAIUserChanged:" + aiUsers.size());
          getAiRobotUserList();
        }
      };

  // 会话列表转换并通知LiveData
  public void convertAndNotify(
      FetchResult<List<ConversationBean>> result, List<V2NIMConversation> conversationList) {
    if (conversationList != null) {
      List<ConversationBean> resultData = createConversationBean(conversationList);
      result.setData(resultData);
      changeLiveData.setValue(result);
    }
  }

  //工具方法，将会话信息转换为会话列表数据
  public List<ConversationBean> createConversationBean(List<V2NIMConversation> data) {
    List<ConversationBean> resultData = new ArrayList<>();
    if (data != null) {
      for (int index = 0; index < data.size(); index++) {
        ConversationBean conversationBean = conversationFactory.CreateBean(data.get(index));
        resultData.add(conversationBean);
      }
    }
    return resultData;
  }

  //工具方法，去除重复会话
  public void checkConversationAndRemove(List<V2NIMConversation> data) {
    Set<String> conversationIds = new HashSet<>();
    if (data != null) {
      ALog.d(LIB_TAG, TAG, "checkConversationAndRemove start:" + data.size());
      for (int index = 0; index < data.size(); index++) {
        if (conversationIds.contains(data.get(index).getConversationId())) {
          data.remove(index);
          index--;
        } else {
          conversationIds.add(data.get(index).getConversationId());
        }
      }
      ALog.d(LIB_TAG, TAG, "checkConversationAndRemove end:" + data.size());
    }
  }

  // 是否还有更多数据
  public boolean hasMore() {
    return hasMore;
  }

  protected void subscribeOnlineStatus(List<ConversationBean> conversationBeanList) {
    if (conversationBeanList == null) {
      return;
    }
    List<String> accountList = new ArrayList<>();
    for (ConversationBean conversationBean : conversationBeanList) {
      if (conversationBean.getConversationType()
              == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P
          && !AIUserManager.isAIUser(conversationBean.getTargetId())) {
        accountList.add(conversationBean.getTargetId());
      }
    }
    if (!accountList.isEmpty()) {
      OnlineStatusManager.subscribeUserStatus(accountList);
      // 刷新在线状态
      FetchResult<List<String>> updateResult = new FetchResult<>(FetchResult.FetchType.Update);
      updateResult.setData(accountList);
      updateResult.setLoadStatus(LoadStatus.Finish);
      updateLiveData.setValue(updateResult);
    }
  }

  /** 根据滑动来订阅会话在线状态 前置条件：订阅数量超过3000限制 */
  public void dynamicSubscribeConversation(
      int start, int end, List<ConversationBean> conversationBeanList) {
    if (IMKitConfigCenter.getEnableOnlineStatus() && conversationBeanList != null) {
      if (start < PAGE_LIMIT && end < PAGE_LIMIT) {
        return;
      }
      int startIndex = start - onlineDiff > PAGE_LIMIT ? start - onlineDiff : PAGE_LIMIT + 1;
      int endIndex = end + onlineDiff > PAGE_LIMIT ? end + onlineDiff : PAGE_LIMIT + 1;
      List<String> accountList = new ArrayList<>();
      for (int i = startIndex; i < endIndex && i < conversationBeanList.size(); i++) {
        String conversationId = conversationBeanList.get(i).getConversationId();
        if (ConversationIdUtils.conversationType(conversationId)
            == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
          accountList.add(ConversationIdUtils.conversationTargetId(conversationId));
        }
      }
      if (!accountList.isEmpty()) {
        if (!onlineScrollAccountList.isEmpty()) {
          OnlineStatusManager.unsubscribeUserStatus(onlineScrollAccountList);
          onlineScrollAccountList.clear();
        }
        onlineScrollAccountList.addAll(accountList);
        OnlineStatusManager.subscribeUserStatus(accountList);
        // 刷新在线状态
        FetchResult<List<String>> updateResult = new FetchResult<>(FetchResult.FetchType.Update);
        updateResult.setData(accountList);
        updateResult.setLoadStatus(LoadStatus.Finish);
        updateLiveData.setValue(updateResult);
      }
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ALog.d(LIB_TAG, TAG, "onCleared:");
    ConversationRepo.removeConversationListener(conversationListener);
    TeamRepo.removeTeamListener(teamListener);
    // 注册@信息监听,业务层逻辑
    EventCenter.unregisterEventNotify(aitNotify);
    EventCenter.unregisterEventNotify(dismissTeamEvent);
    IMKitClient.removeLoginDetailListener(loginDetailListener);
    if (IMKitConfigCenter.getEnableAIUser()) {
      ContactRepo.removeContactListener(contactListener);
      AIUserManager.removeAIUserChangeListener(aiUserChangeListener);
    }
  }
}
