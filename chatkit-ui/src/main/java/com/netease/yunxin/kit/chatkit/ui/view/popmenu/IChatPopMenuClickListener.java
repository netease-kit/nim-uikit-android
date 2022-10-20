// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.popmenu;

import android.view.View;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/** message long click menu event listener */
public interface IChatPopMenuClickListener {
  default boolean onCopy(ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onReply(ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onForward(ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onSignal(ChatMessageBean messageInfo, boolean isCancel) {
    return false;
  }

  default boolean onMultiSelected(ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onCollection(ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onDelete(ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onRecall(ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onCustom(View view, ChatMessageBean messageInfo, String action) {
    return false;
  }
}
