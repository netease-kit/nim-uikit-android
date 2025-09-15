// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.CommonBaseMessageViewHolder;

/** 聊天界面默认工厂接口 该接口定义了聊天界面的默认工厂方法，用于创建自定义消息视图Holder */
public interface IChatDefaultFactory extends IChatFactory {
  /**
   * 添加自定义消息视图Holder
   *
   * @param type 消息类型
   * @param viewHolder 视图Holder类
   */
  <T extends ChatBaseMessageViewHolder> void addCustomViewHolder(int type, Class<T> viewHolder);

  /**
   * 移除自定义消息视图Holder
   *
   * @param type 消息类型
   */
  <T extends ChatBaseMessageViewHolder> void removeCustomViewHolder(int type);

  /**
   * 添加自定义通用消息视图Holder
   *
   * @param type 消息类型
   * @param viewHolderClass 视图Holder类
   * @param layoutRes 布局资源ID
   */
  <T extends CommonBaseMessageViewHolder> void addCommonCustomViewHolder(
      int type, Class<T> viewHolderClass, @LayoutRes int layoutRes);

  /**
   * 移除自定义通用消息视图Holder
   *
   * @param type 消息类型
   */
  void removeCommonCustomViewHolder(int type);

  /**
   * 获取自定义消息视图类型
   *
   * @param messageBean 消息对象
   * @return 自定义消息视图类型
   */
  int getCustomViewType(ChatMessageBean messageBean);

  /**
   * 创建自定义消息视图Holder
   *
   * @param parent 父容器
   * @param viewType 视图类型
   * @return 自定义消息视图Holder
   */
  @Nullable
  CommonBaseMessageViewHolder createViewHolderCustom(@NonNull ViewGroup parent, int viewType);
}
