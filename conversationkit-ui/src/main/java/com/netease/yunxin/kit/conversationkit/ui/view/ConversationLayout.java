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

  public TitleBarView getTitleBar() {
    return viewBinding.conversationTitleBar;
  }

  public LinearLayout getTopLayout() {
    return viewBinding.conversationTopLayout;
  }

  public LinearLayout getBodyLayout() {
    return viewBinding.conversationBodyLayout;
  }

  public ConversationView getConversationView() {
    return viewBinding.conversationView;
  }

  public FrameLayout getBottomLayout() {
    return viewBinding.conversationBottomLayout;
  }

  public FrameLayout getBodyTopLayout() {
    return viewBinding.conversationBodyTopLayout;
  }

  public TextView getErrorTextView() {
    return viewBinding.conversationNetworkErrorTv;
  }
}
