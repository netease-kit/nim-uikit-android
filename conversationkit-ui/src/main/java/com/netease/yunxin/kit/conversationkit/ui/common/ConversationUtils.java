// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.common;

import android.content.Context;
import android.text.TextUtils;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.AttachmentContent;

public class ConversationUtils {

  public static boolean isMineLeave(ConversationInfo conversationInfo) {
    if (conversationInfo.getAttachment() instanceof NotificationAttachment) {
      NotificationAttachment notify = (NotificationAttachment) conversationInfo.getAttachment();
      if (notify.getType() == NotificationType.DismissTeam
          || notify.getType() == NotificationType.KickMember
          || (notify.getType() == NotificationType.LeaveTeam
              && TextUtils.equals(conversationInfo.getFromAccount(), IMKitClient.account()))) {
        return true;
      }
    }

    return false;
  }

  public static String getConversationText(Context context, ConversationInfo conversationInfo) {
    if (conversationInfo != null && context != null) {
      MsgTypeEnum typeEnum = conversationInfo.getMsgType();
      switch (typeEnum) {
        case notification:
          return context.getString(R.string.msg_type_notification);
          //        case file:
          //          return context.getString(R.string.msg_type_file);
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
