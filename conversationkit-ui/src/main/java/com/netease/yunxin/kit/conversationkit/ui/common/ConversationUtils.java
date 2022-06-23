/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.conversationkit.ui.common;

import android.text.TextUtils;

import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.corekit.im.IMKitClient;

public class ConversationUtils {

    public static boolean isMineLeave(ConversationInfo conversationInfo) {
        if (conversationInfo.getAttachment() instanceof NotificationAttachment) {
            NotificationAttachment notify = (NotificationAttachment) conversationInfo.getAttachment();
            if (notify.getType() == NotificationType.DismissTeam
                    || notify.getType() == NotificationType.KickMember
                    || (notify.getType() == NotificationType.LeaveTeam &&
                    TextUtils.equals(conversationInfo.getFromAccount(), IMKitClient.account()))) {
                return true;
            }
        }

        return false;
    }
}
