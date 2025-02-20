// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.interfaces;

import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netease.yunxin.kit.common.ui.widgets.TitleBarView;
import com.netease.yunxin.kit.conversationkit.local.ui.view.ConversationView;

/** 会话界面布局接口 由于普通版和娱乐版底层业务逻辑通用，所以需要一个接口层进行抽象 */
public interface IConversationLayout {

  // 获取标题栏
  public TitleBarView getTitleBar();

  // 获取顶部布局，页面顶部布局，现在该布局中只包含标题栏
  public LinearLayout getTopLayout();

  // 获取中间布局
  public LinearLayout getBodyLayout();

  // 获取会话列表
  public ConversationView getConversationView();

  // 获取底部布局，目前组件中没有使用，提供给开发者扩展使用。在整个会话页面最底部增加一些View的时候使用
  public FrameLayout getBottomLayout();

  // 获取会话列表的顶部布局，目前组件中没有使用，提供给开发者扩展使用。如果需要再会话列表顶部增加一些View，可以在这个布局中添加
  public FrameLayout getBodyTopLayout();

  // 获取展示错误信息的TextView，目前用于展示网络错误信息
  public TextView getErrorTextView();

  // 获取设置是否展示空数据的View，目前用于展示会话列表为空的情况
  public void setEmptyViewVisible(int visible);
}
