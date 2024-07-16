// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageCreator;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageAudioAttachment;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageFileAttachment;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageImageAttachment;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageLocationAttachment;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageVideoAttachment;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;

public class MessageCreator {

  //创建新的消息，只要消息体内容，其他都不需要
  // 主要用于收藏消息时，只保留内容即可
  public static V2NIMMessage createMessage(V2NIMMessage message) {
    if (message == null) {
      return null;
    }
    V2NIMMessage result = null;
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
      result = V2NIMMessageCreator.createTextMessage(message.getText());
    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE) {
      V2NIMMessageImageAttachment attachment =
          (V2NIMMessageImageAttachment) message.getAttachment();
      if (attachment != null) {
        String path = attachment.getPath();
        if (TextUtils.isEmpty(path)) {
          path = attachment.getUrl();
        }
        result =
            V2NIMMessageCreator.createImageMessage(
                path,
                attachment.getName(),
                attachment.getSceneName(),
                attachment.getWidth(),
                attachment.getHeight());
        result.setAttachment(attachment);
      }

    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE) {
      V2NIMMessageFileAttachment attachment = (V2NIMMessageFileAttachment) message.getAttachment();
      if (attachment != null) {
        String path = attachment.getPath();
        if (TextUtils.isEmpty(path)) {
          path = attachment.getUrl();
        }
        result =
            V2NIMMessageCreator.createFileMessage(
                path, attachment.getName(), attachment.getSceneName());
        result.setAttachment(attachment);
      }

    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO) {
      V2NIMMessageAudioAttachment attachment =
          (V2NIMMessageAudioAttachment) message.getAttachment();
      if (attachment != null) {
        String path = attachment.getPath();
        if (TextUtils.isEmpty(path)) {
          path = attachment.getUrl();
        }
        result =
            V2NIMMessageCreator.createAudioMessage(
                path, attachment.getName(), attachment.getSceneName(), attachment.getDuration());
        result.setAttachment(attachment);
      }

    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO) {
      V2NIMMessageVideoAttachment attachment =
          (V2NIMMessageVideoAttachment) message.getAttachment();
      if (attachment != null) {
        String path = attachment.getPath();
        if (TextUtils.isEmpty(path)) {
          path = attachment.getUrl();
        }
        result =
            V2NIMMessageCreator.createVideoMessage(
                path,
                attachment.getName(),
                attachment.getSceneName(),
                attachment.getDuration(),
                attachment.getWidth(),
                attachment.getHeight());
        result.setAttachment(attachment);
      }

    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_LOCATION) {
      V2NIMMessageLocationAttachment attachment =
          (V2NIMMessageLocationAttachment) message.getAttachment();
      if (attachment != null) {
        result =
            V2NIMMessageCreator.createLocationMessage(
                attachment.getLatitude(), attachment.getLongitude(), attachment.getAddress());
        result.setAttachment(attachment);
      }

    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      result =
          V2NIMMessageCreator.createCustomMessage(
              message.getText(), message.getAttachment().getRaw());
      result.setAttachment(message.getAttachment());
    }
    if (result != null) {
      result.setText(message.getText());
    }
    return result;
  }
}
