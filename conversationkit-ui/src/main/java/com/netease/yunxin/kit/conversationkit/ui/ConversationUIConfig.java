// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui;

import android.graphics.drawable.Drawable;
import android.view.View;
import com.netease.yunxin.kit.chatkit.model.ConversationInfo;
import java.util.Comparator;

public class ConversationUIConfig {

  public static Integer INT_DEFAULT_NULL = 0;
  public boolean showTitleBar = true;
  public boolean showTitleBarLeftIcon = true;
  public boolean showTitleBarRightIcon = true;
  public boolean showTitleBarRight2Icon = true;

  public Integer titleBarLeftRes = null;
  public Integer titleBarRightRes = null;
  public Integer titleBarRight2Res = null;

  public String titleBarTitle;
  public Integer titleBarTitleColor = null;

  public Integer itemTitleColor = null;
  public Integer itemTitleSize = null;
  public Integer itemContentColor = null;
  public Integer itemContentSize = null;
  public Integer itemDateColor = null;
  public Integer itemDateSize = null;

  public View.OnClickListener titleBarRightClick;

  public View.OnClickListener titleBarRight2Click;

  public View.OnClickListener titleBarLeftClick;
  public ItemClickListener itemClickListener;
  public Comparator<ConversationInfo> conversationComparator;
  public IConversationFactory conversationFactory;
  public Float avatarCornerRadius = null;
  public Drawable itemStickTopBackground;
  public Drawable itemBackground;
  public ConversationCustom conversationCustom;
  public IConversationViewLayout customLayout;
}
