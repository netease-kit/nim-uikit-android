// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginListener;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginClientChange;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus;
import com.netease.nimlib.sdk.v2.auth.model.V2NIMKickedOfflineDetail;
import com.netease.nimlib.sdk.v2.auth.model.V2NIMLoginClient;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMClearHistoryNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageDeletedNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessagePinNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageQuickCommentNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.sdk.v2.message.V2NIMP2PMessageReadReceipt;
import com.netease.nimlib.sdk.v2.message.V2NIMTeamMessageReadReceipt;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessagePinState;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.listener.ChatListener;
import com.netease.yunxin.kit.chatkit.listener.MessageRevokeNotification;
import com.netease.yunxin.kit.chatkit.listener.MessageUpdateType;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.MessagePinInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserChangedListener;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserManager;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.PinEvent;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.listener.ContactChangeType;
import com.netease.yunxin.kit.corekit.im2.listener.ContactListener;
import com.netease.yunxin.kit.corekit.im2.model.FriendAddApplicationInfo;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 标记消息ViewModel 提供标记信息查询、移除标记、转发等功能
 *
 * <p>
 */
public class ChatPinViewModel extends BaseViewModel {

  public static final String TAG = "ChatPinViewModel";
  // 单聊则为对方账号ID，群聊则为群ID
  protected String mSessionId;
  // 会话类型
  private V2NIMConversationType mSessionType;
  protected boolean needACK = true;
  protected boolean showRead = true;

  // 标记消息查询LiveData
  private final MutableLiveData<FetchResult<List<ChatMessageBean>>> messageLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ChatMessageBean>> messageFetchResult =
      new FetchResult<>(LoadStatus.Finish);

  // 移除标记LiveData
  private final MutableLiveData<FetchResult<String>> removePinLiveData = new MutableLiveData<>();
  private final FetchResult<String> removePinResult = new FetchResult<>(LoadStatus.Finish);

  // 添加标记LiveData
  private final MutableLiveData<FetchResult<List<ChatMessageBean>>> addLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ChatMessageBean>> addFetchResult =
      new FetchResult<>(LoadStatus.Finish);

  // 删除消息LiveData
  private final MutableLiveData<FetchResult<List<String>>> deleteMessageLiveData =
      new MutableLiveData<>();

  // 附件下载进度LiveData
  private final MutableLiveData<FetchResult<IMMessageProgress>> attachmentProgressLiveData =
      new MutableLiveData<>();

  //用户信息变更
  private final MutableLiveData<FetchResult<List<String>>> userChangeLiveData =
      new MutableLiveData<>();

  // 初始化
  public void init(String sessionId, V2NIMConversationType sessionType) {
    this.mSessionId = sessionId;
    this.mSessionType = sessionType;
    this.needACK = SettingRepo.getShowReadStatus();
    ChatRepo.addMessageListener(messageListener);
    if (sessionType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM) {
      TeamUserManager.getInstance().addMemberChangedListener(userInfoListener);
    } else {
      ContactRepo.addContactListener(contactListener);
    }
    IMKitClient.addLoginListener(loginListener);
  }

  public void setShowRead(boolean showRead) {
    this.showRead = showRead;
  }

  // 获取标记消息查询列表LiveData
  public MutableLiveData<FetchResult<List<ChatMessageBean>>> getMessageLiveData() {
    return messageLiveData;
  }

  // 获取移除标记LiveData
  public MutableLiveData<FetchResult<String>> getRemovePinLiveData() {
    return removePinLiveData;
  }

  // 获取添加标记LiveData
  public MutableLiveData<FetchResult<List<ChatMessageBean>>> getAddPinLiveData() {
    return addLiveData;
  }

  // 获取删除消息LiveData
  public MutableLiveData<FetchResult<List<String>>> getDeleteMessageLiveData() {
    return deleteMessageLiveData;
  }

  // 获取附件下载进度LiveData
  public MutableLiveData<FetchResult<IMMessageProgress>> getAttachmentProgressLiveData() {
    return attachmentProgressLiveData;
  }

  //获取用户信息变更LiveData
  public MutableLiveData<FetchResult<List<String>>> getUserChangeLiveData() {
    return userChangeLiveData;
  }

  /** 获取PIN消息 */
  public void getPinMessageList() {
    ChatRepo.getPinnedMessageList(
        V2NIMConversationIdUtil.conversationId(mSessionId, mSessionType),
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getPinMessage , onError:" + errorCode + "errorMsg:" + errorMsg);
            messageFetchResult.setLoadStatus(LoadStatus.Error);
            messageFetchResult.setError(new FetchResult.ErrorMsg(errorCode, errorMsg));
            messageLiveData.setValue(messageFetchResult);
          }

          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> data) {
            ALog.d(LIB_TAG, TAG, "getPinMessage , onSuccess:" + (data != null ? data.size() : 0));
            messageFetchResult.setLoadStatus(LoadStatus.Success);
            messageFetchResult.setData(MessageHelper.convertToChatMessageBean(data));
            messageLiveData.setValue(messageFetchResult);
          }
        });
  }

  // 移除标记消息
  public void removePin(IMMessageInfo messageInfo) {
    if (messageInfo.getPinOption() == null) {
      return;
    }
    ChatRepo.unpinMessage(
        messageInfo.getPinOption().getMessageRefer(),
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "removePin , onError:" + errorCode + "errorMsg:" + errorMsg);
            if (errorCode == ChatKitUIConstant.ERROR_CODE_NETWORK) {
              ToastX.showShortToast(R.string.chat_network_error_tip);
            }
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ALog.d(
                LIB_TAG,
                TAG,
                "removePin , onSuccess:" + messageInfo.getMessage().getMessageClientId());
            removePinResult.setLoadStatus(LoadStatus.Success);
            removePinResult.setData(messageInfo.getMessage().getMessageClientId());
            removePinLiveData.setValue(removePinResult);
            EventCenter.notifyEvent(
                new PinEvent(messageInfo.getMessage().getMessageClientId(), true));
            ToastX.showShortToast(R.string.chat_remove_tips);
          }
        });
  }

  // 转发PIN消息并发送文本消息
  public void sendForwardMessage(V2NIMMessage message, String inputMsg, String conversationId) {
    ALog.d(LIB_TAG, TAG, "sendForwardMessage:" + conversationId);
    MessageHelper.sendForwardMessage(
        new ChatMessageBean(new IMMessageInfo(message)),
        inputMsg,
        Collections.singletonList(conversationId),
        true,
        needACK && showRead);
  }

  private final V2NIMLoginListener loginListener =
      new V2NIMLoginListener() {
        @Override
        public void onLoginStatus(V2NIMLoginStatus status) {
          //断网重连，重新拉取数据
          if (status == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED) {
            getPinMessageList();
          }
        }

        @Override
        public void onLoginFailed(V2NIMError error) {
          // do nothing
        }

        @Override
        public void onKickedOffline(V2NIMKickedOfflineDetail detail) {
          // do nothing
        }

        @Override
        public void onLoginClientChanged(
            V2NIMLoginClientChange change, List<V2NIMLoginClient> clients) {
          // do nothing
        }
      };

  //群信息变更通知
  private final TeamUserChangedListener userInfoListener =
      new TeamUserChangedListener() {
        @Override
        public void onUsersChanged(List<String> accountIds) {
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Finish);
          result.setData(accountIds);
          result.setType(FetchResult.FetchType.Update);
          userChangeLiveData.setValue(result);
        }

        @Override
        public void onUserDelete(List<String> accountIds) {}

        @Override
        public void onUsersAdd(List<String> accountIds) {}
      };

  //单聊用户信息变更通知
  private final ContactListener contactListener =
      new ContactListener() {

        @Override
        public void onFriendAddRejected(@NonNull FriendAddApplicationInfo rejectionInfo) {}

        @Override
        public void onFriendAddApplication(@NonNull FriendAddApplicationInfo friendApplication) {}

        @Override
        public void onContactChange(
            @NonNull ContactChangeType changeType,
            @NonNull List<? extends UserWithFriend> contactList) {
          if (changeType == ContactChangeType.Update || changeType == ContactChangeType.AddFriend) {
            List<String> accounts = new ArrayList<>();
            for (UserWithFriend userInfo : contactList) {
              accounts.add(userInfo.getAccount());
            }
            FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success);
            result.setData(accounts);
            userChangeLiveData.setValue(result);
          }
        }
      };

  // 消息监听
  private final ChatListener messageListener =
      new ChatListener() {
        @Override
        public void onReceiveMessagesModified(@Nullable List<V2NIMMessage> messages) {
          ALog.d(LIB_TAG, TAG, "onReceiveMessagesModified -->> ");
        }

        @Override
        public void onSendMessageFailed(
            int errorCode,
            @NonNull String errorMsg,
            @NonNull String conversationId,
            @NonNull V2NIMConversationType conversationType,
            @Nullable V2NIMMessage data) {}

        @Override
        public void onSendMessage(@NonNull V2NIMMessage message) {
          ALog.d(LIB_TAG, TAG, "onSendMessage -->> ");
        }

        @Override
        public void onMessageAttachmentDownloadProgress(
            @NonNull V2NIMMessage message, int progress) {
          ALog.d(LIB_TAG, TAG, "onMessageAttachmentDownloadProgress -->> " + progress);
          FetchResult<IMMessageProgress> result = new FetchResult<>(LoadStatus.Success);
          IMMessageProgress attachmentProgress =
              new IMMessageProgress(message.getMessageClientId(), progress);
          result.setData(attachmentProgress);
          result.setType(FetchResult.FetchType.Update);
          result.setTypeIndex(-1);
          attachmentProgressLiveData.setValue(result);
        }

        @Override
        public void onMessagesUpdate(
            @NonNull List<IMMessageInfo> messages, @NonNull MessageUpdateType type) {
          ALog.d(LIB_TAG, TAG, "onMessagesUpdate -->> ");
          //do nothing
        }

        @Override
        public void onClearHistoryNotifications(
            @Nullable List<? extends V2NIMClearHistoryNotification> clearHistoryNotifications) {
          ALog.d(LIB_TAG, TAG, "messageListener , clear history -->> ");
        }

        @Override
        public void onMessageDeletedNotifications(
            @NonNull List<? extends V2NIMMessageDeletedNotification> messages) {
          ALog.d(LIB_TAG, TAG, "msg delete -->> " + messages.size());
          List<String> clientIdList = new ArrayList<>();
          for (V2NIMMessageDeletedNotification message : messages) {
            if (isSameConversation(message.getMessageRefer().getConversationId())) {
              clientIdList.add(message.getMessageRefer().getMessageClientId());
            }
          }
          removeMessagesByClientId(clientIdList);
        }

        @Override
        public void onMessageQuickCommentNotification(
            @Nullable V2NIMMessageQuickCommentNotification quickCommentNotification) {}

        @Override
        public void onMessagePinNotification(
            @Nullable V2NIMMessagePinNotification pinNotification) {
          ALog.d(LIB_TAG, TAG, "pinMessage -->> ");
          if (pinNotification != null) {
            if (pinNotification.getPinState()
                == V2NIMMessagePinState.V2NIM_MESSAGE_PIN_STEATE_PINNED) {
              fillPinMessage(pinNotification);
            } else if (pinNotification.getPinState()
                == V2NIMMessagePinState.V2NIM_MESSAGE_PIN_STEATE_NOT_PINNED) {
              if (isSameConversation(
                  pinNotification.getPin().getMessageRefer().getConversationId())) {
                ALog.d(
                    LIB_TAG,
                    TAG,
                    "pinMessageRemove:"
                        + pinNotification.getPin().getOperatorId()
                        + "sessionID:"
                        + mSessionId);
                String uuid = pinNotification.getPin().getMessageRefer().getMessageClientId();
                if (!TextUtils.isEmpty(uuid)) {
                  removePinResult.setData(uuid);
                  removePinLiveData.setValue(removePinResult);
                }
              }
            }
          }
        }

        @Override
        public void onMessageRevokeNotifications(
            @Nullable List<MessageRevokeNotification> revokeNotifications) {
          ALog.d(LIB_TAG, TAG, "onMessageRevokeNotifications -->> ");
          if (revokeNotifications == null) {
            return;
          }
          List<String> clientIdList = new ArrayList<>();
          for (MessageRevokeNotification notify : revokeNotifications) {
            if (isSameConversation(
                notify.getNimNotification().getMessageRefer().getConversationId())) {
              clientIdList.add(notify.getNimNotification().getMessageRefer().getMessageClientId());
            }
          }
          removeMessagesByClientId(clientIdList);
        }

        @Override
        public void onReceiveTeamMessageReadReceipts(
            @Nullable List<? extends V2NIMTeamMessageReadReceipt> readReceipts) {
          ALog.d(LIB_TAG, TAG, "onReceiveTeamMessageReadReceipts -->> ");
        }

        @Override
        public void onReceiveP2PMessageReadReceipts(
            @Nullable List<? extends V2NIMP2PMessageReadReceipt> readReceipts) {
          ALog.d(LIB_TAG, TAG, "onReceiveP2PMessageReadReceipts -->> ");
        }

        @Override
        public void onReceiveMessages(@NonNull List<IMMessageInfo> messages) {
          ALog.d(LIB_TAG, TAG, "onReceiveMessages -->> ");
        }
      };

  /**
   * 根据客户端消息ID移除消息
   *
   * @param messages 消息列表
   */
  private void removeMessagesByClientId(List<String> messages) {
    if (messages.size() > 0) {
      FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success);
      result.setType(FetchResult.FetchType.Remove);
      result.setData(messages);
      deleteMessageLiveData.setValue(result);
    }
  }

  /**
   * 判断是否在同一个会话
   *
   * @param conversationId 会话ID
   * @return 是否在同一个会话
   */
  private boolean isSameConversation(String conversationId) {
    return TextUtils.equals(
            V2NIMConversationIdUtil.conversationTargetId(conversationId), mSessionId)
        && V2NIMConversationIdUtil.conversationType(conversationId) == mSessionType;
  }

  // 填充Pin消息
  private void fillPinMessage(V2NIMMessagePinNotification notification) {
    if (notification != null && notification.getPin() != null) {
      List<V2NIMMessageRefer> refers = new ArrayList<>();
      refers.add(notification.getPin().getMessageRefer());
      ChatRepo.getMessageListByRefers(
          refers,
          new FetchCallback<List<IMMessageInfo>>() {

            @Override
            public void onSuccess(@Nullable List<IMMessageInfo> data) {
              ALog.d(LIB_TAG, TAG, "fillPinMessage , onSuccess:");
              addFetchResult.setLoadStatus(LoadStatus.Success);
              if (data != null && data.size() > 0) {
                data.get(0).setPinOption(new MessagePinInfo(notification.getPin()));
              }
              addFetchResult.setData(MessageHelper.convertToChatMessageBean(data));
              addLiveData.setValue(addFetchResult);
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.d(
                  LIB_TAG, TAG, "fillPinMessage , onError:" + errorCode + "errorMsg:" + errorMsg);
            }
          });
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ChatRepo.removeMessageListener(messageListener);
    ContactRepo.removeContactListener(contactListener);
    TeamUserManager.getInstance().removeMemberChangedListener(userInfoListener);
    IMKitClient.removeLoginListener(loginListener);
  }
}
