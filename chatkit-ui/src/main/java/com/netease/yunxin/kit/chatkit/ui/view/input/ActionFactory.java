// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.input;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import java.util.ArrayList;
import java.util.List;

public class ActionFactory {

  public static List<ActionItem> assembleDefaultInputActions() {
    ArrayList<ActionItem> actions = new ArrayList<>();
    // voice
    actions.add(
        new ActionItem(ActionConstants.ACTION_TYPE_RECORD, R.drawable.ic_send_voice_selector));
    // emoji
    actions.add(
        new ActionItem(ActionConstants.ACTION_TYPE_EMOJI, R.drawable.ic_send_emoji_selector));
    // image
    actions.add(new ActionItem(ActionConstants.ACTION_TYPE_ALBUM, R.drawable.ic_send_image));
    // more+
    actions.add(new ActionItem(ActionConstants.ACTION_TYPE_MORE, R.drawable.ic_more_selector));

    if (ChatKitClient.getChatUIConfig() != null
        && ChatKitClient.getChatUIConfig().chatInputMenu != null) {
      return ChatKitClient.getChatUIConfig().chatInputMenu.customizeInputBar(actions);
    }
    return actions;
  }

  public static List<ActionItem> assembleInputMoreActions(SessionTypeEnum sessionType) {
    ArrayList<ActionItem> actions = new ArrayList<>();
    actions.add(
        new ActionItem(
            ActionConstants.ACTION_TYPE_CAMERA,
            R.drawable.ic_shoot,
            R.string.chat_message_more_shoot));
    actions.add(
        new ActionItem(
            ActionConstants.ACTION_TYPE_LOCATION,
            R.drawable.ic_location,
            R.string.chat_message_location));
    actions.add(
        new ActionItem(
            ActionConstants.ACTION_TYPE_FILE, R.drawable.ic_send_file, R.string.chat_message_file));
    if (sessionType == SessionTypeEnum.P2P) {
      actions.add(
          new ActionItem(
              ActionConstants.ACTION_TYPE_VIDEO_CALL,
              R.drawable.ic_video_call,
              R.string.chat_message_video_call));
    }
    if (ChatKitClient.getChatUIConfig() != null
        && ChatKitClient.getChatUIConfig().chatInputMenu != null) {
      return ChatKitClient.getChatUIConfig().chatInputMenu.customizeInputMore(actions);
    }
    return actions;
  }

  public static ArrayList<ActionItem> assembleTakeShootActions() {
    ArrayList<ActionItem> actions = new ArrayList<>();
    actions.add(
        new ActionItem(ActionConstants.ACTION_TYPE_TAKE_PHOTO, 0, R.string.chat_message_take_photo)
            .setTitleColorResId(R.color.color_333333));
    actions.add(
        new ActionItem(ActionConstants.ACTION_TYPE_TAKE_VIDEO, 0, R.string.chat_message_take_video)
            .setTitleColorResId(R.color.color_333333));
    return actions;
  }

  public static ArrayList<ActionItem> assembleVideoCallActions() {
    ArrayList<ActionItem> actions = new ArrayList<>();
    actions.add(
        new ActionItem(
                ActionConstants.ACTION_TYPE_VIDEO_CALL_ACTION,
                0,
                R.string.chat_message_video_call_action)
            .setTitleColorResId(R.color.color_333333));
    actions.add(
        new ActionItem(
                ActionConstants.ACTION_TYPE_AUDIO_CALL_ACTION,
                0,
                R.string.chat_message_audio_call_action)
            .setTitleColorResId(R.color.color_333333));
    return actions;
  }
}
