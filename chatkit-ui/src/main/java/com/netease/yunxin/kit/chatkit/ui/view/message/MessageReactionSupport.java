// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageSendingState;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/** Centralizes the message-type and state policy for message Reactions. */
public final class MessageReactionSupport {

  private MessageReactionSupport() {}

  public static boolean canOperate(@Nullable ChatMessageBean messageBean) {
    if (!isSupportedType(messageBean) || messageBean.isRevoked() || messageBean.hasErrorCode()) {
      return false;
    }
    V2NIMMessage message = messageBean.getMessage();
    return message != null
        && message.getSendingState()
            == V2NIMMessageSendingState.V2NIM_MESSAGE_SENDING_STATE_SUCCEEDED;
  }

  public static boolean canQuery(@Nullable ChatMessageBean messageBean) {
    if (!canOperate(messageBean)) {
      return false;
    }
    V2NIMMessage message = messageBean.getMessage();
    return message != null && !TextUtils.isEmpty(message.getMessageClientId());
  }

  private static boolean isSupportedType(@Nullable ChatMessageBean messageBean) {
    if (messageBean == null || messageBean.getMessageData() == null) {
      return false;
    }
    V2NIMMessage message = messageBean.getMessage();
    if (message == null) {
      return false;
    }
    int viewType = messageBean.getViewType();
    return viewType == ChatMessageType.TEXT_MESSAGE_VIEW_TYPE
        || viewType == ChatMessageType.IMAGE_MESSAGE_VIEW_TYPE
        || viewType == ChatMessageType.VIDEO_MESSAGE_VIEW_TYPE
        || viewType == ChatMessageType.FILE_MESSAGE_VIEW_TYPE
        || viewType == ChatMessageType.AUDIO_MESSAGE_VIEW_TYPE
        || viewType == ChatMessageType.LOCATION_MESSAGE_VIEW_TYPE
        || viewType == ChatMessageType.CALL_MESSAGE_VIEW_TYPE
        || viewType == ChatMessageType.RICH_TEXT_ATTACHMENT
        || viewType == ChatMessageType.MULTI_FORWARD_ATTACHMENT;
  }
}
