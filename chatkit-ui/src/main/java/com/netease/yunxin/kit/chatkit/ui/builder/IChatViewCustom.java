// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.builder;

import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatView;

/** 聊天视图自定义接口 用于自定义聊天界面的布局和行为 实现此接口可以对聊天视图进行个性化定制 */
public interface IChatViewCustom {
  /**
   * 自定义聊天布局 在此方法中可以对聊天视图进行各种定制操作
   *
   * @param layout 聊天视图接口实例
   */
  void customizeChatLayout(final IChatView layout);
}
