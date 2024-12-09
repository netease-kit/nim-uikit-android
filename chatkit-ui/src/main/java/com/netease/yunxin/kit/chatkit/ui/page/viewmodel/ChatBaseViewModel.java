// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.ai.enums.V2NIMAIModelRoleType;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMAIModelCallContent;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMAIModelCallMessage;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversation;
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
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageAudioAttachment;
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessageAIConfig;
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessageConfig;
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessagePushConfig;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessagePinState;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageQueryDirection;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageSendingState;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.sdk.v2.message.option.V2NIMMessageListOption;
import com.netease.nimlib.sdk.v2.message.params.V2NIMAddCollectionParams;
import com.netease.nimlib.sdk.v2.message.params.V2NIMMessageAIConfigParams;
import com.netease.nimlib.sdk.v2.message.params.V2NIMSendMessageParams;
import com.netease.nimlib.sdk.v2.message.params.V2NIMVoiceToTextParams;
import com.netease.nimlib.sdk.v2.message.result.V2NIMSendMessageResult;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.listener.ChatListener;
import com.netease.yunxin.kit.chatkit.listener.MessageRevokeNotification;
import com.netease.yunxin.kit.chatkit.listener.MessageUpdateType;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.map.ChatLocationBean;
import com.netease.yunxin.kit.chatkit.media.ImageUtil;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.RecentForward;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.ResourceRepo;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
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
import com.netease.yunxin.kit.common.utils.EncryptUtils;
import com.netease.yunxin.kit.common.utils.FileUtils;
import com.netease.yunxin.kit.common.utils.ImageUtils;
import com.netease.yunxin.kit.common.utils.UriUtils;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.extend.ProgressFetchCallback;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.im2.provider.V2MessageProvider;
import java.io.File;
import java.io.IOException;
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
  // 是否需要消息回执
  protected boolean needACK = false;
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

  // 消息监听
  private final ChatListener messageListener =
      new ChatListener() {

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
            ALog.d(LIB_TAG, TAG, "onSendMessage -->> " + message.getMessageClientId());
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
            ALog.d(LIB_TAG, TAG, "onMessageAttachmentDownloadProgress -->> " + progress);
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
          ALog.d(LIB_TAG, TAG, "onMessagesUpdate -->> " + messages.size() + ", type:" + type);
          FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> messageUpdateResult =
              new FetchResult<>(LoadStatus.Success);
          messageUpdateResult.setData(new Pair<>(type, convert(messages)));
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
          ALog.d(LIB_TAG, TAG, "receive msg -->> " + messages.size());
          FetchResult<List<ChatMessageBean>> messageRecFetchResult =
              new FetchResult<>(LoadStatus.Success);
          messageRecFetchResult.setData(convert(messages));
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
          ALog.d(
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
          ALog.d(LIB_TAG, TAG, "onMessagePinNotification");
          if (pinNotification != null
              && Objects.equals(
                  pinNotification.getPin().getMessageRefer().getConversationId(),
                  mConversationId)) {
            ALog.d(
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
            ALog.d(
                LIB_TAG,
                TAG,
                "getPinedMessageList,onFailed:" + errorCode + " errorMsg:" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable List<V2NIMMessagePin> data) {
            ALog.d(
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
    if (messageList == null || messageList.isEmpty()) {
      return;
    }
    if (messageList.size() < 2) {
      ChatRepo.deleteMessage(
          messageList.get(0).getMessageData().getMessage(),
          null,
          false,
          new FetchCallback<Void>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              FetchResult<List<V2NIMMessageRefer>> fetchResult =
                  new FetchResult<>(LoadStatus.Error);
              fetchResult.setError(-1, R.string.chat_message_delete_error);
              deleteMessageLiveData.setValue(fetchResult);
              ALog.d(LIB_TAG, TAG, "deleteMessage,onFailed:" + errorCode + " errorMsg:" + errorMsg);
            }

            @Override
            public void onSuccess(@Nullable Void data) {
              doActionAfterDelete(messageList);
            }
          });
    } else {
      List<V2NIMMessage> deleteList = new ArrayList<>();
      boolean onlyDeleteLocal = true;
      for (ChatMessageBean messageBean : messageList) {
        deleteList.add(messageBean.getMessageData().getMessage());
        //只要有一条成功的消息就不会只删除本地
        if (!TextUtils.isEmpty(messageBean.getMessageData().getMessage().getMessageServerId())) {
          onlyDeleteLocal = false;
        }
      }
      ChatRepo.deleteMessages(
          deleteList,
          null,
          onlyDeleteLocal,
          new FetchCallback<Void>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              FetchResult<List<V2NIMMessageRefer>> fetchResult =
                  new FetchResult<>(LoadStatus.Error);
              fetchResult.setError(-1, R.string.chat_message_delete_error);
              deleteMessageLiveData.setValue(fetchResult);
              ALog.d(
                  LIB_TAG, TAG, "deleteMessages,onFailed:" + errorCode + " errorMsg:" + errorMsg);
            }

            @Override
            public void onSuccess(@Nullable Void data) {
              doActionAfterDelete(messageList);
            }
          });
    }
  }
  // 执行删除消息后的操作
  private void doActionAfterDelete(List<ChatMessageBean> messageBean) {
    List<V2NIMMessageRefer> deleteMessageList = new ArrayList<>();
    for (ChatMessageBean message : messageBean) {
      deleteMessageList.add(message.getMessageData().getMessage());
    }
    FetchResult<List<V2NIMMessageRefer>> result = new FetchResult<>(LoadStatus.Success);
    result.setData(deleteMessageList);
    result.setType(FetchResult.FetchType.Remove);
    result.setTypeIndex(-1);
    deleteMessageLiveData.setValue(result);
    ALog.d(LIB_TAG, TAG, "deleteMessage, onSuccess");
  }

  // 好友变更通知，子类根据自己需要重写
  public void notifyFriendChange(UserWithFriend friend) {}
  // 撤回消息
  public void revokeMessage(ChatMessageBean messageBean) {
    if (messageBean != null && messageBean.getMessageData() != null) {
      ALog.d(
          LIB_TAG,
          TAG,
          "revokeMessage " + messageBean.getMessageData().getMessage().getMessageClientId());
      revokedMessageClientId = messageBean.getMessageData().getMessage().getMessageClientId();
      ChatRepo.revokeMessage(
          messageBean.getMessageData().getMessage(),
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
              ALog.d(LIB_TAG, TAG, "revokeMessage,onFailed:" + errorCode + " errorMsg:" + errorMsg);
            }

            @Override
            public void onSuccess(@Nullable Void data) {
              if (!TextUtils.isEmpty(messageBean.getPinAccid())) {
                ChatRepo.unpinMessage(messageBean.getMessageData().getMessage(), null);
              }

              FetchResult<List<MessageRevokeInfo>> fetchResult =
                  new FetchResult<>(LoadStatus.Success);
              MessageRevokeInfo messageRevokeInfo =
                  new MessageRevokeInfo(messageBean.getMessageData().getMessage(), null);
              fetchResult.setData(Collections.singletonList(messageRevokeInfo));
              revokeMessageLiveData.setValue(fetchResult);

              ALog.d(LIB_TAG, TAG, "revokeMessage, onSuccess");
            }
          });
    }
  }

  //语音转文字
  public void voiceToText(ChatMessageBean messageBean) {
    if (messageBean != null
        && messageBean.getMessageData() != null
        && messageBean.getMessageData().getMessage().getMessageType()
            == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO) {
      V2NIMMessageAudioAttachment audioAttachment =
          (V2NIMMessageAudioAttachment) messageBean.getMessageData().getMessage().getAttachment();
      V2NIMVoiceToTextParams.V2NIMVoiceToTextParamsBuilder paramsBuilder =
          V2NIMVoiceToTextParams.V2NIMVoiceToTextParamsBuilder.builder(
              audioAttachment.getDuration());
      String path = audioAttachment.getPath();
      if (!TextUtils.isEmpty(audioAttachment.getUrl())) {
        paramsBuilder.withVoiceUrl(audioAttachment.getUrl());
      } else if (!TextUtils.isEmpty(path) && FileUtils.isFileExists(path)) {
        paramsBuilder.withVoicePath(path);
      } else {
        ALog.d(
            LIB_TAG,
            TAG,
            "voiceToText,param error path = " + path + " url = " + audioAttachment.getUrl());
        return;
      }
      paramsBuilder.withSceneName(audioAttachment.getSceneName());
      V2NIMVoiceToTextParams params = paramsBuilder.build();
      ChatRepo.voiceToText(
          params,
          new FetchCallback<String>() {
            @Override
            public void onSuccess(@Nullable String data) {
              if (!TextUtils.isEmpty(data)) {
                FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> messageUpdateResult =
                    new FetchResult<>(LoadStatus.Success);
                List<ChatMessageBean> messageList = new ArrayList<>();
                messageBean.setVoiceToText(data);
                messageList.add(messageBean);
                messageUpdateResult.setData(new Pair<>(MessageUpdateType.VoiceToText, messageList));
                messageUpdateResult.setType(FetchResult.FetchType.Update);
                messageUpdateResult.setTypeIndex(-1);
                updateMessageLiveData.setValue(messageUpdateResult);
              } else {
                FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> messageUpdateResult =
                    new FetchResult<>(LoadStatus.Error);
                messageUpdateResult.setError(0, R.string.chat_voice_to_text_failed);
                messageUpdateResult.setData(null);
                updateMessageLiveData.setValue(messageUpdateResult);
              }
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> messageUpdateResult =
                  new FetchResult<>(LoadStatus.Error);
              messageUpdateResult.setError(0, R.string.chat_voice_to_text_failed);
              messageUpdateResult.setData(null);
              updateMessageLiveData.setValue(messageUpdateResult);
              ALog.d(LIB_TAG, TAG, "voiceToText,onFailed:" + errorCode + " errorMsg:" + errorMsg);
            }
          });
    }
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
    ALog.d(
        LIB_TAG,
        TAG,
        "init accountId:"
            + accountId
            + " sessionType:"
            + sessionType
            + " conversationId:"
            + mConversationId);
    ChatUserCache.getInstance().clear();
    SettingRepo.getShowReadStatus(
        new FetchCallback<Boolean>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {}

          @Override
          public void onSuccess(@Nullable Boolean param) {
            needACK = Boolean.TRUE.equals(param);
          }
        });
  }

  // 设置当前会话账号，清理未读数
  public void setChattingAccount() {
    ALog.d(LIB_TAG, TAG, "setChattingAccount sessionId:" + mConversationId);
    if (!TextUtils.isEmpty(mConversationId)) {
      ChatRepo.setChattingId(mConversationId, mSessionType);
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
    ChatRepo.clearChattingId();
  }

  // 注册监听
  public void addListener() {
    ALog.d(LIB_TAG, TAG, "registerObservers ");
    ChatRepo.addMessageListener(messageListener);
  }

  // 移除监听
  public void removeListener() {
    ALog.d(LIB_TAG, TAG, "unregisterObservers ");
    ChatUserCache.getInstance().clear();
    ChatRepo.removeMessageListener(messageListener);
  }

  // 发送文本消息
  public void sendTextMessage(String content, List<String> pushList) {
    ALog.d(LIB_TAG, TAG, "sendTextMessage:" + (content != null ? content.length() : "null"));
    sendTextMessage(content, pushList, null);
  }

  // 发送文本消息
  public void sendTextMessage(
      String content, List<String> pushList, Map<String, Object> remoteExtension) {
    ALog.d(LIB_TAG, TAG, "sendTextMessage:" + (content != null ? content.length() : "null"));
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
    ALog.d(LIB_TAG, TAG, "sendTextMessage:" + (content != null ? content.length() : "null"));
    V2NIMMessage textMessage = V2NIMMessageCreator.createTextMessage(content);
    //        sendMessage(textMessage, pushList, remoteExtension);
    if (remoteExtension != null) {
      JSONObject jsonObject = new JSONObject(remoteExtension);
      sendMessageStrExtension(
          textMessage, mConversationId, pushList, jsonObject.toString(), aiUser, aiMessages);
    } else {
      sendMessageStrExtension(textMessage, mConversationId, pushList, null, aiUser, aiMessages);
    }
  }

  // 添加收藏
  public void addMsgCollection(String conversationName, IMMessageInfo messageInfo) {
    if (messageInfo == null) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "addMsgCollection:" + messageInfo.getMessage().getMessageClientId());
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
      ALog.d(LIB_TAG, TAG, "sendAudioMessage:" + audio.getPath());
      V2NIMMessage audioMessage =
          V2NIMMessageCreator.createAudioMessage(
              audio.getPath(), audio.getName(), null, audioLength);
      sendMessage(audioMessage, null, null);
    }
  }

  // 发送图片消息
  public void sendImageMessage(File imageFile) {
    if (imageFile != null) {
      ALog.d(LIB_TAG, TAG, "sendImageMessage:" + imageFile.getPath());
      int[] bounds = ImageUtils.getSize(imageFile);
      V2NIMMessage imageMessage =
          V2NIMMessageCreator.createImageMessage(
              imageFile.getPath(), imageFile.getName(), null, bounds[0], bounds[1]);
      sendMessage(imageMessage, null, null);
    }
  }

  // 发送自定义消息
  public void sendCustomMessage(Map<String, Object> attachment, String content) {
    if (attachment != null) {
      ALog.d(LIB_TAG, TAG, "sendCustomMessage:" + attachment.getClass().getName());
      String attachStr = new JSONObject(attachment).toString();
      V2NIMMessage customMsg = V2NIMMessageCreator.createCustomMessage(content, attachStr);
      sendMessage(customMsg, null, null);
    }
  }

  // 发送转发消息(单条转发)
  public void sendForwardMessage(
      ChatMessageBean message, String inputMsg, List<String> conversationIds) {
    ALog.d(LIB_TAG, TAG, "sendForwardMessage:" + conversationIds.size());
    MessageHelper.sendForwardMessage(message, inputMsg, conversationIds, false, needACK);
  }
  // 发送转发消息（逐条转发）
  public void sendForwardMessages(
      String inputMsg, List<String> conversationIds, List<ChatMessageBean> messages) {
    MessageHelper.sendForwardMessages(inputMsg, conversationIds, messages, false, needACK);
  }

  /** AI 消息本地保存一个欢迎语 */
  public void saveWelcomeMessage() {
    String content = AIUserManager.getWelcomeText(mChatAccountId);
    if (TextUtils.isEmpty(content)) {
      return;
    }
    V2NIMMessage welcomeMessage = V2NIMMessageCreator.createTextMessage(content);
    ConversationRepo.createConversation(
        mConversationId,
        new FetchCallback<V2NIMConversation>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(TAG, "createConversation onError:" + errorCode + " errorMsg:" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable V2NIMConversation data) {
            //成功之后插入
            ChatRepo.insertMessageToLocal(
                welcomeMessage, mConversationId, mChatAccountId, System.currentTimeMillis(), null);
          }
        });
  }

  // 发送合并转发消息
  public void sendMultiForwardMessage(
      String displayName,
      String inputMsg,
      List<String> conversationIds,
      List<ChatMessageBean> messages) {
    ALog.d(LIB_TAG, TAG, "sendMultiForwardMessage");
    if (conversationIds == null
        || conversationIds.isEmpty()
        || messages == null
        || messages.isEmpty()) {
      return;
    }

    List<IMMessageInfo> iMessageList = new ArrayList<>();
    for (ChatMessageBean message : messages) {
      iMessageList.add(message.getMessageData());
    }

    //合并转发消息，序列化消息
    String msgInfo = MessageHelper.createMultiForwardMsg(iMessageList);
    try {

      File localFile = SendMediaHelper.createTextFile();
      //      保存到本地并上传到nos
      ResourceRepo.writeLocalFileAndUploadNOS(
          localFile,
          msgInfo,
          new FetchCallback<String>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.e(
                  LIB_TAG,
                  TAG,
                  "writeLocalFileAndUploadNOS onError:" + errorCode + " errorMsg:" + errorMsg);
            }

            @Override
            public void onSuccess(@Nullable String param) {
              if (param != null) {
                String fileMD5 = EncryptUtils.md5(localFile);
                MultiForwardAttachment attachment =
                    MessageHelper.createMultiTransmitAttachment(
                        displayName, mChatAccountId, param, iMessageList);
                attachment.md5 = fileMD5;
                List<RecentForward> recentForwards = new ArrayList<>();
                for (String conversationId : conversationIds) {
                  V2NIMMessage multiForwardMessage =
                      V2NIMMessageCreator.createCustomMessage(displayName, attachment.toJsonStr());
                  sendMessageStrExtension(multiForwardMessage, conversationId, null, null);
                  String sessionId = V2NIMConversationIdUtil.conversationTargetId(conversationId);
                  V2NIMConversationType sessionType =
                      V2NIMConversationIdUtil.conversationType(conversationId);
                  recentForwards.add(new RecentForward(sessionId, sessionType));
                }
                SettingRepo.saveRecentForward(recentForwards);

                MessageHelper.sendNoteMessage(inputMsg, conversationIds, needACK);
              }
            }
          });

    } catch (IOException e) {
      ALog.e(LIB_TAG, TAG, "sendMultiForwardMessage IOException:" + e.getMessage());
    }
  }

  // 发送位置消息
  public void sendLocationMessage(ChatLocationBean locationBean) {
    ALog.d(LIB_TAG, TAG, "sendLocationMessage:" + locationBean);
    V2NIMMessage locationMsg =
        V2NIMMessageCreator.createLocationMessage(
            locationBean.getLat(), locationBean.getLng(), locationBean.getAddress());
    locationMsg.setText(locationBean.getTitle());
    sendMessage(locationMsg, null, null);
  }

  // 发送视频消息
  public void sendVideoMessage(
      File videoFile, int duration, int width, int height, String displayName) {
    if (videoFile != null) {
      ALog.d(LIB_TAG, TAG, "sendVideoMessage:" + videoFile.getPath());
      V2NIMMessage msg =
          V2NIMMessageCreator.createVideoMessage(
              videoFile.getPath(), displayName, null, duration, width, height);
      sendMessage(msg, null, null);
    }
  }

  // 发送文件消息
  public void sendFileMessage(File docsFile, String displayName) {
    if (docsFile != null) {
      ALog.d(LIB_TAG, TAG, "sendFileMessage:" + docsFile.getPath());
      if (TextUtils.isEmpty(displayName)) {
        displayName = docsFile.getName();
      }
      V2NIMMessage msg =
          V2NIMMessageCreator.createFileMessage(docsFile.getPath(), displayName, null);
      sendMessage(msg, null, null);
    }
  }

  public void sendImageOrVideoMessage(Uri uri, Context context) {
    ALog.d(LIB_TAG, TAG, "sendImageOrVideoMessage:" + uri);
    if (uri == null) {
      return;
    }
    //文件大小限制，单位字节
    long limitSize = ChatUtils.getFileLimitSize() * 1024 * 1024;
    String mimeType = FileUtils.getFileExtension(uri.getPath());
    if (TextUtils.isEmpty(mimeType)) {
      try {
        String realPath = UriUtils.uri2FileRealPath(uri);
        mimeType = FileUtils.getFileExtension(realPath);
      } catch (IllegalStateException e) {
        ToastX.showShortToast(R.string.chat_message_type_resource_error);
        return;
      }
    }
    if (ImageUtil.isValidPictureFile(mimeType)) {
      File file = UriUtils.uri2File(uri);
      if (file != null && file.length() > limitSize) {
        String fileSizeLimit = String.valueOf(ChatUtils.getFileLimitSize());
        String limitText =
            String.format(
                context.getString(R.string.chat_message_file_size_limit_tips), fileSizeLimit);
        ToastX.showShortToast(limitText);
        return;
      }
      sendImageMessage(file);
    } else if (ImageUtil.isValidVideoFile(mimeType)) {
      File file = UriUtils.uri2File(uri);
      if (file != null && file.length() > limitSize) {
        String fileSizeLimit = String.valueOf(ChatUtils.getFileLimitSize());
        String limitText =
            String.format(
                context.getString(R.string.chat_message_file_size_limit_tips), fileSizeLimit);
        ToastX.showShortToast(limitText);
        return;
      }
      MediaMetadataRetriever mmr = new MediaMetadataRetriever();
      try {
        mmr.setDataSource(file.getPath());
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String orientation =
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (TextUtils.equals(orientation, Orientation_Vertical)) {
          String local = width;
          width = height;
          height = local;
        }
        ALog.d(LIB_TAG, TAG, "width:" + width + "height" + height + "orientation:" + orientation);
        sendVideoMessage(
            file,
            Integer.parseInt(duration),
            Integer.parseInt(width),
            Integer.parseInt(height),
            file.getName());
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          mmr.release();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } else {
      ToastX.showShortToast(R.string.chat_message_type_not_support_tips);
      ALog.d(LIB_TAG, TAG, "invalid file type");
    }
  }

  // 发送文件消息
  public void sendFile(Uri uri) {
    ALog.d(LIB_TAG, TAG, "sendFile:" + (uri != null ? uri.getPath() : "uri is null"));
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
    ALog.d(LIB_TAG, TAG, "postMessageSend:" + sending);
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
    if (remoteExtension != null) {
      JSONObject jsonObject = new JSONObject(remoteExtension);
      sendMessageStrExtension(
          message, mConversationId, pushList, jsonObject.toString(), aiUser, aiMessages);
    } else {
      sendMessageStrExtension(message, mConversationId, pushList, null, aiUser, aiMessages);
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
      ALog.d(
          LIB_TAG,
          TAG,
          "sendMessage:"
              + message.getMessageClientId()
              + " needACK:"
              + needACK
              + " showRead:"
              + showRead);

      V2NIMMessageConfig.V2NIMMessageConfigBuilder configBuilder =
          V2NIMMessageConfig.V2NIMMessageConfigBuilder.builder();
      configBuilder.withReadReceiptEnabled(needACK && showRead);
      V2NIMMessagePushConfig.V2NIMMessagePushConfigBuilder pushConfigBuilder =
          V2NIMMessagePushConfig.V2NIMMessagePushConfigBuilder.builder();
      if (pushList != null && !pushList.isEmpty()) {
        pushConfigBuilder.withForcePush(true).withForcePushAccountIds(pushList);
      }
      V2NIMSendMessageParams.V2NIMSendMessageParamsBuilder paramsBuilder =
          V2NIMSendMessageParams.V2NIMSendMessageParamsBuilder.builder()
              .withMessageConfig(configBuilder.build())
              .withPushConfig(pushConfigBuilder.build());

      //remoteExtension设置
      if (!TextUtils.isEmpty(remoteExtension)) {
        message.setServerExtension(remoteExtension);
      }

      //@ AI机器人代理设置
      V2NIMMessageAIConfigParams aiConfigParams = null;
      String chatId = V2NIMConversationIdUtil.conversationTargetId(conversationId);
      if (aiAgent == null && AIUserManager.isAIUser(chatId)) {
        aiAgent = AIUserManager.getAIUserById(chatId);
      }
      if (aiAgent != null) {
        aiConfigParams = new V2NIMMessageAIConfigParams(aiAgent.getAccountId());
        if (!TextUtils.isEmpty(MessageHelper.getAIContentMsg(message))) {
          V2NIMAIModelCallContent content =
              new V2NIMAIModelCallContent(MessageHelper.getAIContentMsg(message), 0);
          aiConfigParams.setContent(content);
        }
      }
      //AI消息上下文设置
      if (aiConfigParams != null && aiMessage != null && !aiMessage.isEmpty()) {
        aiConfigParams.setMessages(aiMessage);
      }
      if (aiConfigParams != null) {
        paramsBuilder.withAIConfig(aiConfigParams);
      }
      ChatRepo.sendMessage(
          message,
          conversationId,
          paramsBuilder.build(),
          new ProgressFetchCallback<V2NIMSendMessageResult>() {

            @Override
            public void onProgress(int progress) {
              ALog.d(LIB_TAG, TAG, "sendMessage progress -->> " + progress);
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
              ALog.d(LIB_TAG, TAG, "sendMessage onSuccess -->> ");
              if (data != null
                  && TextUtils.equals(data.getMessage().getConversationId(), mConversationId)) {
                ALog.d(LIB_TAG, TAG, "sendMessage onSuccess -->> " + mConversationId);
                //                postMessageSend(new IMMessageInfo(data.getMessage()), false);
                V2NIMMessageAIConfig aiConfig = data.getMessage().getAIConfig();
                if (aiConfig != null) {
                  ToastX.showShortToast(R.string.chat_ai_message_progressing);
                }
              }
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.d(
                  LIB_TAG, TAG, "sendMessage onError -->> " + errorCode + " errorMsg:" + errorMsg);
            }
          });
    }
  }

  // 发送已读回执
  public abstract void sendReceipt(V2NIMMessage message);

  // 获取消息列表
  public void getMessageList(V2NIMMessage anchor, boolean needToScrollEnd) {
    ALog.d(LIB_TAG, TAG, "initFetch:" + (anchor == null ? "null" : anchor.getMessageClientId()));
    addListener();
    V2NIMMessageListOption.V2NIMMessageListOptionBuilder optionBuilder =
        V2NIMMessageListOption.V2NIMMessageListOptionBuilder.builder(mConversationId)
            .withLimit(messagePageSize)
            .withDirection(V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC);
    if (anchor == null) {
      ChatRepo.getMessageList(
          optionBuilder.build(),
          new FetchCallback<List<IMMessageInfo>>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {}

            @Override
            public void onSuccess(@Nullable List<IMMessageInfo> param) {
              if (param != null) {
                Collections.reverse(param);
                //                    fetchPinInfo();
                onListFetchSuccess(param, V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC);
              }
              if (!hasLoadMessage) {
                hasLoadMessage = true;
              }
            }
          });
    } else {
      fetchMessageListBothDirect(anchor, needToScrollEnd);
    }
  }

  /** called when entering the chat page */
  public void getMessageList(V2NIMMessage anchor) {
    getMessageList(anchor, true);
  }

  // 获取更多消息列表
  public void fetchMoreMessage(
      V2NIMMessage anchor, V2NIMMessageQueryDirection direction, boolean needToScrollEnd) {
    ALog.d(LIB_TAG, TAG, "fetchMoreMessage:" + " direction:" + direction);

    V2NIMMessageListOption.V2NIMMessageListOptionBuilder optionBuilder =
        V2NIMMessageListOption.V2NIMMessageListOptionBuilder.builder(mConversationId)
            .withLimit(messagePageSize)
            .withAnchorMessage(anchor)
            .withDirection(direction);

    if (direction == V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC) {
      optionBuilder.withEndTime(anchor.getCreateTime());
    } else {
      optionBuilder.withBeginTime(anchor.getCreateTime());
    }

    ChatRepo.getMessageList(
        optionBuilder.build(),
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            onListFetchFailed(errorCode);
            ALog.d(LIB_TAG, TAG, "fetchMoreMessage:" + errorCode + " errorMsg:" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> data) {
            if (data != null && !data.isEmpty()) {
              if (direction == V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC) {
                Collections.reverse(data);
              }
              ALog.d(LIB_TAG, TAG, "fetchMoreMessage,reverse:" + data.size());
              onListFetchSuccess(anchor, needToScrollEnd, data, direction);
            }
          }
        });
  }

  public void fetchMoreMessage(V2NIMMessage anchor, V2NIMMessageQueryDirection direction) {
    fetchMoreMessage(anchor, direction, true);
  }

  public void fetchMessageListBothDirect(V2NIMMessage anchor, boolean needToScrollEnd) {
    ALog.d(LIB_TAG, TAG, "fetchMessageListBothDirect");
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
    ALog.d(
        LIB_TAG,
        TAG,
        "onListFetchSuccess -->> size:"
            + (param == null ? "null" : param.size())
            + " direction:"
            + direction);

    LoadStatus loadStatus =
        (param == null || param.size() == 0) ? LoadStatus.Finish : LoadStatus.Success;
    messageFetchResult.setLoadStatus(loadStatus);
    messageFetchResult.setData(convert(param));
    if (anchorMsg != null && !needToScrollEnd) {
      messageFetchResult.setExtraInfo(new AnchorScrollInfo(anchorMsg));
    }
    messageFetchResult.setTypeIndex(
        direction == V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC ? 0 : -1);
    messageLiveData.setValue(messageFetchResult);
    if (anchorMsg == null) {
      //AI用户首次加载消息，如果消息为空则本地保存一条欢迎语
      if (AIUserManager.isAIUser(mChatAccountId) && (param == null || param.isEmpty())) {
        saveWelcomeMessage();
      }
      //首次加载消息，获取消息的发送者信息
      getTeamMemberInfoWithMessage(param);
    }
  }

  protected void getTeamMemberInfoWithMessage(List<IMMessageInfo> messages) {}

  private void onListFetchFailed(int code) {
    ALog.d(LIB_TAG, TAG, "onListFetchFailed code:" + code);
    messageFetchResult.setError(code, R.string.chat_message_fetch_error);
    messageFetchResult.setData(null);
    messageFetchResult.setTypeIndex(-1);
    messageLiveData.setValue(messageFetchResult);
  }

  private List<ChatMessageBean> convert(List<IMMessageInfo> messageList) {
    if (messageList == null) {
      return null;
    }
    ArrayList<ChatMessageBean> result = new ArrayList<>(messageList.size());
    for (IMMessageInfo message : messageList) {
      result.add(new ChatMessageBean(message));
    }
    return result;
  }

  // **********reply message**************
  public void replyMessage(
      V2NIMMessage message,
      V2NIMMessage replyMsg,
      List<String> pushList,
      Map<String, Object> remoteExtension,
      V2NIMAIUser aiUser) {
    ALog.d(
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
    Map<String, Object> remote = MessageHelper.createReplyExtension(remoteExtension, replyMsg);

    sendMessage(
        message,
        pushList,
        remote,
        aiUser,
        aiMessage == null ? null : Collections.singletonList(aiMessage));
  }

  public void replyTextMessage(
      String content,
      V2NIMMessage message,
      List<String> pushList,
      Map<String, Object> remoteExtension,
      V2NIMAIUser aiUser) {
    ALog.d(
        LIB_TAG,
        TAG,
        "replyTextMessage,message" + (message == null ? "null" : message.getMessageClientId()));
    V2NIMMessage textMessage = V2NIMMessageCreator.createTextMessage(content);
    replyMessage(textMessage, message, pushList, remoteExtension, aiUser);
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
    ALog.d(LIB_TAG, TAG, "addMessagePin,message" + messageInfo.getMessage().getMessageClientId());
    ChatRepo.pinMessage(
        messageInfo.getMessage(),
        ext,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (errorCode == ChatKitUIConstant.ERROR_CODE_PIN_MSG_LIMIT) {
              ToastX.showShortToast(R.string.chat_pin_limit_tips);
            }
            ALog.d(LIB_TAG, TAG, "addMessagePin,onError" + errorCode + " errorMsg:" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            ALog.d(
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
    ALog.d(LIB_TAG, TAG, "removeMsgPin,message" + messageInfo.getMessage().getMessageClientId());
    ChatRepo.unpinMessage(messageInfo.getPinOption().getMessageRefer(), null);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    removeListener();
  }
}
