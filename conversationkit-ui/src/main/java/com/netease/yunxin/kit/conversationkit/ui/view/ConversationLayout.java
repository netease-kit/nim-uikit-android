// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.ui.widgets.TitleBarView;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationViewLayoutBinding;

/** 会话列表布局，整个页面的布局都封装在该View中 包括标题栏、会话列表、底部布局等 */
public class ConversationLayout extends LinearLayout {

  private ConversationViewLayoutBinding viewBinding;

  public ConversationLayout(Context context) {
    super(context);
    init(null);
  }

  public ConversationLayout(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public ConversationLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  public ConversationLayout(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    LayoutInflater layoutInflater = LayoutInflater.from(getContext());
    viewBinding = ConversationViewLayoutBinding.inflate(layoutInflater, this);
  }

  // 获取标题栏
  public TitleBarView getTitleBar() {
    return viewBinding.conversationTitleBar;
  }

  // 获取顶部布局
  public LinearLayout getTopLayout() {
    return viewBinding.conversationTopLayout;
  }
  // 获取会话列表布局
  public LinearLayout getBodyLayout() {
    return viewBinding.conversationBodyLayout;
  }

  // 获取会话列表
  public ConversationView getConversationView() {
    return viewBinding.conversationView;
  }
  // 获取底部布局，目前组件中没有使用，提供给开发者扩展使用。在整个会话页面最底部增加一些View的时候使用
  public FrameLayout getBottomLayout() {
    return viewBinding.conversationBottomLayout;
  }

  // 获取会话列表的顶部布局，目前组件中没有使用，提供给开发者扩展使用。如果需要再会话列表顶部增加一些View，可以在这个布局中添加
  public FrameLayout getBodyTopLayout() {
    return viewBinding.conversationBodyTopLayout;
  }

  // 获取展示错误信息的TextView，目前用于展示网络错误信息
  public TextView getErrorTextView() {
    return viewBinding.conversationNetworkErrorTv;
  }

  // 设置是否展示空数据的View，目前用于展示会话列表为空的情况
  public void setEmptyViewVisible(int visible) {
    viewBinding.conversationEmptyView.setVisibility(visible);
  }
}
