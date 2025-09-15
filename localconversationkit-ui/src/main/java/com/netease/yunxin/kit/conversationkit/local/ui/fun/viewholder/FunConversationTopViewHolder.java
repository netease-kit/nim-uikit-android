// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.fun.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.FunLocalConversationTopItemBinding;
import com.netease.yunxin.kit.conversationkit.local.ui.model.AIUserBean;

/** PIN会话viewHolder 当前使用AI数字人，默认聊天数字人都放在顶部 */
public class FunConversationTopViewHolder extends BaseViewHolder<AIUserBean> {

  /** 顶部会话项视图绑定对象，用于访问布局中的UI组件 */
  protected FunLocalConversationTopItemBinding viewBinding;

  /**
   * 构造函数，初始化顶部会话ViewHolder
   *
   * @param binding 顶部会话项的视图绑定对象，包含布局中的UI元素引用
   */
  public FunConversationTopViewHolder(@NonNull FunLocalConversationTopItemBinding binding) {
    super(binding.getRoot()); // 调用父类构造函数，传入根视图
    viewBinding = binding; // 保存视图绑定对象引用
  }

  /**
   * 绑定AI用户数据到顶部会话项视图
   *
   * @param data AI用户数据模型，包含头像、名称等信息
   * @param position 当前项在横向列表中的位置
   */
  @Override
  public void onBindData(AIUserBean data, int position) {
    // 设置头像：使用AI用户头像URL、名称及基于账号ID生成的头像颜色
    viewBinding.avatarView.setData(
        data.getAvatar(), data.getName(), AvatarColor.avatarColor(data.getAccountId()));
    // 设置AI用户名称
    viewBinding.nameView.setText(data.getName());
    // 设置项点击事件：触发监听器回调
    viewBinding
        .getRoot()
        .setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                // 若监听器存在，调用其onClick方法传递事件信息
                if (itemListener != null) {
                  itemListener.onClick(v, data, position);
                }
              }
            });
  }
}
