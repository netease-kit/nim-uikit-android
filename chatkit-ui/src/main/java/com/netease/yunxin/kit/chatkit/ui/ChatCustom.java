// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.common.TeamNotificationHelper;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;

public class ChatCustom {

  /**
   * 获取消息的简要信息，回复消息是展示的被回复内容
   *
   * @param messageInfo
   * @return
   */
  public String getReplyMsgBrief(IMMessageInfo messageInfo) {

    IMMessage msg = messageInfo.getMessage();
    switch (msg.getMsgType()) {
      case avchat:
        return IMKitClient.getApplicationContext().getString(R.string.chat_reply_message_call);
      case image:
        return IMKitClient.getApplicationContext()
            .getString(R.string.chat_reply_message_brief_image);
      case video:
        return IMKitClient.getApplicationContext()
            .getString(R.string.chat_reply_message_brief_video);
      case audio:
        return IMKitClient.getApplicationContext()
            .getString(R.string.chat_reply_message_brief_audio);
      case location:
        return IMKitClient.getApplicationContext()
            .getString(R.string.chat_reply_message_brief_location);
      case file:
        return IMKitClient.getApplicationContext()
            .getString(R.string.chat_reply_message_brief_file);
      case notification:
        return TeamNotificationHelper.getTeamNotificationText(messageInfo);
      case robot:
        return IMKitClient.getApplicationContext()
            .getString(R.string.chat_reply_message_brief_robot);
      case custom:
        if (msg.getAttachment() instanceof CustomAttachment) {
          return ((CustomAttachment) msg.getAttachment()).getContent();
        } else {
          return msg.getContent();
        }
      default:
        return msg.getContent();
    }
  }
}
