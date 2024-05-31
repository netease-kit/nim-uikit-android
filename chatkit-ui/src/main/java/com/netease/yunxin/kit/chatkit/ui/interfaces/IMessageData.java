// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.interfaces;

import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessagePin;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import java.util.List;

/** Message list event */
public interface IMessageData {

  void addMessageListForward(List<ChatMessageBean> messageList);

  void appendMessageList(List<ChatMessageBean> messageList);

  void appendMessageList(List<ChatMessageBean> messageList, boolean needToScrollEnd);

  void appendMessage(ChatMessageBean message);

  void updateMessageStatus(ChatMessageBean message);

  void updateMessage(ChatMessageBean message, Object payload);

  void updateMessage(V2NIMMessage message, Object payload);

  void deleteMessage(ChatMessageBean message);

  void deleteMessage(List<ChatMessageBean> message);

  void revokeMessage(V2NIMMessageRefer message);

  void addPinMessage(String uuid, V2NIMMessagePin pinOption);

  void removePinMessage(String uuid);

  void clearMessageList();

  void setHasMoreNewerMessages(boolean hasMoreNewerMessages);

  void setHasMoreForwardMessages(boolean hasMoreForwardMessages);

  void setMultiSelect(boolean multiSelect);

  void updateMultiSelectMessage(List<ChatMessageBean> message);

  //消息UI复用，需要设置当前展示模式。0代表会话消息，1代表转发消息（合并转发详情页使用）
  void setMessageMode(int mode);

  boolean hasMoreNewerMessages();

  boolean hasMoreForwardMessages();
}
