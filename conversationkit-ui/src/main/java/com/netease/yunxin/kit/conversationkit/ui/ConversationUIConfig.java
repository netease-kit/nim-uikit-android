// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui;

import android.graphics.drawable.Drawable;
import android.view.View;

/** 会话UI配置 */
public class ConversationUIConfig {

  // 默认值，如果是该值，则任务该字段未设置
  public static Integer INT_DEFAULT_NULL = 0;
  // 是否展示会话列表标题栏
  public boolean showTitleBar = true;
  // 是否展示会话列表标题栏左侧图标
  public boolean showTitleBarLeftIcon = true;
  // 是否展示会话列表标题栏右侧图标
  public boolean showTitleBarRightIcon = true;
  // 是否展示会话列表标题栏右侧第二个图标
  public boolean showTitleBarRight2Icon = true;

  // 会话列表标题栏左侧图标，资源ID
  public Integer titleBarLeftRes = null;
  // 会话列表标题栏右侧图标，资源ID
  public Integer titleBarRightRes = null;
  // 会话列表标题栏右侧第二个图标，资源ID
  public Integer titleBarRight2Res = null;

  // 会话列表标题栏标题内容
  public String titleBarTitle;
  // 会话列表标题栏标题颜色
  public Integer titleBarTitleColor = null;

  // 会话列表Item中，会话名称字体颜色
  public Integer itemTitleColor = null;
  // 会话列表Item中，会话名称字体大小
  public Integer itemTitleSize = null;
  // 会话列表Item中，会话内容（最近一条消息）字体颜色
  public Integer itemContentColor = null;
  // 会话列表Item中，会话内容（最近一条消息）字体大小
  public Integer itemContentSize = null;
  // 会话列表Item中，会话时间字体颜色
  public Integer itemDateColor = null;
  // 会话列表Item中，会话时间字体大小
  public Integer itemDateSize = null;

  // 会话列表标题栏右侧点击事件
  public View.OnClickListener titleBarRightClick;

  // 会话列表标题栏右侧第二个点击事件
  public View.OnClickListener titleBarRight2Click;

  // 会话列表标题栏左侧点击事件
  public View.OnClickListener titleBarLeftClick;

  // 会话列表Item点击事件
  public ItemClickListener itemClickListener;
  // 会话列表ViewHolder创建工厂
  public IConversationFactory conversationFactory;
  // 会话列表Item中，头像圆角
  public Float avatarCornerRadius = null;
  // 会话列表Item中，置顶会话的背景
  public Drawable itemStickTopBackground;
  // 会话列表Item中，会话的背景
  public Drawable itemBackground;
  // 会话定制能力，当前只支持定制最近一条消息的内容生成
  public ConversationCustom conversationCustom;
  // 会话列表定制能力，页面加载时，回调该接口，并传入当前Fragment
  public IConversationViewLayout customLayout;

  public boolean showConversationTopAIList = true;
}
