// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.interfaces;

import android.view.View;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/** Pin列表界面的点击接口 */
public interface IChatClickListener {

  default boolean onMessageClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  default boolean onViewClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }
}
