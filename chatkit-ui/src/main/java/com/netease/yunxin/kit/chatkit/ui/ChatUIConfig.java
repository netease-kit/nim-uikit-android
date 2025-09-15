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

/**
 * 聊天模块UI配置
 * 该类定义了聊天模块的UI配置，包括消息点击事件、消息UI配置、权限授权监听、聊天工厂、聊天页面布局定制、消息页面输入框定制、消息长按菜单点击监听、消息长按菜单、聊天页面定制、聊天页面监听、输入框定制、撤回操作时间限制、撤回重新编辑时间、是否使用权限说明弹窗等。
 */
public class ChatUIConfig {

  /** 消息点击事件 该接口定义了消息的点击事件方法，用于处理用户点击消息的事件。 */
  public IMessageItemClickListener messageItemClickListener;

  /** 消息UI配置 该类定义了消息的UI配置，消息的外观。 */
  public MessageProperties messageProperties;

  /** 权限授权监听 该接口定义了权限授权的回调方法，用于处理权限申请的结果。 */
  public IPermissionListener permissionListener;

  /** 聊天工厂 该工厂用于创建聊天相关的组件，如聊天界面、输入框等。 */
  public IChatFactory chatFactory;

  /** 聊天页面布局定制 该接口定义了聊天页面的布局定制方法，用于自定义聊天页面的外观和行为。 */
  public IChatViewCustom chatViewCustom;

  /** 消息页面输入框定制 该接口定义了消息页面输入框的定制方法，用于自定义输入框的外观和行为。 */
  public IChatInputMenu chatInputMenu;

  /** 消息长按菜单点击监听 该接口定义了消息长按菜单的点击监听方法，用于处理用户点击长按菜单的事件。 */
  public IChatPopMenuClickListener popMenuClickListener;

  /** 消息长按菜单 该接口定义了消息长按菜单的定制方法，用于自定义长按菜单的外观和行为。 */
  public IChatPopMenu chatPopMenu;

  /** 聊天页面定制 该接口定义了聊天页面的定制方法，用于自定义聊天页面的外观和行为。 */
  public ChatCustom chatCustom;

  /** 聊天页面监听 该接口定义了聊天页面的监听方法，用于处理聊天页面的事件。 */
  public IChatListener chatListener;

  /** 输入框定制 该接口定义了输入框的定制方法，用于自定义输入框的外观和行为。 */
  public InputProperties inputProperties;

  /** 撤回操作时间限制 单位分钟，客户端限制，需要小于云信控制台配置时间才有用 */
  public Long revokeTimeGap;

  /** 撤回重新编辑时间 单位分钟，默认两分钟 */
  public Long revokeEditTimeGap;

  /** 是否使用权限说明弹窗 默认true */
  public Boolean showPermissionPop;
}
