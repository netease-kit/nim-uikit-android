// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_REVOKE_EDIT_TAG;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_REVOKE_TAG;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_REVOKE_TIME_TAG;

import android.text.TextUtils;
import com.netease.nimlib.coexist.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessagePin;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessageReferBuilder;
import com.netease.nimlib.coexist.sdk.v2.message.config.V2NIMMessageAIConfig;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageAIStatus;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageAIStreamStatus;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageSendingState;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.coexist.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.CustomAttachment;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.MessagePinInfo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.utils.MessageExtensionHelper;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/** this bean for UI show message， only IMMessageInfo will store in db */
public class ChatMessageBean implements Serializable {

  public ChatMessageBean() {}

  public ChatMessageBean(IMMessageInfo messageData) {
    this.messageData = messageData;
    if (messageData == null) {
      return;
    }
    Map<String, Object> localExtensionMap =
        MessageExtensionHelper.parseJsonStringToMap(messageData.getMessage().getLocalExtension());

    if (localExtensionMap != null && localExtensionMap.containsKey(KEY_REVOKE_TAG)) {
      Object revokeLocal = localExtensionMap.get(KEY_REVOKE_TAG);
      Object revokeEdit = localExtensionMap.get(KEY_REVOKE_EDIT_TAG);
      Object revokeTime = localExtensionMap.get(KEY_REVOKE_TIME_TAG);
      if (revokeLocal instanceof Boolean) {
        isRevoked = (Boolean) revokeLocal;
      }

      if (revokeEdit instanceof Boolean) {
        revokeMsgEdit = (Boolean) revokeEdit;
      }

      if (revokeTime instanceof Integer) {
        revokeMsgTime = (Integer) revokeTime;
      } else if (revokeTime instanceof Long) {
        revokeMsgTime = (Long) revokeTime;
      }
    }

    initReplyMessage();
  }

  IMMessageInfo messageData;

  //默认-1，表示未知
  int viewType = V2NIMMessageType.V2NIM_MESSAGE_TYPE_INVALID.getValue();

  boolean haveRead;

  float loadProgress;

  boolean isRevoked;

  public boolean revokeMsgEdit = true;
  public long revokeMsgTime = 0;

  // 上传进度 % 0-100
  public int progress;

  boolean hasReply = false;
  V2NIMMessageRefer replyMessageRefer;

  IMMessageInfo replyMessage;

  //语音转文字结果，默认是空
  private String voiceToText;

  public void setVoiceToText(String voiceToText) {
    this.voiceToText = voiceToText;
  }

  public String getVoiceToText() {
    return voiceToText;
  }

  public IMMessageInfo getMessageData() {
    return messageData;
  }

  public boolean hasErrorCode() {
    return messageData != null
        && messageData.getMessage().getSendingState()
            == V2NIMMessageSendingState.V2NIM_MESSAGE_SENDING_STATE_SUCCEEDED
        && messageData.getMessage().getMessageStatus() != null
        && messageData.getMessage().getMessageStatus().getErrorCode() != 200;
  }

  public int getErrorCode() {
    if (messageData != null && messageData.getMessage().getMessageStatus() != null) {
      return messageData.getMessage().getMessageStatus().getErrorCode();
    }
    return 0;
  }

  public boolean isReadReceiptEnabled() {
    if (messageData == null
        || messageData.getMessage() == null
        || messageData.getMessage().isSelf()) {
      return false;
    }
    if (messageData.getMessage().getConversationType()
            == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM
        || messageData.getMessage().getConversationType()
            == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_SUPER_TEAM) {
      return messageData.getMessage().getMessageConfig().isReadReceiptEnabled();
    } else if (messageData.getMessage().getConversationType()
        == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      // 单聊默认发送读取回执
      return true;
    }
    return false;
  }

  public V2NIMMessage getMessage() {
    return messageData.getMessage();
  }

  public V2NIMMessageAIConfig getAIConfig() {
    if (messageData != null) {
      return messageData.getMessage().getAIConfig();
    }
    return null;
  }

  public boolean isAIStream() {
    if (messageData != null) {
      V2NIMMessageAIConfig aiConfig = messageData.getMessage().getAIConfig();
      return aiConfig != null && aiConfig.isAIStream();
    } else {
      return false;
    }
  }

  /**
   * 流式消息正在输出中
   *
   * @return
   */
  public boolean AIMessageStreaming() {
    if (isAIStream()) {
      return messageData.getMessage().getAIConfig().getAIStreamStatus()
              == V2NIMMessageAIStreamStatus.V2NIM_MESSAGE_AI_STREAM_STATUS_PLACEHOLDER
          || messageData.getMessage().getAIConfig().getAIStreamStatus()
              == V2NIMMessageAIStreamStatus.V2NIM_MESSAGE_AI_STREAM_STATUS_STREAMING;
    } else {
      return false;
    }
  }

  public boolean isAIResponseMsg() {
    if (messageData != null) {
      V2NIMMessageAIConfig aiConfig = messageData.getMessage().getAIConfig();
      return aiConfig != null
          && aiConfig.getAIStatus() == V2NIMMessageAIStatus.V2NIM_MESSAGE_AI_STATUS_RESPONSE;
    } else {
      return false;
    }
  }

  public String getSenderId() {
    if (messageData != null) {
      V2NIMMessageAIConfig aiConfig = messageData.getMessage().getAIConfig();
      if (aiConfig != null
          && !TextUtils.isEmpty(aiConfig.getAccountId())
          && aiConfig.getAIStatus() == V2NIMMessageAIStatus.V2NIM_MESSAGE_AI_STATUS_RESPONSE) {
        return aiConfig.getAccountId();
      }
    }
    return messageData == null ? "" : messageData.getMessage().getSenderId();
  }

  public String getMsgClientId() {
    return messageData == null ? "" : messageData.getMessage().getMessageClientId();
  }

  public void setMessageData(IMMessageInfo messageData) {
    this.messageData = messageData;
    if (messageData == null) {
      return;
    }
    Map<String, Object> localExtensionMap =
        MessageExtensionHelper.parseJsonStringToMap(messageData.getMessage().getLocalExtension());

    if (localExtensionMap != null && localExtensionMap.containsKey(KEY_REVOKE_TAG)) {
      Object revokeLocal = localExtensionMap.get(KEY_REVOKE_TAG);
      Object revokeEdit = localExtensionMap.get(KEY_REVOKE_EDIT_TAG);
      Object revokeTime = localExtensionMap.get(KEY_REVOKE_TIME_TAG);

      if (revokeLocal instanceof Boolean) {
        isRevoked = (Boolean) revokeLocal;
      }

      if (revokeEdit instanceof Boolean) {
        revokeMsgEdit = (Boolean) revokeEdit;
      }

      if (revokeTime instanceof Integer) {
        revokeMsgTime = (Integer) revokeTime;
      } else if (revokeTime instanceof Long) {
        revokeMsgTime = (Long) revokeTime;
      }
    }

    initReplyMessage();
  }

  public boolean hasReply() {
    return hasReply;
  }

  public V2NIMMessageRefer getReplyMessageRefer() {
    return replyMessageRefer;
  }

  public IMMessageInfo getReplyMessage() {
    return replyMessage;
  }

  public void setReplyMessage(IMMessageInfo msg) {
    replyMessage = msg;
  }

  private void initReplyMessage() {
    if (messageData != null) {
      // 优先取threadReply
      if (messageData.getMessage().getThreadReply() != null) {
        replyMessageRefer = messageData.getMessage().getThreadReply();
        hasReply = true;
        return;
      }
      Map<String, Object> serverExtensionMap =
          MessageExtensionHelper.parseJsonStringToMap(
              messageData.getMessage().getServerExtension());
      hasReply =
          serverExtensionMap != null
              && serverExtensionMap.containsKey(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY);
      if (serverExtensionMap != null
          && serverExtensionMap.containsKey(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY)) {
        Object replyInfo = serverExtensionMap.get(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY);
        if (replyInfo instanceof Map) {
          String clientId = "";
          String senderId = "";
          String serverId = "";
          long time = 0;
          String conversationId = "";
          String receiveId = "";
          V2NIMConversationType conversationType =
              V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P;
          try {
            Map<String, Object> replyMap = (Map<String, Object>) replyInfo;
            if (replyMap.containsKey(ChatKitUIConstant.REPLY_UUID_KEY)) {
              clientId = (String) replyMap.get(ChatKitUIConstant.REPLY_UUID_KEY);
              senderId = (String) replyMap.get(ChatKitUIConstant.REPLY_FROM_KEY);
              serverId = (String) replyMap.get(ChatKitUIConstant.REPLY_SERVER_ID_KEY);
              time = (long) replyMap.get(ChatKitUIConstant.REPLY_TIME_KEY);
              conversationId = (String) replyMap.get(ChatKitUIConstant.REPLY_TO_KEY);
              receiveId =
                  replyMap.get(ChatKitUIConstant.REPLY_RECEIVE_ID_KEY) == null
                      ? V2NIMConversationIdUtil.conversationTargetId(conversationId)
                      : (String) replyMap.get(ChatKitUIConstant.REPLY_RECEIVE_ID_KEY);
              conversationType = V2NIMConversationIdUtil.conversationType(conversationId);
            }
          } catch (Exception e) {
            ALog.e(
                LIB_TAG,
                "V2ChatMessageBean",
                "getReplyUUid,error message"
                    + (messageData == null
                        ? "null"
                        : messageData.getMessage().getMessageClientId()));
          }
          replyMessageRefer =
              V2NIMMessageReferBuilder.builder()
                  .withMessageClientId(clientId)
                  .withSenderId(senderId)
                  .withReceiverId(receiveId)
                  .withConversationType(conversationType)
                  .withMessageServerId(serverId)
                  .withCreateTime(time)
                  .build();
        }
      }
    }
  }

  public boolean isRevoked() {
    return isRevoked;
  }

  public void setRevoked(boolean revoked) {
    isRevoked = revoked;
  }

  public void setHaveRead(boolean haveRead) {
    this.haveRead = haveRead;
  }

  public boolean isHaveRead() {
    return haveRead;
  }

  public int getViewType() {
    if (messageData != null) {
      if (messageData.getMessage().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
        CustomAttachment attachment = messageData.getAttachment();
        if (attachment != null) {
          return attachment.getType();
        }
      } else {
        return messageData.getMessage().getMessageType().getValue();
      }
    }
    return viewType;
  }

  public ChatMessageBean setViewType(int viewType) {
    this.viewType = viewType;
    return this;
  }

  public float getLoadProgress() {
    return loadProgress;
  }

  public void setLoadProgress(float loadProgress) {
    this.loadProgress = loadProgress;
  }

  public void setPinAccid(V2NIMMessagePin pinOption) {
    if (pinOption != null) {
      messageData.setPinOption(new MessagePinInfo(pinOption));
    } else {
      messageData.setPinOption(null);
    }
  }

  /**
   * 获取消息的pin操作者
   *
   * @return pin操作者的accid
   */
  public String getPinAccid() {
    return messageData.getPinOption() == null ? null : messageData.getPinOption().getOperatorId();
  }

  public boolean isSameMessage(ChatMessageBean bean) {
    if (bean == null) {
      return false;
    }
    return this.messageData != null
        && bean.messageData != null
        && this.messageData.getMessage().getMessageType()
            == bean.messageData.getMessage().getMessageType()
        && this.messageData
            .getMessage()
            .getMessageClientId()
            .equals(bean.messageData.getMessage().getMessageClientId());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ChatMessageBean that = (ChatMessageBean) o;
    return viewType == that.viewType && Objects.equals(messageData, that.messageData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageData, viewType);
  }
}
