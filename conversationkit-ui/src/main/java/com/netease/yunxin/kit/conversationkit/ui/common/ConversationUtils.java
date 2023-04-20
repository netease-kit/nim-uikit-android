// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.common;

import static com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant.LIB_TAG;

import android.content.Context;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.conversationkit.ui.ConversationCustom;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;

public class ConversationUtils {

  private static final String TAG = "ConversationUtils";

  private static ConversationCustom custom = new ConversationCustom();

  public static boolean isMineLeave(ConversationInfo conversationInfo) {
    ALog.d(LIB_TAG, TAG, "isMineLeave");
    if (conversationInfo.getAttachment() instanceof NotificationAttachment) {
      NotificationAttachment notify = (NotificationAttachment) conversationInfo.getAttachment();
      ALog.d(LIB_TAG, TAG, "isMineLeave,notificationType=" + notify.getType());
      if (notify.getType() == NotificationType.DismissTeam) {
        return true;
      }
      return (notify.getType() == NotificationType.KickMember
              || notify.getType() == NotificationType.LeaveTeam)
          && conversationInfo.getTeamInfo() != null
          && !conversationInfo.getTeamInfo().isMyTeam();
    }

    return false;
  }

  public static String getConversationText(Context context, ConversationInfo conversationInfo) {
    if (ConversationKitClient.getConversationUIConfig() != null
        && ConversationKitClient.getConversationUIConfig().conversationCustom != null) {
      return ConversationKitClient.getConversationUIConfig()
          .conversationCustom
          .customContentText(context, conversationInfo);
    }
    return custom.customContentText(context, conversationInfo);
  }
}
