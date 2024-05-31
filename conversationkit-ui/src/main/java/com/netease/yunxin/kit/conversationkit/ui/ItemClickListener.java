// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui;

import android.content.Context;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;

/** 会话列表Item点击事件监听 */
public interface ItemClickListener {
  //会话Item点击事件
  default boolean onClick(Context context, ConversationBean data, int position) {
    return false;
  }

  //会话Item长按事件
  default boolean onLongClick(Context context, ConversationBean data, int position) {
    return false;
  }

  //会话Item头像点击事件
  default boolean onAvatarClick(Context context, ConversationBean data, int position) {
    return false;
  }

  //会话Item头像长按事件
  default boolean onAvatarLongClick(Context context, ConversationBean data, int position) {
    return false;
  }
}
