// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import com.netease.nimlib.sdk.msg.attachment.NetCallAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.corekit.im.model.AttachmentContent;

/** 获取消息的简要信息，合并转发中展示的消息内容 */
public class ChatBriefUtils {

  public static String customContentText(Context context, IMMessageInfo messageInfo) {
    if (messageInfo != null && context != null) {
      MsgTypeEnum typeEnum = messageInfo.getMessage().getMsgType();
      switch (typeEnum) {
        case notification:
          return context.getString(R.string.msg_type_notification);
        case text:
          return messageInfo.getMessage().getContent();
        case audio:
          return context.getString(R.string.msg_type_audio);
        case video:
          return context.getString(R.string.msg_type_video);
        case tip:
          return context.getString(R.string.msg_type_tip);
        case image:
          return context.getString(R.string.msg_type_image);
        case file:
          return context.getString(R.string.msg_type_file);
        case location:
          return context.getString(R.string.msg_type_location);
        case nrtc_netcall:
          NetCallAttachment attachment =
              (NetCallAttachment) messageInfo.getMessage().getAttachment();
          int type = attachment.getType();
          if (type == 1) {
            return context.getString(R.string.msg_type_rtc_audio);
          } else {
            return context.getString(R.string.msg_type_rtc_video);
          }
        case custom:
          String result = messageInfo.getMessage().getContent();
          if (messageInfo.getMessage().getAttachment() instanceof AttachmentContent) {
            result = ((AttachmentContent) messageInfo.getMessage().getAttachment()).getContent();
          }
          return result;
        default:
          return context.getString(R.string.msg_type_no_tips);
      }
    }
    return "";
  }
}
