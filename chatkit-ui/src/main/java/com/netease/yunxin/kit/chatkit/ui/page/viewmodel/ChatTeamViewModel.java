// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageReferBuilder;
import com.netease.nimlib.sdk.v2.message.V2NIMTeamMessageReadReceipt;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ChatConstants;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamChangeListener;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserChangedListener;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserManager;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.TopStickyMessage;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.custom.TeamEvent;
import com.netease.yunxin.kit.corekit.im2.custom.TeamEventAction;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

/** Team chat info view model team message receipt, team member info for Team chat page */
public class ChatTeamViewModel extends ChatBaseViewModel {

  private static final String TAG = "ChatTeamViewModel";

  private final MutableLiveData<FetchResult<List<V2NIMTeamMessageReadReceipt>>>
      teamMessageReceiptLiveData = new MutableLiveData<>();

  private final MutableLiveData<V2NIMTeam> teamLiveData = new MutableLiveData<>();

  //置顶消息变化通知
  private final MutableLiveData<IMMessageInfo> topMessageLiveData = new MutableLiveData<>();

  //置顶消息权限过滤通知
  private final MutableLiveData<String> topMessagePermissionLiveData = new MutableLiveData<>();
  private boolean myDismiss = false;

  private final TeamUserChangedListener userInfoListener =
      new TeamUserChangedListener() {
        @Override
        public void onUsersChanged(List<String> accountIds) {
          ALog.d(
              LIB_TAG, TAG, "onUsersChanged:" + (accountIds == null ? "null" : accountIds.size()));
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Finish);
          result.setData(accountIds);
          result.setType(FetchResult.FetchType.Update);
          userChangeLiveData.setValue(result);
          updateMyTeamMember(accountIds);
        }

        @Override
        public void onUserDelete(List<String> accountIds) {
          ALog.d(LIB_TAG, TAG, "onUserDelete:" + (accountIds == null ? "null" : accountIds.size()));
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Finish);
          result.setData(accountIds);
          result.setType(FetchResult.FetchType.Remove);
          userChangeLiveData.setValue(result);
        }

        @Override
        public void onUsersAdd(List<String> accountIds) {
          ALog.d(LIB_TAG, TAG, "onUsersAdd:" + (accountIds == null ? "null" : accountIds.size()));
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Finish);
          result.setData(accountIds);
          result.setType(FetchResult.FetchType.Add);
          userChangeLiveData.setValue(result);
        }
      };

  private final TeamChangeListener teamInfoListener =
      new TeamChangeListener() {

        @Override
        public void onTeamUpdate(V2NIMTeam team) {
          ALog.d(LIB_TAG, TAG, "onTeamInfoUpdated:");
          if (team != null && TextUtils.equals(team.getTeamId(), mChatAccountId)) {
            teamLiveData.setValue(team);
            ChatRepo.setCurrentTeam(team);
            if (!TextUtils.isEmpty(team.getServerExtension())) {
              try {

                //处理置顶消息
                handleTopMessage(team.getServerExtension());

                //处理最后一次操作类型
                JSONObject jsonTeam = new JSONObject(team.getServerExtension());
                if (jsonTeam.has(ChatConstants.KEY_EXTENSION_LAST_OPT_TYPE)) {
                  String lastOptType =
                      jsonTeam.optString(ChatConstants.KEY_EXTENSION_LAST_OPT_TYPE);
                  if (TextUtils.equals(
                      lastOptType, ChatConstants.KEY_EXTENSION_STICKY_PERMISSION)) {
                    handleTopMessagePermission(team.getServerExtension());
                  }
                }
              } catch (JSONException e) {
                ALog.e(LIB_TAG, TAG, "handleTopMessage json error:" + e);
              }
            }
          }
        }
      };

  @Override
  public void init(String accountId, V2NIMConversationType sessionType) {
    super.init(accountId, sessionType);
    TeamUserManager.getInstance().init(accountId);
    if (IMKitClient.account() != null) {
      //查询自己在群里的信息
      TeamUserManager.getInstance().getTeamMember(Objects.requireNonNull(IMKitClient.account()));
    }
  }

  @Override
  protected void onTeamMessageReadReceipts(List<V2NIMTeamMessageReadReceipt> readReceipts) {
    super.onTeamMessageReadReceipts(readReceipts);
    ALog.d(
        LIB_TAG,
        TAG,
        "onTeamMessageReadReceipts:" + (readReceipts == null ? "null" : readReceipts.size()));
    FetchResult<List<V2NIMTeamMessageReadReceipt>> receiptResult =
        new FetchResult<>(LoadStatus.Finish);
    receiptResult.setData(readReceipts);
    receiptResult.setType(FetchResult.FetchType.Update);
    receiptResult.setTypeIndex(-1);
    teamMessageReceiptLiveData.setValue(receiptResult);
  }

  //用于记录是否为自己解散的群聊
  private final EventNotify<TeamEvent> teamDismissNotify =
      new EventNotify<TeamEvent>() {
        @Override
        public void onNotify(@NonNull TeamEvent event) {
          ALog.d(LIB_TAG, TAG, "teamDismissNotify:" + event.getTeamId() + event.getAction());
          myDismiss =
              TextUtils.equals(event.getTeamId(), mChatAccountId)
                  && TextUtils.equals(event.getAction(), TeamEventAction.ACTION_DISMISS);
        }

        @NonNull
        @Override
        public String getEventType() {
          return "TeamEvent";
        }
      };

  /** 发送解散群聊事件 如果进入该群，发现该群已被解散，发送解散群聊事件，用于通知会话列表界面更新 */
  public void sendTeamDismissEvent() {
    ALog.d(LIB_TAG, TAG, "sendTeamDismissEvent:" + mChatAccountId);
    EventCenter.notifyEvent(new TeamEvent(mChatAccountId, TeamEventAction.ACTION_DISMISS));
  }

  public MutableLiveData<FetchResult<List<V2NIMTeamMessageReadReceipt>>>
      getTeamMessageReceiptLiveData() {
    return teamMessageReceiptLiveData;
  }

  //置顶消息
  public MutableLiveData<IMMessageInfo> getTopMessageLiveData() {
    return topMessageLiveData;
  }

  //置顶消息权限
  public MutableLiveData<String> getTopMessagePermissionLiveData() {
    return topMessagePermissionLiveData;
  }

  public boolean isMyDismiss() {
    return myDismiss;
  }

  public void refreshTeamMessageReceipt(List<ChatMessageBean> messageBeans) {
    ALog.d(
        LIB_TAG,
        TAG,
        "refreshTeamMessageReceipt:" + (messageBeans == null ? "null" : messageBeans.size()));
    if (messageBeans == null || messageBeans.size() < 1) {
      return;
    }
    List<V2NIMMessage> messages = new ArrayList<>();
    for (ChatMessageBean messageBean : messageBeans) {
      messages.add(messageBean.getMessageData().getMessage());
    }
    ChatRepo.getTeamMessageReceipts(messages);
  }

  /** team info change live data */
  public MutableLiveData<V2NIMTeam> getTeamLiveData() {
    return teamLiveData;
  }

  @Override
  public void addListener() {
    super.addListener();
    TeamUserManager.getInstance().addTeamChangedListener(teamInfoListener);
    TeamUserManager.getInstance().addMemberChangedListener(userInfoListener);
    EventCenter.registerEventNotify(teamDismissNotify);
  }

  @Override
  public void removeListener() {
    super.removeListener();
    TeamUserManager.getInstance().removeTeamChangedListener(teamInfoListener);
    TeamUserManager.getInstance().removeMemberChangedListener(userInfoListener);
    EventCenter.unregisterEventNotify(teamDismissNotify);
  }

  @Override
  public void sendReceipt(V2NIMMessage message) {
    ALog.d(
        LIB_TAG, TAG, "sendReceipt:" + (message == null ? "null" : message.getMessageClientId()));
    if (message != null
        && message.getMessageConfig().isReadReceiptEnabled()
        && showRead
        && !message.getMessageStatus().getReadReceiptSent()) {
      List<V2NIMMessage> msgList = new ArrayList<>();
      msgList.add(message);
      ChatRepo.markTeamMessagesRead(msgList, null);
    }
  }

  public void getTeamInfo() {
    ALog.d(LIB_TAG, TAG, "getTeamInfo:" + mChatAccountId);
    V2NIMTeam team = TeamUserManager.getInstance().getCurrentTeam();
    if (team != null) {
      teamLiveData.setValue(team);
      ChatRepo.setCurrentTeam(team);
      if (!TextUtils.isEmpty(team.getServerExtension())) {
        handleTopMessage(team.getServerExtension());
      }
    }
  }

  @Override
  protected void getTeamMemberInfoWithMessage(List<IMMessageInfo> messages) {
    super.getTeamMemberInfoWithMessage(messages);
    ALog.d(
        LIB_TAG,
        TAG,
        "getTeamMemberInfoWithMessage:" + (messages == null ? "null" : messages.size()));
    Set<String> memberIds = new HashSet<>();
    for (IMMessageInfo message : messages) {
      memberIds.add(message.getMessage().getSenderId());
    }
    List<String> userIds = new ArrayList<>(memberIds);
    userIds.add(IMKitClient.account());
    TeamUserManager.getInstance().getTeamMembers(userIds, null);
  }

  public boolean hasLoadMessage() {
    return hasLoadMessage;
  }

  /**
   * 处理置顶消息权限
   *
   * @param teamExtension 群扩展信息
   */
  public void handleTopMessagePermission(String teamExtension) {
    try {
      JSONObject jsonTeam = new JSONObject(teamExtension);
      if (jsonTeam.has(ChatConstants.KEY_EXTENSION_STICKY_PERMISSION)) {
        String result =
            jsonTeam.optString(
                ChatConstants.KEY_EXTENSION_STICKY_PERMISSION,
                ChatConstants.TYPE_EXTENSION_ALLOW_MANAGER);

        topMessagePermissionLiveData.setValue(result);
      }
    } catch (JSONException e) {
      ALog.e(LIB_TAG, TAG, "handleTopMessage json error:" + e);
    }
  }

  /**
   * 处理置顶消息
   *
   * @param teamExtension 群扩展信息
   */
  public void handleTopMessage(String teamExtension) {
    ALog.d(LIB_TAG, TAG, "handleTopMessage:" + teamExtension);
    try {
      JSONObject jsonTeam = new JSONObject(teamExtension);
      if (jsonTeam.has(ChatConstants.KEY_EXTENSION_STICKY)) {
        JSONObject jsonSticky = jsonTeam.getJSONObject(ChatConstants.KEY_EXTENSION_STICKY);
        int operation = jsonSticky.getInt(ChatConstants.KEY_STICKY_MESSAGE_OPERATION);
        TopStickyMessage topStickyMessage = TopStickyMessage.fromJson(jsonSticky);
        if (topStickyMessage != null && ChatConstants.TYPE_EXTENSION_STICKY_ADD == operation) {
          //添加置顶消息
          getTopStickyMessage(topStickyMessage);
        } else if (ChatConstants.TYPE_EXTENSION_STICKY_REMOVE == operation) {
          //移除置顶消息
          topMessageLiveData.postValue(null);
          ChatUserCache.getInstance().removeTopMessage();
        }
      }
    } catch (JSONException e) {
      ALog.e(LIB_TAG, TAG, "handleTopMessage json error:" + e);
    }
  }

  /**
   * 根据置顶信息获取IM 消息
   *
   * @param topMessage 置顶信息
   */
  private void getTopStickyMessage(TopStickyMessage topMessage) {
    V2NIMMessageRefer refer =
        V2NIMMessageReferBuilder.builder()
            .withMessageClientId(topMessage.getIdClient())
            .withMessageServerId(topMessage.getIdServer())
            .withConversationId(topMessage.getTo())
            .withConversationType(topMessage.getConversationType())
            .withSenderId(topMessage.getFrom())
            .withReceiverId(topMessage.getReceiverId())
            .withCreateTime(topMessage.getTime())
            .build();
    ALog.d(LIB_TAG, TAG, "getTopStickyMessage:" + topMessage.getIdClient());
    List<V2NIMMessageRefer> refers = new ArrayList<>();
    refers.add(refer);
    ChatRepo.getMessageListByRefers(
        refers,
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getTopStickyMessage,onError:" + errorCode);
          }

          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> data) {
            ALog.d(LIB_TAG, TAG, "getTopStickyMessage,onSuccess:" + (data == null));
            if (data != null && data.size() > 0) {
              ChatUserCache.getInstance().setTopMessage(data.get(0));
              topMessageLiveData.postValue(data.get(0));
            } else {
              //请求失败移除置顶消息
              topMessageLiveData.postValue(null);
              ChatUserCache.getInstance().removeTopMessage();
            }
          }
        });
  }

  /** 更新我的群成员信息 */
  public void updateMyTeamMember(List<String> accIds) {
    if (accIds != null && !accIds.isEmpty()) {
      for (String account : accIds) {
        if (TextUtils.equals(account, IMKitClient.account())) {
          if (ChatUserCache.getInstance().getTopMessage() != null) {
            //更新置顶消息的权限
            topMessagePermissionLiveData.postValue("");
          }
          break;
        }
      }
    }
  }

  /**
   * 添加置顶消息
   *
   * @param message 消息
   */
  public void addStickyMessage(V2NIMMessage message) {
    try {
      //添加置顶消息的类容
      JSONObject jsonMessage = new JSONObject();
      jsonMessage.put(ChatConstants.KEY_STICKY_MESSAGE_CLIENT_ID, message.getMessageClientId());
      jsonMessage.put(ChatConstants.KEY_STICKY_MESSAGE_TIME, message.getCreateTime());
      jsonMessage.put(
          ChatConstants.KEY_STICKY_MESSAGE_SCENE, message.getConversationType().getValue());
      jsonMessage.put(ChatConstants.KEY_STICKY_MESSAGE_FROM, message.getSenderId());
      jsonMessage.put(ChatConstants.KEY_STICKY_MESSAGE_TO, message.getConversationId());
      jsonMessage.put(ChatConstants.KEY_STICKY_MESSAGE_SERVER_ID, message.getMessageServerId());
      jsonMessage.put(ChatConstants.KEY_STICKY_MESSAGE_OPERATOR, IMKitClient.account());
      jsonMessage.put(
          ChatConstants.KEY_STICKY_MESSAGE_OPERATION, ChatConstants.TYPE_EXTENSION_STICKY_ADD);
      jsonMessage.put(ChatConstants.KEY_STICKY_MESSAGE_RECEIVER_ID, message.getReceiverId());

      V2NIMTeam team = TeamUserManager.getInstance().getCurrentTeam();
      if (team != null) {
        String teamExtension = team.getServerExtension();
        JSONObject jsonTeam = new JSONObject();
        if (!TextUtils.isEmpty(teamExtension)) {
          jsonTeam = new JSONObject(teamExtension);
        }
        jsonTeam.put(ChatConstants.KEY_EXTENSION_STICKY, jsonMessage);
        jsonTeam.put(ChatConstants.KEY_EXTENSION_LAST_OPT_TYPE, ChatConstants.KEY_EXTENSION_STICKY);
        teamExtension = jsonTeam.toString();
        TeamRepo.updateTeamExtension(
            team.getTeamId(),
            V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
            teamExtension,
            new FetchCallback<Void>() {
              @Override
              public void onSuccess(@Nullable Void param) {
                ALog.d(LIB_TAG, TAG, "addStickyMessage,onSuccess");
              }

              @Override
              public void onError(int errorCode, @NonNull String errorMsg) {
                ALog.d(LIB_TAG, TAG, "addStickyMessage,onFailed:" + errorCode);
              }
            });
      }

    } catch (JSONException e) {
      ALog.e(LIB_TAG, TAG, "addStickyMessage json error:" + e);
    }
  }

  @Override
  protected void setSentMessageReadCount(IMMessageInfo message) {
    if (message.getMessage().getConversationType()
        == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM) {
      message.setReadCount(0);
      if (teamLiveData.getValue() != null) {
        message.setUnReadCount(teamLiveData.getValue().getMemberCount());
      } else {
        message.setUnReadCount(0);
      }
    }
  }

  /** 移除置顶消息 */
  public void removeStickyMessage() {
    try {
      //添加置顶消息的类容
      JSONObject jsonMessage = new JSONObject();
      IMMessageInfo topMessage = ChatUserCache.getInstance().getTopMessage();
      if (topMessage != null) {
        jsonMessage.put(
            ChatConstants.KEY_STICKY_MESSAGE_CLIENT_ID,
            topMessage.getMessage().getMessageClientId());
      }
      jsonMessage.put(ChatConstants.KEY_STICKY_MESSAGE_OPERATOR, IMKitClient.account());
      jsonMessage.put(
          ChatConstants.KEY_STICKY_MESSAGE_OPERATION, ChatConstants.TYPE_EXTENSION_STICKY_REMOVE);

      JSONObject jsonSticky = new JSONObject();

      jsonSticky.put(ChatConstants.KEY_EXTENSION_STICKY, jsonMessage);

      V2NIMTeam team = TeamUserManager.getInstance().getCurrentTeam();
      if (team != null) {
        String teamExtension = team.getServerExtension();
        JSONObject jsonTeam = new JSONObject();
        if (!TextUtils.isEmpty(teamExtension)) {
          jsonTeam = new JSONObject(teamExtension);
        }
        jsonTeam.put(ChatConstants.KEY_EXTENSION_STICKY, jsonMessage);
        jsonTeam.put(ChatConstants.KEY_EXTENSION_LAST_OPT_TYPE, ChatConstants.KEY_EXTENSION_STICKY);
        teamExtension = jsonTeam.toString();
        TeamRepo.updateTeamExtension(
            team.getTeamId(),
            V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
            teamExtension,
            new FetchCallback<Void>() {
              @Override
              public void onSuccess(@Nullable Void param) {
                ALog.d(LIB_TAG, TAG, "removeStickyMessage,onSuccess");
              }

              @Override
              public void onError(int errorCode, @NonNull String errorMsg) {
                ALog.d(LIB_TAG, TAG, "removeStickyMessage,onFailed:" + errorCode);
              }
            });
      }

    } catch (JSONException e) {
      ALog.e(LIB_TAG, TAG, "removeStickyMessage json error:" + e);
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    TeamUserManager.getInstance().clear();
  }
}
