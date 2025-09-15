// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.CommonBaseMessageViewHolder;

/** 聊天工厂 该工厂用于创建聊天相关的组件，如聊天界面等。 */
public interface IChatFactory {
  /**
   * 获取消息的ViewType
   *
   * @param messageBean 消息对象
   * @return ViewType
   */
  int getItemViewType(ChatMessageBean messageBean);

  /**
   * 创建ViewHolder
   *
   * @param parent 父容器
   * @param viewType 视图类型
   * @return ViewHolder
   */
  CommonBaseMessageViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType);
}
