// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_REVOKE_EDIT_TAG;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_REVOKE_TAG;

import com.netease.nimlib.sdk.v2.message.V2NIMMessagePin;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
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
    Map<String, Object> localExtensionMap =
        MessageExtensionHelper.parseJsonStringToMap(messageData.getMessage().getLocalExtension());

    if (localExtensionMap != null && localExtensionMap.containsKey(KEY_REVOKE_TAG)) {
      Object revokeLocal = localExtensionMap.get(KEY_REVOKE_TAG);
      Object revokeEdit = localExtensionMap.get(KEY_REVOKE_EDIT_TAG);
      if (revokeLocal instanceof Boolean) {
        isRevoked = (Boolean) revokeLocal;
      }

      if (revokeEdit instanceof Boolean) {
        revokeMsgEdit = (Boolean) revokeEdit;
      }
    }
  }

  IMMessageInfo messageData;

  //默认-1，表示未知
  int viewType = V2NIMMessageType.V2NIM_MESSAGE_TYPE_INVALID.getValue();

  boolean haveRead;

  float loadProgress;

  boolean isRevoked;

  public boolean revokeMsgEdit = true;

  // 上传进度 % 0-100
  public int progress;

  public IMMessageInfo getMessageData() {
    return messageData;
  }

  public String getSenderId() {
    return messageData == null ? "" : messageData.getMessage().getSenderId();
  }

  public String getMsgClientId() {
    return messageData == null ? "" : messageData.getMessage().getMessageClientId();
  }

  public void setMessageData(IMMessageInfo messageData) {
    this.messageData = messageData;
  }

  public boolean hasReply() {
    if (messageData == null) {
      return false;
    }
    Map<String, Object> serverExtensionMap =
        MessageExtensionHelper.parseJsonStringToMap(messageData.getMessage().getServerExtension());
    return serverExtensionMap != null
        && serverExtensionMap.containsKey(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY);
  }

  public String getReplyUUid() {
    if (messageData == null) {
      return null;
    }
    Map<String, Object> serverExtensionMap =
        MessageExtensionHelper.parseJsonStringToMap(messageData.getMessage().getServerExtension());

    if (serverExtensionMap != null
        && serverExtensionMap.containsKey(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY)) {
      Object replyInfo = serverExtensionMap.get(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY);
      if (replyInfo instanceof Map) {
        try {
          Map<String, Object> replyMap = (Map<String, Object>) replyInfo;
          if (replyMap.containsKey(ChatKitUIConstant.REPLY_UUID_KEY)) {
            Object uuid = replyMap.get(ChatKitUIConstant.REPLY_UUID_KEY);
            if (uuid != null) {
              return uuid.toString();
            }
          }
        } catch (Exception e) {
          ALog.e(
              LIB_TAG,
              "V2ChatMessageBean",
              "getReplyUUid,error message"
                  + (messageData == null ? "null" : messageData.getMessage().getMessageClientId()));
        }
      }
    }
    return null;
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
