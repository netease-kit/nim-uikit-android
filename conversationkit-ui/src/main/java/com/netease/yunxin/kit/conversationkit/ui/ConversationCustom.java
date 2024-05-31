// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui;

import android.content.Context;
import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMLastMessageState;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.model.CustomAttachment;
import com.netease.yunxin.kit.chatkit.utils.ChatUtils;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationUtils;

/** 会话自定义配置 用于外部定制替换 */
public class ConversationCustom {

  // 获取会话最近一条消息的内容展示
  public String customContentText(Context context, V2NIMConversation conversationInfo) {
    if (conversationInfo != null && context != null && conversationInfo.getLastMessage() != null) {
      V2NIMMessageType typeEnum = conversationInfo.getLastMessage().getMessageType();
      switch (typeEnum) {
        case V2NIM_MESSAGE_TYPE_NOTIFICATION:
          return context.getString(R.string.msg_type_notification);
        case V2NIM_MESSAGE_TYPE_TEXT:
          return conversationInfo.getLastMessage().getText();
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
          String title = conversationInfo.getLastMessage().getText();
          return context.getString(R.string.msg_type_location) + title;
        case V2NIM_MESSAGE_TYPE_CALL:
          int type =
              ConversationUtils.getMessageCallType(
                  conversationInfo.getLastMessage().getAttachment());
          if (type == 1) {
            return context.getString(R.string.msg_type_rtc_audio);
          } else {
            return context.getString(R.string.msg_type_rtc_video);
          }
        case V2NIM_MESSAGE_TYPE_CUSTOM:
          String result = "";
          // 自定义消息解析，可以通过ChatKitClient.addCustomAttach添加自定义消息Attachment
          // 也可以使用，ChatKit-ui 中通过配置ChatKitConfig.customParse实现自定义消息解析
          CustomAttachment attachment =
              ChatUtils.parseLastMsgCustomAttachment(conversationInfo.getLastMessage());
          if (attachment != null) {
            result = attachment.getContent();
          }
          if (TextUtils.isEmpty(result)) {
            result = context.getString(R.string.msg_type_no_tips);
          }
          return result;
        default:
          String defaultText = "";
          if (conversationInfo.getLastMessage().getLastMessageState()
              == V2NIMLastMessageState.V2NIM_MESSAGE_STATE_REVOKED) {
            defaultText = context.getString(R.string.msg_type_revoke_tips);
          }
          if (TextUtils.isEmpty(defaultText)) {
            defaultText = context.getString(R.string.msg_type_no_tips);
          }
          return defaultText;
      }
    }
    return "";
  }
}
