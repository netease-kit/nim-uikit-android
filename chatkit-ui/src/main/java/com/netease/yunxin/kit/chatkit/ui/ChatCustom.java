// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageAttachment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.CustomAttachment;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.common.TeamNotificationHelper;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import org.json.JSONObject;

public class ChatCustom {

  /**
   * 获取消息的简要信息， 1,回复消息是展示的被回复内容 2,置顶消息展示的置顶内容
   *
   * @param messageInfo 消息
   * @return 会话列表中展示的最近信息内容
   */
  public String getMessageBrief(IMMessageInfo messageInfo, boolean showLocationDetail) {

    V2NIMMessage msg = messageInfo.getMessage();
    switch (msg.getMessageType()) {
      case V2NIM_MESSAGE_TYPE_AVCHAT:
        int type = getMessageCallType(messageInfo.getMessage().getAttachment());
        if (type == 1) {
          return IMKitClient.getApplicationContext().getString(R.string.msg_type_rtc_audio);
        } else {
          return IMKitClient.getApplicationContext().getString(R.string.msg_type_rtc_video);
        }
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
        if (showLocationDetail) {
          return IMKitClient.getApplicationContext()
                  .getString(R.string.chat_reply_message_brief_location)
              + messageInfo.getMessage().getText();

        } else {
          return IMKitClient.getApplicationContext()
              .getString(R.string.chat_reply_message_brief_location);
        }
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

  /**
   * 获取话单消息的通话类型
   *
   * @param attachment 消息附件
   * @return 通话类型
   */
  public static int getMessageCallType(V2NIMMessageAttachment attachment) {
    // 此处只处理话单消息
    int callType = 0;
    if (attachment == null) {
      return callType;
    }
    String attachmentStr = attachment.getRaw();
    ALog.d(LIB_TAG, "ChatBriefUtils", "getMessageCallType: ");
    if (!TextUtils.isEmpty(attachmentStr)) {
      try {
        JSONObject dataJson = new JSONObject(attachmentStr);
        // 音频/视频 类型通话
        callType = dataJson.getInt("type");
      } catch (Exception e) {
        ALog.e(LIB_TAG, "ChatBriefUtils", "getMessageCallType: " + callType);
      }
    }
    return callType;
  }
}
