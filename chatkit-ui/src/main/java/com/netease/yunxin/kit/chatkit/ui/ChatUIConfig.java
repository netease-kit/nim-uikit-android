// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.input.InputProperties;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenu;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenuClickListener;

/** 聊天模块UI配置 */
public class ChatUIConfig {

  //消息点击事件
  public IMessageItemClickListener messageItemClickListener;

  //消息UI配置
  public MessageProperties messageProperties;

  //权限授权监听
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

  //个性化定制内容
  public ChatCustom chatCustom;

  //进入会话监听，每次进入新的会话回调
  public IChatListener chatListener;

  //输入框定制
  public InputProperties inputProperties;

  // 撤回操作时间限制，单位分钟，客户端限制，需要小于云信控制台配置时间才有用
  public Long revokeTimeGap;

  //撤回重新编辑时间,时间单位分钟，默认为两分钟
  public Long revokeEditTimeGap;

  //是否使用权限说明弹窗
  public Boolean showPermissionPop;
}
