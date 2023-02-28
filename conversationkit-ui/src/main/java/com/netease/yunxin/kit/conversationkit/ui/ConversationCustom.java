// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui;

import android.content.Context;
import com.netease.nimlib.sdk.msg.attachment.NetCallAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.corekit.im.model.AttachmentContent;

public class ConversationCustom {

  public String customContentText(Context context, ConversationInfo conversationInfo) {
    if (conversationInfo != null && context != null) {
      MsgTypeEnum typeEnum = conversationInfo.getMsgType();
      switch (typeEnum) {
        case notification:
          return context.getString(R.string.msg_type_notification);
        case text:
          return conversationInfo.getContent();
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
          NetCallAttachment attachment = (NetCallAttachment) conversationInfo.getAttachment();
          int type = attachment.getType();
          if (type == 1) {
            return context.getString(R.string.msg_type_rtc_audio);
          } else {
            return context.getString(R.string.msg_type_rtc_video);
          }
        case custom:
          String result = conversationInfo.getContent();
          if (conversationInfo.getAttachment() instanceof AttachmentContent) {
            result = ((AttachmentContent) conversationInfo.getAttachment()).getContent();
          }
          return result;
        default:
          return context.getString(R.string.msg_type_no_tips);
      }
    }
    return "";
  }
}
