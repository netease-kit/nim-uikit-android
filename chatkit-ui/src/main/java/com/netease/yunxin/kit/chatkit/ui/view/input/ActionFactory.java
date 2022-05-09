/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.view.input;

import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.common.ui.action.ActionItem;

import java.util.ArrayList;


public class ActionFactory {

    public static ArrayList<ActionItem> assembleDefaultInputActions() {
        ArrayList<ActionItem> actions = new ArrayList<>();
        // voice
        actions.add(new ActionItem(ActionConstants.ACTION_TYPE_RECORD, R.drawable.ic_send_voice_selector));
        // emoji
        actions.add(new ActionItem(ActionConstants.ACTION_TYPE_EMOJI, R.drawable.ic_send_emoji_selector));
        // image
        actions.add(new ActionItem(ActionConstants.ACTION_TYPE_ALBUM, R.drawable.ic_send_image));
        // file
        actions.add(new ActionItem(ActionConstants.ACTION_TYPE_FILE, R.drawable.ic_send_file));
        // more+
        actions.add(new ActionItem(ActionConstants.ACTION_TYPE_MORE, R.drawable.ic_more_selector));
        return actions;
    }

    public static ArrayList<ActionItem> assembleInputMoreActions() {
        ArrayList<ActionItem> actions = new ArrayList<>();
        actions.add(new ActionItem(ActionConstants.ACTION_MORE_SHOOT, R.drawable.ic_shoot, R.string.chat_message_more_shoot));
        return actions;
    }

    public static ArrayList<ActionItem> assembleTakeShootActions() {
        ArrayList<ActionItem> actions = new ArrayList<>();
        actions.add(new ActionItem(ActionConstants.ACTION_TYPE_TAKE_PHOTO, 0,
                R.string.chat_message_take_photo).setTitleColorResId(R.color.color_333333));
        actions.add(new ActionItem(ActionConstants.ACTION_TYPE_TAKE_VIDEO, 0,
                R.string.chat_message_take_video).setTitleColorResId(R.color.color_333333));
        return actions;
    }
}
