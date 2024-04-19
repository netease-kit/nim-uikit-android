// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.yunxin.kit.chatkit.model.CustomAttachment;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.common.TeamNotificationHelper;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;

public class ChatCustom {

  /**
   * 获取消息的简要信息，回复消息是展示的被回复内容
   *
   * @param messageInfo 消息
   * @return 会话列表中展示的最近信息内容
   */
  public String getReplyMsgBrief(IMMessageInfo messageInfo) {

    V2NIMMessage msg = messageInfo.getMessage();
    switch (msg.getMessageType()) {
      case V2NIM_MESSAGE_TYPE_AVCHAT:
        return IMKitClient.getApplicationContext().getString(R.string.chat_reply_message_call);
      case V2NIM_MESSAGE_TYPE_IMAGE:
        return IMKitClient.getApplicationContext()
            .getString(R.string.chat_reply_message_brief_image);
      case V2NIM_MESSAGE_TYPE_VIDEO:
        return IMKitClient.getApplicationContext()
            .getString(R.string.chat_reply_message_brief_video);
      case V2NIM_MESSAGE_TYPE_AUDIO:
        return IMKitClient.getApplicationContext()
            .getString(R.string.chat_reply_message_brief_audio);
      case V2NIM_MESSAGE_TYPE_LOCATION:
        return IMKitClient.getApplicationContext()
            .getString(R.string.chat_reply_message_brief_location);
      case V2NIM_MESSAGE_TYPE_FILE:
        return IMKitClient.getApplicationContext()
            .getString(R.string.chat_reply_message_brief_file);
      case V2NIM_MESSAGE_TYPE_NOTIFICATION:
        return TeamNotificationHelper.getTeamNotificationText(messageInfo);
      case V2NIM_MESSAGE_TYPE_ROBOT:
        return IMKitClient.getApplicationContext()
            .getString(R.string.chat_reply_message_brief_robot);
      case V2NIM_MESSAGE_TYPE_CUSTOM:
        messageInfo.parseAttachment();
        if (messageInfo.getAttachment() != null) {
          return ((CustomAttachment) messageInfo.getAttachment()).getContent();
        } else {
          return msg.getText();
        }
      default:
        return msg.getText();
    }
  }
}
