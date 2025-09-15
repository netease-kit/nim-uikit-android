// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import android.view.View;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import java.util.List;

/** 聊天输入菜单 该接口定义了聊天输入菜单的相关方法，用于自定义输入菜单的行为和外观。 */
public interface IChatInputMenu {
  /**
   * 自定义输入菜单，输入框下方的菜单
   *
   * @param actionItemList 输入菜单列表
   * @return 自定义后的输入菜单列表
   */
  public default List<ActionItem> customizeInputBar(List<ActionItem> actionItemList) {
    return actionItemList;
  }

  /**
   * 自定义更多菜单，更多区域按钮
   *
   * @param actionItemList 更多菜单列表
   * @return 自定义后的更多菜单列表
   */
  public default List<ActionItem> customizeInputMore(List<ActionItem> actionItemList) {
    return actionItemList;
  }

  /**
   * 自定义输入菜单点击事件
   *
   * @param context 上下文
   * @param view 触发事件的视图
   * @param action 点击的操作类型
   * @return 是否消费该事件
   */
  public default boolean onCustomInputClick(Context context, View view, String action) {
    return false;
  }

  /**
   * 自定义更多菜单点击事件
   *
   * @param context 上下文
   * @param view 触发事件的视图
   * @param action 点击的操作类型
   * @return 是否消费该事件
   */
  public default boolean onCustomInputMoreClick(Context context, View view, String action) {
    return false;
  }

  /**
   * 自定义输入菜单点击事件
   *
   * @param context 上下文
   * @param view 触发事件的视图
   * @param action 点击的操作类型
   * @return 是否消费该事件
   */
  public default boolean onInputClick(Context context, View view, String action) {
    return false;
  }

  /**
   * 自定义智能助手点击事件
   *
   * @param context 上下文
   * @param view 触发事件的视图
   * @param action 点击的操作类型
   * @param messageInfoList 消息列表
   * @return 是否消费该事件
   */
  public default boolean onAIHelperClick(
      Context context, View view, String action, List<IMMessageInfo> messageInfoList) {
    return false;
  }
}
