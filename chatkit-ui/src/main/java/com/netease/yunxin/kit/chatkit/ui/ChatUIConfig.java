// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenu;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenuClickListener;

public class ChatUIConfig {

  //消息点击事件
  public IMessageItemClickListener messageItemClickListener;

  //消息UI配置
  public MessageProperties messageProperties;

  public IPermissionListener permissionListener;

  public IChatFactory chatFactory;

  //聊天页面布局定制
  public IChatViewCustom chatViewCustom;

  //消息页面输入框定制
  public IChatInputMenu chatInputMenu;

  //消息长按菜单时间定制
  public IChatPopMenuClickListener popMenuClickListener;

  //消息长按弹窗相关配置
  public IChatPopMenu chatPopMenu;
}
