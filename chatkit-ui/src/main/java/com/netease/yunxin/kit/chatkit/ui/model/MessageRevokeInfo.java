// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model;

import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessageRevokeNotification;

/** 消息撤回信息 */
public class MessageRevokeInfo {

  public MessageRevokeInfo(
      V2NIMMessage revokeMessage, V2NIMMessageRevokeNotification revokeNotification) {
    this.revokeMessage = revokeMessage;
    this.revokeNotification = revokeNotification;
  }

  //如果是自己撤回则有此信息
  private V2NIMMessage revokeMessage;

  //如果是别人撤回则有此信息
  private V2NIMMessageRevokeNotification revokeNotification;

  public V2NIMMessageRefer getRevokeRefer() {
    if (revokeMessage != null) {
      return revokeMessage;
    } else {
      return revokeNotification.getMessageRefer();
    }
  }

  public V2NIMMessageRevokeNotification getRevokeNotification() {
    return revokeNotification;
  }

  public void setRevokeNotification(V2NIMMessageRevokeNotification revokeNotification) {
    this.revokeNotification = revokeNotification;
  }

  public V2NIMMessage getRevokeMessage() {
    return revokeMessage;
  }

  public void setRevokeMessage(V2NIMMessage revokeMessage) {
    this.revokeMessage = revokeMessage;
  }

  public String getRevokeMessageClientId() {
    if (revokeMessage != null) {
      return revokeMessage.getMessageClientId();
    }
    if (revokeNotification != null) {
      return revokeNotification.getMessageRefer().getMessageClientId();
    }
    return "";
  }
}
