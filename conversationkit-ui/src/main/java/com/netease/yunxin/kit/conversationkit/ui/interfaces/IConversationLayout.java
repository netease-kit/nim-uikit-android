// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.interfaces;

import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netease.yunxin.kit.common.ui.widgets.TitleBarView;
import com.netease.yunxin.kit.conversationkit.ui.view.ConversationView;

public interface IConversationLayout {

  public TitleBarView getTitleBar();

  public LinearLayout getTopLayout();

  public LinearLayout getBodyLayout();

  public ConversationView getConversationView();

  public FrameLayout getBottomLayout();

  public FrameLayout getBodyTopLayout();

  public TextView getErrorTextView();

  public void setEmptyViewVisible(int visible);
}
