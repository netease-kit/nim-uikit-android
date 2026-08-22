// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageFileAttachment;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.sdk.v2.topic.V2NIMTopic;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.corekit.im2.utils.TimeFormatLocalUtils;
import java.util.Locale;

public final class BotSubSessionUtils {

  private static final int MAX_TITLE_LENGTH = 20;
  private static final int MAX_SUMMARY_LENGTH = 30;

  private BotSubSessionUtils() {}

  public static String getTopicTitle(Context context, V2NIMTopic topic) {
    return getTopicTitle(context, topic, null);
  }

  public static String getTopicTitle(
      Context context, V2NIMTopic topic, @Nullable String fallbackTitle) {
    if (topic == null || TextUtils.isEmpty(topic.getTopicName())) {
      return TextUtils.isEmpty(fallbackTitle)
          ? context.getString(R.string.chat_bot_sub_session_untitled)
          : fallbackTitle;
    }
    return topic.getTopicName();
  }

  public static String buildAutoTopicName(Context context, String content) {
    String title = content == null ? "" : content.trim().replaceAll("\\s+", " ");
    if (TextUtils.isEmpty(title)) {
      return context.getString(R.string.chat_bot_sub_session_untitled);
    }
    return title.length() > MAX_TITLE_LENGTH ? title.substring(0, MAX_TITLE_LENGTH) : title;
  }

  public static String buildAutoTopicName(Context context, V2NIMMessage message) {
    if (message == null) {
      return context.getString(R.string.chat_bot_sub_session_untitled);
    }
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
      return buildAutoTopicName(context, message.getText());
    }
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      RichTextAttachment attachment = MessageHelper.isRichTextMsg(message);
      if (attachment != null && !TextUtils.isEmpty(attachment.getContent())) {
        return buildAutoTopicName(context, attachment.getContent());
      }
    }
    String typeText;
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE) {
      typeText = context.getString(R.string.chat_bot_sub_session_auto_title_image);
    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO) {
      typeText = context.getString(R.string.chat_bot_sub_session_auto_title_audio);
    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO) {
      typeText = context.getString(R.string.chat_bot_sub_session_auto_title_video);
    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE) {
      typeText = context.getString(R.string.chat_bot_sub_session_auto_title_file);
    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_LOCATION) {
      typeText = context.getString(R.string.chat_bot_sub_session_auto_title_location);
    } else {
      typeText = context.getString(R.string.chat_bot_sub_session_auto_title_message);
    }
    return typeText;
  }

  public static String getMessageSummary(Context context, V2NIMMessage message) {
    if (message == null) {
      return context.getString(R.string.chat_bot_sub_session_no_message);
    }
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
      String text = message.getText();
      if (TextUtils.isEmpty(text)) {
        return context.getString(R.string.chat_bot_sub_session_no_message);
      }
      return text.length() > MAX_SUMMARY_LENGTH ? text.substring(0, MAX_SUMMARY_LENGTH) : text;
    }
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      RichTextAttachment attachment = MessageHelper.isRichTextMsg(message);
      if (attachment != null && !TextUtils.isEmpty(attachment.getContent())) {
        String content = attachment.getContent();
        return content.length() > MAX_SUMMARY_LENGTH
            ? content.substring(0, MAX_SUMMARY_LENGTH)
            : content;
      }
    }
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE) {
      return context.getString(R.string.chat_bot_sub_session_summary_image);
    }
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO) {
      return context.getString(R.string.chat_bot_sub_session_summary_audio);
    }
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO) {
      return context.getString(R.string.chat_bot_sub_session_summary_video);
    }
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE) {
      String fileName = "";
      if (message.getAttachment() instanceof V2NIMMessageFileAttachment) {
        fileName = ((V2NIMMessageFileAttachment) message.getAttachment()).getName();
      }
      return TextUtils.isEmpty(fileName)
          ? context.getString(R.string.chat_bot_sub_session_summary_file)
          : context.getString(R.string.chat_bot_sub_session_summary_file_with_name, fileName);
    }
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_LOCATION) {
      return context.getString(R.string.chat_message_location);
    }
    return context.getString(R.string.chat_bot_sub_session_summary_message);
  }

  public static String formatTime(Context context, long time) {
    if (time <= 0) {
      return "";
    }
    return TimeFormatLocalUtils.formatMillisecond(context, time, Locale.getDefault());
  }

  public static int getColorIndex(V2NIMTopic topic) {
    if (topic == null) {
      return 0;
    }
    String id =
        topic.getTopicId() > 0
            ? String.valueOf(topic.getTopicId())
            : String.valueOf(topic.getCreateTime());
    return Math.abs(id.hashCode()) % 7;
  }
}
