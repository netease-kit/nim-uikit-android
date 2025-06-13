// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.factory;

import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import java.util.ArrayList;
import java.util.List;

public class FunBottomActionFactory {

  public static List<ActionItem> assembleInputMoreActions(
      String accountId, V2NIMConversationType conversationType) {
    ArrayList<ActionItem> actions = new ArrayList<>();
    actions.add(
        new ActionItem(
            ActionConstants.ACTION_TYPE_ALBUM,
            R.drawable.fun_ic_chat_input_more_album,
            R.string.fun_chat_input_more_album_title));
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
    if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P
        && !AIUserManager.isAIUser(accountId)) {
      actions.add(
          new ActionItem(
              ActionConstants.ACTION_TYPE_VIDEO_CALL,
              R.drawable.ic_video_call,
              R.string.chat_message_video_call));
    }
    if (IMKitConfigCenter.getEnableAIUser() && AIUserManager.getAITranslateUser() != null) {
      actions.add(
          new ActionItem(
              ActionConstants.ACTION_TYPE_TRANSLATE,
              R.drawable.ic_chat_translate_action,
              R.string.chat_message_translate_action));
    }
    if (IMKitConfigCenter.getEnableAIChatHelper()
        && conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P
        && !AIUserManager.isAIUser(accountId)) {
      actions.add(
          new ActionItem(
              ActionConstants.ACTION_TYPE_AI_HELPER,
              R.drawable.fun_ic_chat_input_ai_helper,
              R.string.chat_message_ai_helper_action));
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
