// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.input;

import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.qchatkit.ui.R;
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

    return actions;
  }

  public static List<ActionItem> assembleInputMoreActions() {
    ArrayList<ActionItem> actions = new ArrayList<>();
    return actions;
  }

  public static ArrayList<ActionItem> assembleTakeShootActions() {
    ArrayList<ActionItem> actions = new ArrayList<>();

    return actions;
  }

  public static ArrayList<ActionItem> assembleVideoCallActions() {
    ArrayList<ActionItem> actions = new ArrayList<>();

    return actions;
  }
}
