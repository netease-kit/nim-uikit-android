// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.interfaces;

import android.view.View;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/** Message item click event listener */
public interface IMessageItemClickListener {
  default boolean onMessageLongClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onMessageClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onMessageSelect(
      View view, int position, ChatMessageBean messageInfo, boolean selected) {
    return false;
  }

  default boolean onUserIconClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onSelfIconClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onSelfIconLongClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onUserIconLongClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onReeditRevokeMessage(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onReplyMessageClick(View view, int position, IMMessageInfo messageInfo) {
    return false;
  }

  default boolean onSendFailBtnClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onTextSelected(
      View view, int position, ChatMessageBean messageInfo, String text, boolean isSelectAll) {
    return false;
  }

  default boolean onCustomClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onAIMessageRefresh(View view, int position, IMMessageInfo messageInfo) {
    return false;
  }

  default boolean onAIMessageStreamStop(View view, int position, IMMessageInfo messageInfo) {
    return false;
  }
}
