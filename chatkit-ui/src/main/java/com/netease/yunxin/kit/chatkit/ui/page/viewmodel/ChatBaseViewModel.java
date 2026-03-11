// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.ai.enums.V2NIMAIModelRoleType;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMAIModelCallMessage;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMClearHistoryNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMCollection;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageCreator;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageDeletedNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessagePin;
import com.netease.nimlib.sdk.v2.message.V2NIMMessagePinNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageQuickCommentNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.sdk.v2.message.V2NIMP2PMessageReadReceipt;
import com.netease.nimlib.sdk.v2.message.V2NIMTeamMessageReadReceipt;
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessageAIConfig;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageAIRegenOpType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageAIStreamStopOpType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessagePinState;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageQueryDirection;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageSendingState;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.sdk.v2.message.option.V2NIMMessageListOption;
import com.netease.nimlib.sdk.v2.message.params.V2NIMAddCollectionParams;
import com.netease.nimlib.sdk.v2.message.params.V2NIMMessageAIRegenParams;
import com.netease.nimlib.sdk.v2.message.params.V2NIMMessageAIStreamStopParams;
import com.netease.nimlib.sdk.v2.message.params.V2NIMSendMessageParams;
import com.netease.nimlib.sdk.v2.message.result.V2NIMSendMessageResult;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.listener.ChatListener;
import com.netease.yunxin.kit.chatkit.listener.MessageRevokeNotification;
import com.netease.yunxin.kit.chatkit.listener.MessageUpdateType;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.map.ChatLocationBean;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageCreator;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.MessageOperateUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageParamBuildUtils;
import com.netease.yunxin.kit.chatkit.ui.model.AnchorScrollInfo;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.MessageRevokeInfo;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitService;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.chatkit.utils.SendMediaHelper;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.extend.ProgressFetchCallback;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.im2.provider.V2MessageProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.json.JSONObject;

/** 消息ViewModel 基类 消息接受、发送、撤回等逻辑 用户、好友信息变更监听等 */
public abstract class ChatBaseViewModel extends BaseViewModel {
  public static final String TAG = "ChatViewModel";

  // 撤回消息超时时间
  private static final int RES_REVOKE_TIMEOUT = 107314;

  // 收藏消息类型从1000开始
  private static final int COLLECTION_TYPE = 1000;

  //自己手动撤回的消息的messageClientId，
  //用于判断是否是自己撤回的消息
  // 自己撤回的消息，收到通知后不再做处理
  private String revokedMessageClientId;

  // 拉取历史消息
  private final MutableLiveData<FetchResult<List<ChatMessageBean>>> messageLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ChatMessageBean>> messageFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  // 接受消息
  private final MutableLiveData<FetchResult<List<ChatMessageBean>>> messageRecLiveData =
      new MutableLiveData<>();

  // 用户信息变更，只有点击到用户个人信息页面才会远端拉取，如果有变更本地同步刷新
  protected final MutableLiveData<FetchResult<List<String>>> userChangeLiveData =
      new MutableLiveData<>();

  // 标记消息LiveData
  private final MutableLiveData<FetchResult<Map<String, V2NIMMessagePin>>> msgPinLiveData =
      new MutableLiveData<>();
  // 消息发送LiveData，本地发送消息通过该LiveData通知UI
  private final MutableLiveData<FetchResult<ChatMessageBean>> sendMessageLiveData =
      new MutableLiveData<>();
  private final FetchResult<ChatMessageBean> sendMessageFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  // 消息附件下载进度LiveData
  private final MutableLiveData<FetchResult<IMMessageProgress>> attachmentProgressMutableLiveData =
      new MutableLiveData<>();
  // 撤回消息LiveData
  private final MutableLiveData<FetchResult<List<MessageRevokeInfo>>> revokeMessageLiveData =
      new MutableLiveData<>();
  // 删除消息LiveData
  private final MutableLiveData<FetchResult<List<V2NIMMessageRefer>>> deleteMessageLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>>>
      updateMessageLiveData = new MutableLiveData<>();

  private final MutableLiveData<FetchResult<List<V2NIMMessagePin>>> pinedMessageListLiveData =
      new MutableLiveData<>();

  // 当前会话账号ID，单聊则为对方账号，群聊则为群ID
  protected String mChatAccountId;
  // 当前会话ID
  protected String mConversationId;
  // 当前会话类型
  private V2NIMConversationType mSessionType;
  // 是否群聊
  protected boolean mIsTeamGroup = false;
  // 是否显示已读状态
  protected boolean showRead = true;
  // 是否有加载更多消息
  protected boolean hasLoadMessage = false;

  // 消息分页大小
  private final int messagePageSize = 100;
  // 视频图片旋转角度，适配部分机型发送图片旋转问题
  private final String Orientation_Vertical = "90";

  /**
   * 设置已发送消息的已读状态
   *
   * @param message 已发送消息
   */
  protected void setSentMessageReadCount(IMMessageInfo message) {}

  public void onSentMessagePrepare(IMMessageInfo message) {
    setSentMessageReadCount(message);
  }

  // 消息监听
  private final ChatListener messageListener =
      new ChatListener() {

        @Override
        public void onReceiveMessagesModified(@Nullable List<V2NIMMessage> messages) {
          ALog.i(LIB_TAG, TAG, "onReceiveMessagesModified msg");
          List<ChatMessageBean> messageList = new ArrayList<>();
          for (V2NIMMessage msg : messages) {
            ALog.i(
                LIB_TAG,
                TAG,
                "onReceiveMessagesModified msg:"
                    + msg.getMessageClientId()
                    + " "
                    + ",content: "
                    + msg.getText());
            ChatMessageBean msgBean = new ChatMessageBean(new IMMessageInfo(msg));
            if (msgBean.getMessage().getMessageType()
                == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
              msgBean.getMessageData().parseAttachment();
            }
            messageList.add(msgBean);
          }
          FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> messageUpdateResult =
              new FetchResult<>(LoadStatus.Success);
          messageUpdateResult.setData(new Pair<>(MessageUpdateType.UpdateMessage, messageList));
          messageUpdateResult.setType(FetchResult.FetchType.Update);
          messageUpdateResult.setTypeIndex(-1);
          updateMessageLiveData.setValue(messageUpdateResult);
        }

        @Override
        public void onSendMessageFailed(
            int errorCode,
            @NonNull String errorMsg,
            @NonNull String conversationId,
            @NonNull V2NIMConversationType conversationType,
            @Nullable V2NIMMessage data) {
          if (errorCode == ChatKitUIConstant.ERROR_CODE_IN_BLACK_LIST) {
            //保存本地黑名单消息
            MessageHelper.saveLocalBlackTipMessage(conversationId, IMKitClient.account());
          }
        }

        @Override
        public void onSendMessage(@NonNull V2NIMMessage message) {
          if (message.getConversationId().equals(mConversationId)) {
            ALog.i(LIB_TAG, TAG, "onSendMessage -->> " + message.getMessageClientId());
            IMMessageInfo messageInfo = new IMMessageInfo(message);
            setSentMessageReadCount(messageInfo);
            boolean isSending =
                message.getSendingState()
                    == V2NIMMessageSendingState.V2NIM_MESSAGE_SENDING_STATE_SENDING;
            postMessageSend(messageInfo, isSending);
          }
        }

        // 消息附件下载进度
        @Override
        public void onMessageAttachmentDownloadProgress(
            @NonNull V2NIMMessage message, int progress) {
          if (message.getConversationId() != null
              && message.getConversationId().equals(mConversationId)) {
            ALog.i(LIB_TAG, TAG, "onMessageAttachmentDownloadProgress -->> " + progress);
            FetchResult<IMMessageProgress> result = new FetchResult<>(LoadStatus.Success);
            result.setData(new IMMessageProgress(message.getMessageClientId(), progress));
            result.setType(FetchResult.FetchType.Update);
            result.setTypeIndex(-1);
            attachmentProgressMutableLiveData.setValue(result);
          }
        }

        // 消息发送状态变更
        @Override
        public void onMessagesUpdate(
            @NonNull List<IMMessageInfo> messages, @NonNull MessageUpdateType type) {
          if (messages.isEmpty()) {
            return;
          }
          IMMessageInfo firstMessage = messages.get(0);
          if (!firstMessage.getMessage().getConversationId().equals(mConversationId)) {
            return;
          }
          ALog.i(LIB_TAG, TAG, "onMessagesUpdate -->> " + messages.size() + ", type:" + type);
          FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> messageUpdateResult =
              new FetchResult<>(LoadStatus.Success);
          messageUpdateResult.setData(
              new Pair<>(type, MessageParamBuildUtils.convertToChatBeans(messages)));
          messageUpdateResult.setType(FetchResult.FetchType.Update);
          messageUpdateResult.setTypeIndex(-1);
          updateMessageLiveData.setValue(messageUpdateResult);
        }

        // 接收到新消息
        @Override
        public void onReceiveMessages(@NonNull List<IMMessageInfo> messages) {
          IMMessageInfo firstMessage = messages.get(0);
          if (!firstMessage.getMessage().getConversationId().equals(mConversationId)) {
            return;
          }
          ALog.i(LIB_TAG, TAG, "receive msg -->> " + messages.size());
          FetchResult<List<ChatMessageBean>> messageRecFetchResult =
              new FetchResult<>(LoadStatus.Success);
          messageRecFetchResult.setData(MessageParamBuildUtils.convertToChatBeans(messages));
          messageRecFetchResult.setType(FetchResult.FetchType.Add);
          messageRecFetchResult.setTypeIndex(-1);
          messageRecLiveData.setValue(messageRecFetchResult);
        }

        @Override
        public void onClearHistoryNotifications(
            @Nullable List<? extends V2NIMClearHistoryNotification> clearHistoryNotifications) {}

        @Override
        public void onMessageDeletedNotifications(
            @Nullable List<? extends V2NIMMessageDeletedNotification> messages) {
          ALog.i(
              LIB_TAG,
              TAG,
              "msg delete batch -->> " + (messages == null ? "null" : messages.size()));
          if (messages != null) {
            ArrayList<V2NIMMessageRefer> deleteList = new ArrayList<>();
            FetchResult<List<V2NIMMessageRefer>> result = new FetchResult<>(LoadStatus.Success);
            for (V2NIMMessageDeletedNotification msg : messages) {
              if (TextUtils.equals(msg.getMessageRefer().getConversationId(), mConversationId)) {
                deleteList.add(msg.getMessageRefer());
              }
            }
            if (deleteList.size() > 0) {
              result.setData(deleteList);
              result.setType(FetchResult.FetchType.Remove);
              result.setTypeIndex(-1);
              deleteMessageLiveData.setValue(result);
            }
          }
        }

        @Override
        public void onMessageQuickCommentNotification(
            @Nullable V2NIMMessageQuickCommentNotification quickCommentNotification) {}

        @Override
        public void onMessagePinNotification(
            @Nullable V2NIMMessagePinNotification pinNotification) {
          ALog.i(LIB_TAG, TAG, "onMessagePinNotification");
          if (pinNotification != null
              && Objects.equals(
                  pinNotification.getPin().getMessageRefer().getConversationId(),
                  mConversationId)) {
            ALog.i(
                LIB_TAG, TAG, "onMessagePinNotification:" + pinNotification.getPinState().name());
            if (pinNotification.getPinState()
                == V2NIMMessagePinState.V2NIM_MESSAGE_PIN_STEATE_PINNED) {
              Pair<String, V2NIMMessagePin> pinInfo =
                  new Pair<>(
                      pinNotification.getPin().getMessageRefer().getMessageClientId(),
                      pinNotification.getPin());
              addPinMessageLiveData.setValue(pinInfo);
            } else if (pinNotification.getPinState()
                == V2NIMMessagePinState.V2NIM_MESSAGE_PIN_STEATE_NOT_PINNED) {
              removePinMessageLiveData.setValue(
                  pinNotification.getPin().getMessageRefer().getMessageClientId());
            }
          }
        }

        @Override
        public void onMessageRevokeNotifications(
            @Nullable List<MessageRevokeNotification> revokeNotifications) {
          if (revokeNotifications == null) {
            return;
          }
          FetchResult<List<MessageRevokeInfo>> result = new FetchResult<>(LoadStatus.Success);
          List<MessageRevokeInfo> revokedList = new ArrayList<>();
          for (MessageRevokeNotification revokeNotification : revokeNotifications) {
            //判断不是自己撤回的，并且是当前会话的消息
            if (!TextUtils.equals(
                    revokedMessageClientId,
                    revokeNotification.getNimNotification().getMessageRefer().getMessageClientId())
                && TextUtils.equals(
                    revokeNotification.getNimNotification().getMessageRefer().getConversationId(),
                    mConversationId)) {
              revokedList.add(new MessageRevokeInfo(null, revokeNotification.getNimNotification()));
            }
          }
          result.setData(revokedList);
          result.setType(FetchResult.FetchType.Remove);
          result.setTypeIndex(-1);
          revokeMessageLiveData.setValue(result);
        }

        @Override
        public void onReceiveTeamMessageReadReceipts(
            @Nullable List<? extends V2NIMTeamMessageReadReceipt> readReceipts) {
          if (readReceipts == null) {
            return;
          }
          List<V2NIMTeamMessageReadReceipt> teamReceipts = new ArrayList<>(readReceipts);
          onTeamMessageReadReceipts(teamReceipts);
        }

        @Override
        public void onReceiveP2PMessageReadReceipts(
            @Nullable List<? extends V2NIMP2PMessageReadReceipt> readReceipts) {
          if (readReceipts == null) {
            return;
          }
          List<V2NIMP2PMessageReadReceipt> p2pReceipts = new ArrayList<>(readReceipts);
          onP2PMessageReadReceipts(p2pReceipts);
        }
      };

  //群消息已读回执处理
  protected void onTeamMessageReadReceipts(List<V2NIMTeamMessageReadReceipt> readReceipts) {}

  //点对点消息处理已读回执
  protected void onP2PMessageReadReceipts(List<V2NIMP2PMessageReadReceipt> readReceipts) {}

  // 获取撤回消息LiveData
  public MutableLiveData<FetchResult<List<MessageRevokeInfo>>> getRevokeMessageLiveData() {
    return revokeMessageLiveData;
  }

  // 获取查询消息LiveData
  public MutableLiveData<FetchResult<List<ChatMessageBean>>> getQueryMessageLiveData() {
    return messageLiveData;
  }
  // 获取接收消息LiveData
  public MutableLiveData<FetchResult<List<ChatMessageBean>>> getRecMessageLiveData() {
    return messageRecLiveData;
  }

  // 获取用户信息变更LiveData
  public MutableLiveData<FetchResult<List<String>>> getUserChangeLiveData() {
    return userChangeLiveData;
  }

  // 获取消息更新LiveData
  public MutableLiveData<FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>>>
      getUpdateMessageLiveData() {
    return updateMessageLiveData;
  }

  // 获取pin 消息列表LiveData
  public MutableLiveData<FetchResult<List<V2NIMMessagePin>>> getPinedMessageListLiveData() {
    return pinedMessageListLiveData;
  }

  // 获取标记消息LiveData
  public MutableLiveData<FetchResult<Map<String, V2NIMMessagePin>>> getMsgPinLiveData() {
    return msgPinLiveData;
  }

  // 获取删除消息LiveData
  public MutableLiveData<FetchResult<List<V2NIMMessageRefer>>> getDeleteMessageLiveData() {
    return deleteMessageLiveData;
  }

  /** 获取PIN消息列表 */
  public void getPinedMessageList() {
    V2MessageProvider.getPinMessageList(
        mConversationId,
        new FetchCallback<List<V2NIMMessagePin>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.i(
                LIB_TAG,
                TAG,
                "getPinedMessageList,onFailed:" + errorCode + " errorMsg:" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable List<V2NIMMessagePin> data) {
            ALog.i(
                LIB_TAG,
                TAG,
                "getPinedMessageList,onSuccess:" + (data == null ? "0" : data.size()));
            if (data != null && !data.isEmpty()) {
              FetchResult<List<V2NIMMessagePin>> result = new FetchResult<>(LoadStatus.Success);
              result.setData(data);
              pinedMessageListLiveData.setValue(result);
            }
          }
        });
  }

  /**
   * 批量删除消息，一次最多50条
   *
   * @param messageList 消息列表
   */
  public void deleteMessage(List<ChatMessageBean> messageList) {
    MessageOperateUtils.deleteMessages(messageList, deleteMessageLiveData);
  }

  // 好友变更通知，子类根据自己需要重写
  public void notifyFriendChange(UserWithFriend friend) {}
  // 撤回消息
  public void revokeMessage(ChatMessageBean messageBean) {
    if (messageBean != null && messageBean.getMessageData() != null) {
      revokedMessageClientId = messageBean.getMsgClientId();
      ChatRepo.revokeMessage(
          messageBean.getMessage(),
          null,
          new FetchCallback<Void>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              FetchResult<List<MessageRevokeInfo>> fetchResult =
                  new FetchResult<>(LoadStatus.Error);
              fetchResult.setError(
                  errorCode,
                  errorCode == RES_REVOKE_TIMEOUT
                      ? R.string.chat_message_revoke_over_time
                      : R.string.chat_message_revoke_error);
              revokeMessageLiveData.setValue(fetchResult);
              ALog.i(LIB_TAG, TAG, "revokeMessage,onFailed:" + errorCode + " errorMsg:" + errorMsg);
            }

            @Override
            public void onSuccess(@Nullable Void data) {
              if (!TextUtils.isEmpty(messageBean.getPinAccid())) {
                ChatRepo.unpinMessage(messageBean.getMessage(), null);
              }

              FetchResult<List<MessageRevokeInfo>> fetchResult =
                  new FetchResult<>(LoadStatus.Success);
              MessageRevokeInfo messageRevokeInfo =
                  new MessageRevokeInfo(messageBean.getMessage(), null);
              fetchResult.setData(Collections.singletonList(messageRevokeInfo));
              revokeMessageLiveData.setValue(fetchResult);

              ALog.i(LIB_TAG, TAG, "revokeMessage, onSuccess");
            }
          });
    }
  }

  //语音转文字
  public void voiceToText(ChatMessageBean messageBean) {
    MessageOperateUtils.voiceToText(messageBean, updateMessageLiveData);
  }

  // 获取发送消息LiveData
  public MutableLiveData<FetchResult<ChatMessageBean>> getSendMessageLiveData() {
    return sendMessageLiveData;
  }

  // 获取消息附件下载进度LiveData
  public MutableLiveData<FetchResult<IMMessageProgress>> getAttachmentProgressMutableLiveData() {
    return attachmentProgressMutableLiveData;
  }

  // 初始化，缓存清理、已读未读开关获取
  public void init(String accountId, V2NIMConversationType sessionType) {
    this.mChatAccountId = accountId;
    this.mConversationId = V2NIMConversationIdUtil.conversationId(accountId, sessionType);
    this.mSessionType = sessionType;
    ALog.i(
        LIB_TAG,
        TAG,
        "init accountId:"
            + accountId
            + " sessionType:"
            + sessionType
            + " conversationId:"
            + mConversationId);
    ChatUserCache.getInstance().clear();
  }

  // 设置当前会话账号，清理未读数
  public void setChattingAccount() {
    ALog.i(LIB_TAG, TAG, "setChattingAccount sessionId:" + mConversationId);
    if (!TextUtils.isEmpty(mConversationId)) {
      ChatRepo.setCurrentConversationId(mConversationId);
      AitService.getInstance().clearAitInfo(mConversationId);
    }
  }

  // 设置是否群聊
  public void setTeamGroup(boolean group) {
    mIsTeamGroup = group;
  }

  // 获取会话ID
  public String getConversationId() {
    return mConversationId;
  }

  // 是否展示已读未读状态
  public void setShowReadStatus(boolean show) {
    showRead = show;
  }

  // 清理
  public void clearChattingAccount() {
    ChatRepo.clearCurrentConversationId();
  }

  // 注册监听
  public void addListener() {
    ALog.i(LIB_TAG, TAG, "registerObservers ");
    ChatRepo.addMessageListener(messageListener);
  }

  // 移除监听
  public void removeListener() {
    ALog.i(LIB_TAG, TAG, "unregisterObservers ");
    ChatUserCache.getInstance().clear();
    ChatRepo.removeMessageListener(messageListener);
  }

  // 发送文本消息
  public void sendTextMessage(
      String content, List<String> pushList, Map<String, Object> remoteExtension) {
    ALog.i(LIB_TAG, TAG, "sendTextMessage:" + (content != null ? content.length() : "null"));
    V2NIMMessage textMessage = V2NIMMessageCreator.createTextMessage(content);
    sendMessage(textMessage, pushList, remoteExtension);
  }

  /**
   * 发送文本消息
   *
   * @param content 文本内容
   * @param pushList push 列表
   * @param remoteExtension 扩展字段
   * @param aiUser AI 用户
   * @param aiMessages AI消息上下文
   */
  public void sendTextMessage(
      String content,
      List<String> pushList,
      Map<String, Object> remoteExtension,
      V2NIMAIUser aiUser,
      List<V2NIMAIModelCallMessage> aiMessages) {
    ALog.i(LIB_TAG, TAG, "sendTextMessage:" + (content != null ? content.length() : "null"));
    V2NIMMessage textMessage = V2NIMMessageCreator.createTextMessage(content);
    sendMessage(textMessage, pushList, remoteExtension, aiUser, aiMessages);
  }

  // 添加收藏
  public void addMsgCollection(String conversationName, IMMessageInfo messageInfo) {
    if (messageInfo == null) {
      return;
    }
    ALog.i(LIB_TAG, TAG, "addMsgCollection:" + messageInfo.getMessage().getMessageClientId());
    V2NIMAddCollectionParams params =
        MessageHelper.createCollectionParams(conversationName, messageInfo.getMessage());
    ChatRepo.addCollection(
        params,
        new FetchCallback<V2NIMCollection>() {

          @Override
          public void onSuccess(@Nullable V2NIMCollection data) {
            ToastX.showShortToast(R.string.chat_message_collection_tip);
          }

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ErrorUtils.showErrorCodeToast(IMKitClient.getApplicationContext(), errorCode);
          }
        });
  }

  // 发送语音消息
  public void sendAudioMessage(File audio, int audioLength) {
    if (audio != null) {
      ALog.i(LIB_TAG, TAG, "sendAudioMessage:" + audio.getPath());
      V2NIMMessage audioMessage =
          V2NIMMessageCreator.createAudioMessage(
              audio.getPath(), audio.getName(), null, audioLength);
      sendMessage(audioMessage, null, null);
    }
  }

  // 发送图片消息,参数为图片文件地址path
  public void sendImageMessage(String imagePath, String fileName, int width, int height) {
    File imageFile = MessageOperateUtils.checkImageFile(imagePath);
    if (imageFile != null) {
      ALog.i(LIB_TAG, TAG, "sendImageMessage:" + imagePath);
      V2NIMMessage imageMessage =
          MessageParamBuildUtils.createImageMessage(imageFile, fileName, width, height);
      sendMessage(imageMessage, null, null);
    }
  }

  // 发送自定义消息
  public void sendCustomMessage(Map<String, Object> attachment, String content) {
    if (attachment != null) {
      ALog.i(LIB_TAG, TAG, "sendCustomMessage:" + attachment.getClass().getName());
      String attachStr = new JSONObject(attachment).toString();
      V2NIMMessage customMsg = V2NIMMessageCreator.createCustomMessage(content, attachStr);
      sendMessage(customMsg, null, null);
    }
  }

  // 发送转发消息(单条转发)
  public void sendForwardMessage(
      ChatMessageBean message, String inputMsg, List<String> conversationIds) {
    ALog.i(LIB_TAG, TAG, "sendForwardMessage:" + conversationIds.size());
    MessageHelper.sendForwardMessage(message, inputMsg, conversationIds, false, showRead);
  }
  // 发送转发消息（逐条转发）
  public void sendForwardMessages(
      String inputMsg, List<String> conversationIds, List<ChatMessageBean> messages) {
    MessageHelper.sendForwardMessages(inputMsg, conversationIds, messages, false, showRead);
  }

  // 发送合并转发消息
  public void sendMultiForwardMessage(
      String displayName,
      String inputMsg,
      List<String> conversationIds,
      List<ChatMessageBean> messages) {
    MessageOperateUtils.sendMultiForwardMessage(
        displayName, inputMsg, mChatAccountId, conversationIds, messages, showRead);
  }

  // 发送位置消息
  public void sendLocationMessage(ChatLocationBean locationBean) {
    ALog.i(LIB_TAG, TAG, "sendLocationMessage:" + locationBean);
    V2NIMMessage locationMsg =
        V2NIMMessageCreator.createLocationMessage(
            locationBean.getLat(), locationBean.getLng(), locationBean.getAddress());
    locationMsg.setText(locationBean.getTitle());
    sendMessage(locationMsg, null, null);
  }

  // 发送视频消息
  public void sendVideoMessage(
      String videoFile, int duration, int width, int height, String displayName) {
    if (videoFile != null) {
      ALog.i(LIB_TAG, TAG, "sendVideoMessage:" + videoFile);
      File videoFileObj = new File(videoFile);
      if (!videoFileObj.exists()) {
        ALog.e(LIB_TAG, TAG, "sendVideoMessage videoFile not exist:" + videoFile);
        return;
      }
      V2NIMMessage msg =
          V2NIMMessageCreator.createVideoMessage(
              videoFile, displayName, null, duration, width, height);
      sendMessage(msg, null, null);
    }
  }

  // 发送文件消息
  public void sendFileMessage(File docsFile, String displayName) {
    if (docsFile != null) {
      ALog.i(LIB_TAG, TAG, "sendFileMessage:" + docsFile.getPath());
      if (TextUtils.isEmpty(displayName)) {
        displayName = docsFile.getName();
      }
      V2NIMMessage msg =
          V2NIMMessageCreator.createFileMessage(docsFile.getPath(), displayName, null);
      msg.setText(displayName);
      sendMessage(msg, null, null);
    }
  }

  public void sendImageOrVideoMessage(Uri uri, Context context) {
    V2NIMMessage msg = MessageOperateUtils.processUriAndSend(uri, context);
    if (msg != null) {
      sendMessage(msg, null, null);
    }
  }

  // 发送文件消息
  public void sendFile(Uri uri) {
    ALog.i(LIB_TAG, TAG, "sendFile:" + (uri != null ? uri.getPath() : "uri is null"));
    if (uri == null) {
      return;
    }
    SendMediaHelper.handleFile(
        uri,
        file -> {
          try {
            String displayName = ChatUtils.getUrlFileName(IMKitClient.getApplicationContext(), uri);
            sendFileMessage(file, displayName);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

  // 同步发送消息
  private void postMessageSend(IMMessageInfo message, boolean sending) {
    ALog.i(LIB_TAG, TAG, "postMessageSend:" + sending);
    sendMessageFetchResult.setLoadStatus(LoadStatus.Success);
    if (!sending) {
      sendMessageFetchResult.setType(FetchResult.FetchType.Update);
    } else {
      sendMessageFetchResult.setType(FetchResult.FetchType.Add);
    }
    if (message.getMessage().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      message.parseAttachment();
    }
    sendMessageFetchResult.setData(new ChatMessageBean(message));
    sendMessageLiveData.setValue(sendMessageFetchResult);
  }

  // 发送消息
  public void sendMessage(
      V2NIMMessage message, List<String> pushList, Map<String, Object> remoteExtension) {
    sendMessage(message, pushList, remoteExtension, null, null);
  }

  /**
   * 发送消息
   *
   * @param message 消息
   * @param pushList 推送列表
   * @param remoteExtension 扩展字段
   * @param aiUser AI用户
   * @param aiMessages AI消息上下文
   */
  public void sendMessage(
      V2NIMMessage message,
      List<String> pushList,
      Map<String, Object> remoteExtension,
      V2NIMAIUser aiUser,
      List<V2NIMAIModelCallMessage> aiMessages) {
    String remoteStr = MessageParamBuildUtils.toJson(remoteExtension);
    sendMessageStrExtension(message, mConversationId, pushList, remoteStr, aiUser, aiMessages);
  }

  public void resendMessage(V2NIMMessage message, V2NIMMessage replyMessage) {

    if (replyMessage != null) {
      // 回复消息，被回复消息在当前的消息列表中
      replyMessage(message, replyMessage, null, null, null);
    } else if (message.getThreadReply() != null) {
      // 回复消息但是消息不在当前列表中
      List<V2NIMMessageRefer> messageReferList = new ArrayList<>();
      messageReferList.add(message.getThreadReply());
      ChatRepo.getMessageListByRefers(
          messageReferList,
          new FetchCallback<List<IMMessageInfo>>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.i(LIB_TAG, TAG, "resendMessage replyMessage onError -->> " + errorCode);
              sendMessageStrExtension(message, mConversationId, null, null);
            }

            @Override
            public void onSuccess(@Nullable List<IMMessageInfo> data) {
              if (data != null && data.size() > 0) {
                V2NIMMessage replyMessage = data.get(0).getMessage();
                replyMessage(message, replyMessage, null, null, null);
              } else {
                sendMessageStrExtension(message, mConversationId, null, null);
              }
            }
          });
    } else {
      //直接发送消息
      sendMessageStrExtension(message, mConversationId, null, null);
    }
  }

  public void sendMessageStrExtension(
      V2NIMMessage message, String conversationId, List<String> pushList, String remoteExtension) {
    sendMessageStrExtension(message, conversationId, pushList, remoteExtension, null, null);
  }

  // 发送消息，附带扩展字段
  public void sendMessageStrExtension(
      V2NIMMessage message,
      String conversationId,
      List<String> pushList,
      String remoteExtension,
      V2NIMAIUser aiAgent,
      List<V2NIMAIModelCallMessage> aiMessage) {
    if (message != null) {
      V2NIMSendMessageParams params =
          MessageCreator.createSendMessageParam(
              message, conversationId, pushList, remoteExtension, aiAgent, aiMessage, showRead);
      ChatRepo.sendMessage(
          message,
          conversationId,
          params,
          new ProgressFetchCallback<V2NIMSendMessageResult>() {

            @Override
            public void onProgress(int progress) {
              ALog.i(LIB_TAG, TAG, "sendMessage progress -->> " + progress);
              if (TextUtils.equals(conversationId, mConversationId)
                  && message.getMessageClientId() != null) {
                FetchResult<IMMessageProgress> result = new FetchResult<>(LoadStatus.Success);
                result.setData(new IMMessageProgress(message.getMessageClientId(), progress));
                result.setType(FetchResult.FetchType.Update);
                result.setTypeIndex(-1);
                attachmentProgressMutableLiveData.setValue(result);
              }
            }

            @Override
            public void onSuccess(@Nullable V2NIMSendMessageResult data) {
              ALog.i(LIB_TAG, TAG, "sendMessage onSuccess -->> ");
              if (data != null
                  && TextUtils.equals(data.getMessage().getConversationId(), mConversationId)) {
                ALog.i(LIB_TAG, TAG, "sendMessage onSuccess -->> " + mConversationId);
                V2NIMMessageAIConfig aiConfig = data.getMessage().getAIConfig();
                if (aiConfig != null) {
                  ToastX.showShortToast(R.string.chat_ai_message_progressing);
                }
                if (IMKitConfigCenter.getEnableAntiSpamTipMessage()
                    && data.getAntispamResult() != null) {
                  String tips =
                      MessageHelper.getAntispamTips(
                          IMKitClient.getApplicationContext(), data.getAntispamResult());
                  V2NIMMessage tipMessage = V2NIMMessageCreator.createTipsMessage(tips);
                  ChatRepo.insertMessageToLocal(
                      tipMessage,
                      mConversationId,
                      data.getMessage().getSenderId(),
                      data.getMessage().getCreateTime() + 5,
                      null);
                }
              }
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.i(
                  LIB_TAG, TAG, "sendMessage onError -->> " + errorCode + " errorMsg:" + errorMsg);
            }
          });
    }
  }

  // 发送已读回执
  public abstract void sendReceipt(V2NIMMessage message);

  // 获取消息列表
  public void getMessageList(V2NIMMessage anchor, boolean needToScrollEnd) {
    ALog.i(LIB_TAG, TAG, "initFetch:" + (anchor == null ? "null" : anchor.getMessageClientId()));
    addListener();
    V2NIMMessageListOption.V2NIMMessageListOptionBuilder optionBuilder =
        V2NIMMessageListOption.V2NIMMessageListOptionBuilder.builder(mConversationId)
            .withLimit(messagePageSize)
            .withDirection(V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC);
    if (anchor == null) {
      ALog.i(
          LIB_TAG,
          TAG,
          "Performance getMessageList start timestamp:" + SystemClock.elapsedRealtime());
      ChatRepo.getMessageList(
          optionBuilder.build(),
          new FetchCallback<List<IMMessageInfo>>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.e(LIB_TAG, TAG, "getMessageList error: " + errorCode + ", " + errorMsg);
            }

            @Override
            public void onSuccess(@Nullable List<IMMessageInfo> param) {
              ALog.i(LIB_TAG, TAG, "getMessageList onSuccess");
              ALog.i(
                  LIB_TAG,
                  TAG,
                  "Performance getMessageList onSuccess timestamp:"
                      + SystemClock.elapsedRealtime());
              if (param != null) {
                ALog.i(LIB_TAG, TAG, "getMessageList onSuccess:" + param.size());

                Collections.reverse(param);
                onListFetchSuccess(param, V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC);
              }
              if (!hasLoadMessage) {
                hasLoadMessage = true;
              }
            }
          });
    } else {
      // anchor 不为空时，先重新拉取该消息并通知页面更新，保证消息数据的准确性
      refreshAnchorMessage(anchor);
      fetchMessageListBothDirect(anchor, needToScrollEnd);
    }
  }

  /**
   * 重新从服务器拉取 anchor 消息并通知页面更新该消息的最新数据
   *
   * @param anchor 需要刷新的锚点消息
   */
  private void refreshAnchorMessage(V2NIMMessage anchor) {
    if (anchor == null || TextUtils.isEmpty(anchor.getMessageClientId())) {
      return;
    }
    ALog.i(LIB_TAG, TAG, "refreshAnchorMessage:" + anchor.getMessageClientId());
    List<String> ids = Collections.singletonList(anchor.getMessageClientId());
    ChatRepo.getMessageListByIds(
        ids,
        mConversationId,
        true,
        true,
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(LIB_TAG, TAG, "refreshAnchorMessage onError:" + errorCode + " msg:" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> param) {
            if (param == null || param.isEmpty()) {
              return;
            }
            ALog.i(LIB_TAG, TAG, "refreshAnchorMessage onSuccess size:" + param.size());
            List<ChatMessageBean> beanList = MessageParamBuildUtils.convertToChatBeans(param);
            FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> result =
                new FetchResult<>(LoadStatus.Success);
            result.setData(new Pair<>(MessageUpdateType.ReloadMessage, beanList));
            result.setTypeIndex(-1);
            updateMessageLiveData.setValue(result);
          }
        });
  }

  // 获取更多消息列表
  public void fetchMoreMessage(
      V2NIMMessage anchor, V2NIMMessageQueryDirection direction, boolean needToScrollEnd) {
    ALog.i(
        LIB_TAG,
        TAG,
        "Performance fetchMoreMessage start timestamp:" + SystemClock.elapsedRealtime());
    getMessageListByOptions(anchor, 0, direction, needToScrollEnd);
  }

  private void getMessageListByOptions(
      V2NIMMessage anchor,
      long startTime,
      V2NIMMessageQueryDirection direction,
      boolean needToScrollEnd) {
    V2NIMMessageListOption optionBuilder =
        MessageParamBuildUtils.buildMessageOptions(
            anchor, startTime, mConversationId, messagePageSize, direction);
    ALog.i(LIB_TAG, TAG, "getMessageListByOptions :" + mConversationId);
    ChatRepo.getMessageList(
        optionBuilder,
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            onListFetchFailed(errorCode);
            ALog.i(
                LIB_TAG,
                TAG,
                "getMessageListByOptions:" + errorCode + " errorMsg" + ":" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> data) {
            ALog.i(
                LIB_TAG,
                TAG,
                "Performance getMessageListByOptions onSuccess timestamp:"
                    + SystemClock.elapsedRealtime());
            if (data != null && !data.isEmpty()) {
              if (direction == V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC) {
                Collections.reverse(data);
              }
            }
            ALog.i(LIB_TAG, TAG, "getMessageListByOptions,reverse:" + data.size());
            onListFetchSuccess(anchor, needToScrollEnd, data, direction);
          }
        });
  }

  public void fetchMoreMessage(V2NIMMessage anchor, V2NIMMessageQueryDirection direction) {
    fetchMoreMessage(anchor, direction, true);
  }

  public void fetchMessageListBothDirect(V2NIMMessage anchor, boolean needToScrollEnd) {
    ALog.i(LIB_TAG, TAG, "fetchMessageListBothDirect");
    // 此处避免在获取 anchor 消息后被之前消息添加导致ui移位，因此将 anchor 之前消息请求添加到后续的主线程事件队列中
    new Handler(Looper.getMainLooper())
        .post(
            () ->
                fetchMoreMessage(
                    anchor,
                    V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC,
                    needToScrollEnd));
    fetchMoreMessage(anchor, V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_ASC, needToScrollEnd);
  }

  private void onListFetchSuccess(List<IMMessageInfo> param, V2NIMMessageQueryDirection direction) {
    onListFetchSuccess(null, true, param, direction);
  }

  private void onListFetchSuccess(
      V2NIMMessage anchorMsg,
      boolean needToScrollEnd,
      List<IMMessageInfo> param,
      V2NIMMessageQueryDirection direction) {
    ALog.i(
        LIB_TAG,
        TAG,
        "onListFetchSuccess -->> size:"
            + (param == null ? "null" : param.size())
            + " direction:"
            + direction);

    LoadStatus loadStatus =
        (param == null || param.isEmpty()) ? LoadStatus.Finish : LoadStatus.Success;
    messageFetchResult.setLoadStatus(loadStatus);
    messageFetchResult.setData(MessageParamBuildUtils.convertToChatBeans(param));
    if (anchorMsg != null && !needToScrollEnd) {
      messageFetchResult.setExtraInfo(new AnchorScrollInfo(anchorMsg));
    }
    if (needToScrollEnd) {
      messageFetchResult.setType(FetchResult.FetchType.Init);
    } else {
      messageFetchResult.setType(FetchResult.FetchType.Add);
    }
    messageFetchResult.setTypeIndex(
        direction == V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC ? 0 : -1);
    messageLiveData.setValue(messageFetchResult);
    if (anchorMsg == null) {
      //AI用户首次加载消息，如果消息为空则本地保存一条欢迎语
      if (AIUserManager.isAIUser(mChatAccountId) && (param == null || param.isEmpty())) {
        MessageOperateUtils.saveWelcomeMessage(mConversationId, mChatAccountId);
      }
      //首次加载消息，获取消息的发送者信息
      getTeamMemberInfoWithMessage(param);
    }
  }

  protected void getTeamMemberInfoWithMessage(List<IMMessageInfo> messages) {}

  private void onListFetchFailed(int code) {
    ALog.i(LIB_TAG, TAG, "onListFetchFailed code:" + code);
    messageFetchResult.setError(code, R.string.chat_message_fetch_error);
    messageFetchResult.setData(null);
    messageFetchResult.setTypeIndex(-1);
    messageLiveData.setValue(messageFetchResult);
  }

  // **********reply message**************
  public void replyMessage(
      V2NIMMessage message,
      V2NIMMessage replyMsg,
      List<String> pushList,
      Map<String, Object> remoteExtension,
      V2NIMAIUser aiUser) {
    ALog.i(
        LIB_TAG,
        TAG,
        "replyMessage,message" + (message == null ? "null" : message.getMessageClientId()));
    if (message == null) {
      return;
    }
    //设置remoteExtension
    V2NIMAIModelCallMessage aiMessage = null;
    if (aiUser != null
        && replyMsg != null
        && !TextUtils.isEmpty(MessageHelper.getAIContentMsg(replyMsg))) {
      aiMessage =
          new V2NIMAIModelCallMessage(
              V2NIMAIModelRoleType.V2NIM_AI_MODEL_ROLE_TYPE_USER,
              MessageHelper.getAIContentMsg(replyMsg),
              0);
    }
    //    Map<String, Object> remote = MessageHelper.createReplyExtension(remoteExtension, replyMsg);
    //      sendMessage(
    //              message,
    //              pushList,
    //              remote,
    //              aiUser,
    //              aiMessage == null ? null : Collections.singletonList(aiMessage));
    sendReplyMessage(message, replyMsg, pushList, remoteExtension, aiMessage, aiUser);
  }

  public void replyTextMessage(
      String content,
      V2NIMMessage message,
      List<String> pushList,
      Map<String, Object> remoteExtension,
      V2NIMAIUser aiUser) {
    ALog.i(
        LIB_TAG,
        TAG,
        "replyTextMessage,message" + (message == null ? "null" : message.getMessageClientId()));
    V2NIMMessage textMessage = V2NIMMessageCreator.createTextMessage(content);
    replyMessage(textMessage, message, pushList, remoteExtension, aiUser);
  }

  public void sendReplyMessage(
      V2NIMMessage message,
      V2NIMMessage replyMessage,
      List<String> pushList,
      Map<String, Object> remoteExtension,
      V2NIMAIModelCallMessage aiMessage,
      V2NIMAIUser aiUser) {
    if (replyMessage == null) {
      sendMessage(
          message,
          pushList,
          remoteExtension,
          aiUser,
          aiMessage == null ? null : Collections.singletonList(aiMessage));
    }
    List<V2NIMAIModelCallMessage> aiMessageList =
        aiMessage == null ? null : Collections.singletonList(aiMessage);
    String remoteStr = MessageParamBuildUtils.toJson(remoteExtension);
    V2NIMSendMessageParams params =
        MessageCreator.createSendMessageParam(
            message, mConversationId, pushList, remoteStr, aiUser, aiMessageList, showRead);
    ChatRepo.replyMessage(
        message,
        replyMessage,
        mConversationId,
        params,
        new ProgressFetchCallback<V2NIMSendMessageResult>() {

          @Override
          public void onProgress(int progress) {
            ALog.i(LIB_TAG, TAG, "replyMessage progress -->> " + progress);
            if (TextUtils.equals(replyMessage.getConversationId(), mConversationId)
                && message.getMessageClientId() != null) {
              FetchResult<IMMessageProgress> result = new FetchResult<>(LoadStatus.Success);
              result.setData(new IMMessageProgress(message.getMessageClientId(), progress));
              result.setType(FetchResult.FetchType.Update);
              result.setTypeIndex(-1);
              attachmentProgressMutableLiveData.setValue(result);
            }
          }

          @Override
          public void onSuccess(@Nullable V2NIMSendMessageResult data) {
            ALog.i(LIB_TAG, TAG, "replyMessage onSuccess -->> ");
            if (data != null
                && TextUtils.equals(data.getMessage().getConversationId(), mConversationId)) {
              ALog.i(LIB_TAG, TAG, "replyMessage onSuccess -->> " + mConversationId);
              //                postMessageSend(new IMMessageInfo(data.getMessage()), false);
              V2NIMMessageAIConfig aiConfig = data.getMessage().getAIConfig();
              if (aiConfig != null) {
                ToastX.showShortToast(R.string.chat_ai_message_progressing);
              }
            }
          }

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.i(
                LIB_TAG, TAG, "replyMessage onError -->> " + errorCode + " errorMsg:" + errorMsg);
          }
        });
  }

  //停止流式消息输出
  public void stopAIStream(IMMessageInfo messageInfo) {
    V2NIMMessageAIStreamStopParams params =
        new V2NIMMessageAIStreamStopParams(
            V2NIMMessageAIStreamStopOpType.V2NIM_MESSAGE_AI_STREAM_STOP_OP_DEFAULT);
    ChatRepo.stopAIStreamMessage(
        messageInfo.getMessage(),
        params,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.i(LIB_TAG, TAG, "stopAIStreamMessage onError" + errorCode);
            ToastX.showShortToast(R.string.chat_ai_search_error);
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ALog.i(LIB_TAG, TAG, "stopAIStreamMessage onSuccess");
          }
        });
  }

  // 重新输出数字人消息
  public void regenAIMessage(IMMessageInfo messageInfo) {
    V2NIMMessageAIRegenParams params =
        new V2NIMMessageAIRegenParams(V2NIMMessageAIRegenOpType.V2NIM_MESSAGE_AI_REGEN_OP_NEW);
    ChatRepo.regenAIMessage(
        messageInfo.getMessage(),
        params,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.i(LIB_TAG, TAG, "regenAIMessage onError" + errorCode);
            if (errorCode == ChatKitUIConstant.ERROR_CODE_AI_REGEN_NONE) {
              ToastX.showShortToast(R.string.chat_message_removed_tip);
            } else {
              ToastX.showShortToast(R.string.chat_ai_search_error);
            }
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ALog.i(LIB_TAG, TAG, "regenAIMessage onSuccess");
            ToastX.showShortToast(R.string.chat_ai_message_progressing);
          }
        });
  }

  // ********************Message Pin********************

  private final MutableLiveData<Pair<String, V2NIMMessagePin>> addPinMessageLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<String> removePinMessageLiveData = new MutableLiveData<>();

  public MutableLiveData<Pair<String, V2NIMMessagePin>> getAddPinMessageLiveData() {
    return addPinMessageLiveData;
  }

  public MutableLiveData<String> getRemovePinMessageLiveData() {
    return removePinMessageLiveData;
  }

  public void addMessagePin(IMMessageInfo messageInfo, String ext) {
    if (messageInfo == null) {
      return;
    }
    ALog.i(LIB_TAG, TAG, "addMessagePin,message" + messageInfo.getMessage().getMessageClientId());
    ChatRepo.pinMessage(
        messageInfo.getMessage(),
        ext,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (errorCode == ChatKitUIConstant.ERROR_CODE_PIN_MSG_LIMIT) {
              ToastX.showShortToast(R.string.chat_pin_limit_tips);
            }
            ALog.i(LIB_TAG, TAG, "addMessagePin,onError" + errorCode + " errorMsg:" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ALog.i(
                LIB_TAG,
                TAG,
                "addMessagePin, message onSuccess" + messageInfo.getMessage().getMessageClientId());
          }
        });
  }

  public void removeMsgPin(IMMessageInfo messageInfo) {
    if (messageInfo == null
        || messageInfo.getPinOption() == null
        || messageInfo.getPinOption().getMessageRefer() == null) {
      return;
    }
    ALog.i(LIB_TAG, TAG, "removeMsgPin,message" + messageInfo.getMessage().getMessageClientId());
    ChatRepo.unpinMessage(messageInfo.getPinOption().getMessageRefer(), null);
  }

  /** 更新语音播放模式 */
  public void updateVoicePlayModel() {
    boolean currentValue = ChatKitClient.isEarphoneMode();
    ALog.i(LIB_TAG, TAG, "updateVoicePlayModel, currentValue:" + currentValue);
    ChatKitClient.setEarphoneMode(!currentValue);
    FetchResult<List<String>> userInfoFetchResult = new FetchResult<>(LoadStatus.Finish);
    userInfoFetchResult.setData(Collections.singletonList(mChatAccountId));
    userInfoFetchResult.setType(FetchResult.FetchType.Update);
    userChangeLiveData.setValue(userInfoFetchResult);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    removeListener();
  }
}
