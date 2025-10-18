// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import java.util.ArrayList;

public class ChatDialogUtils {

  // 组装更多操作弹窗按钮
  public static ArrayList<ActionItem> assembleMessageTelActions() {
    ArrayList<ActionItem> actions = new ArrayList<>();
    actions.add(
        new ActionItem(ActionConstants.POP_ACTION_TEL, 0, R.string.chat_message_action_tel)
            .setTitleColorResId(R.color.color_333333));
    actions.add(
        new ActionItem(ActionConstants.POP_ACTION_COPY, 0, R.string.chat_message_action_copy)
            .setTitleColorResId(R.color.color_333333));
    return actions;
  }
}
