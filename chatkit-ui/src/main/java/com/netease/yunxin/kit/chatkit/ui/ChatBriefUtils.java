// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Context;
import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageAttachment;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import org.json.JSONObject;

/** 获取消息的简要信息，合并转发中展示的消息内容 */
public class ChatBriefUtils {

  public static String customContentText(Context context, IMMessageInfo messageInfo) {
    if (messageInfo != null && context != null) {
      V2NIMMessageType typeEnum = messageInfo.getMessage().getMessageType();
      switch (typeEnum) {
        case V2NIM_MESSAGE_TYPE_NOTIFICATION:
          return context.getString(R.string.msg_type_notification);
        case V2NIM_MESSAGE_TYPE_TEXT:
          return messageInfo.getMessage().getText();
        case V2NIM_MESSAGE_TYPE_AUDIO:
          return context.getString(R.string.msg_type_audio);
        case V2NIM_MESSAGE_TYPE_VIDEO:
          return context.getString(R.string.msg_type_video);
        case V2NIM_MESSAGE_TYPE_TIPS:
          return context.getString(R.string.msg_type_tip);
        case V2NIM_MESSAGE_TYPE_IMAGE:
          return context.getString(R.string.msg_type_image);
        case V2NIM_MESSAGE_TYPE_FILE:
          return context.getString(R.string.msg_type_file);
        case V2NIM_MESSAGE_TYPE_LOCATION:
          return context.getString(R.string.msg_type_location);
        case V2NIM_MESSAGE_TYPE_CALL:
          int type = getMessageCallType(messageInfo.getMessage().getAttachment());
          if (type == 1) {
            return context.getString(R.string.msg_type_rtc_audio);
          } else {
            return context.getString(R.string.msg_type_rtc_video);
          }
        case V2NIM_MESSAGE_TYPE_CUSTOM:
          String result = messageInfo.getMessage().getText();
          if (messageInfo.getAttachment() != null) {
            result = messageInfo.getAttachment().getContent();
          }
          return result;
        default:
          return context.getString(R.string.msg_type_no_tips);
      }
    }
    return "";
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
