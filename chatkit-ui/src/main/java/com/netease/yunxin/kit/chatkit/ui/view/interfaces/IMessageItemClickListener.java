// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.interfaces;

import android.view.View;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/** Message item click event listener */
public interface IMessageItemClickListener {
  default boolean onMessageLongClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onMessageClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onUserIconClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onSelfIconClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onUserIconLongClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onReEditRevokeMessage(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onReplyMessageClick(View view, int position, String replyUuid) {
    return false;
  }

  default boolean onSendFailBtnClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onTextSelected(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }
}
