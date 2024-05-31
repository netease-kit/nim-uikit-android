// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMClearHistoryNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageDeletedNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessagePinNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageQuickCommentNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMP2PMessageReadReceipt;
import com.netease.nimlib.sdk.v2.message.V2NIMTeamMessageReadReceipt;
import com.netease.yunxin.kit.chatkit.listener.ChatListener;
import com.netease.yunxin.kit.chatkit.listener.MessageRevokeNotification;
import com.netease.yunxin.kit.chatkit.listener.MessageUpdateType;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import java.util.List;

/** 消息监听实现类 */
public class MessageObserverImpl implements ChatListener {
  @Override
  public void onReceiveMessages(@NonNull List<IMMessageInfo> messages) {}

  @Override
  public void onReceiveP2PMessageReadReceipts(
      @Nullable List<? extends V2NIMP2PMessageReadReceipt> readReceipts) {}

  @Override
  public void onReceiveTeamMessageReadReceipts(
      @Nullable List<? extends V2NIMTeamMessageReadReceipt> readReceipts) {}

  @Override
  public void onMessageRevokeNotifications(
      @Nullable List<MessageRevokeNotification> revokeNotifications) {}

  @Override
  public void onMessagePinNotification(@Nullable V2NIMMessagePinNotification pinNotification) {}

  @Override
  public void onMessageQuickCommentNotification(
      @Nullable V2NIMMessageQuickCommentNotification quickCommentNotification) {}

  @Override
  public void onClearHistoryNotifications(
      @Nullable List<? extends V2NIMClearHistoryNotification> clearHistoryNotifications) {}

  @Override
  public void onMessagesUpdate(
      @NonNull List<IMMessageInfo> messages, @NonNull MessageUpdateType type) {}

  @Override
  public void onMessageAttachmentDownloadProgress(@NonNull V2NIMMessage message, int progress) {}

  @Override
  public void onMessageDeletedNotifications(
      @NonNull List<? extends V2NIMMessageDeletedNotification> messages) {}

  @Override
  public void onSendMessage(@NonNull V2NIMMessage message) {}

  @Override
  public void onSendMessageFailed(
      int errorCode,
      @NonNull String errorMsg,
      @NonNull String conversationId,
      @NonNull V2NIMConversationType conversationType,
      @Nullable V2NIMMessage data) {}
}
