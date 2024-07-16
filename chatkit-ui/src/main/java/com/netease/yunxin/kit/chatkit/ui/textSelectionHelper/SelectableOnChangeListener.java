// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.textSelectionHelper;

import android.view.View;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/** 选择文本信息回调 */
public interface SelectableOnChangeListener {
  /**
   * 选择文本变化
   *
   * @param view 视图
   * @param position 位置
   * @param message 消息
   * @param text 选择文本
   * @param isSelectAll 是否全选
   */
  void onChange(
      View view, int position, ChatMessageBean message, CharSequence text, boolean isSelectAll);
}
