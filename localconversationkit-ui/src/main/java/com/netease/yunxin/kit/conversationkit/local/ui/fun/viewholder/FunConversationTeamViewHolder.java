// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.fun.viewholder;

import android.annotation.SuppressLint;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.conversationkit.local.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.local.ui.common.ConversationHelper;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.FunLocalConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationBean;

/** 会话列表群组会话ViewHolder，用于加载群组会话的UI 头像、会话名称 */
public class FunConversationTeamViewHolder extends FunConversationBaseViewHolder {

  /**
   * 构造函数，初始化群组会话ViewHolder
   *
   * @param binding 群组会话项的视图绑定对象，包含布局中的UI组件引用
   */
  public FunConversationTeamViewHolder(@NonNull FunLocalConversationViewHolderBinding binding) {
    super(binding);
  }

  /**
   * 绑定群组会话数据到视图
   *
   * @param data 会话数据模型，包含群组会话的详细信息（如头像、名称、未读数量等）
   * @param position 当前项在列表中的位置
   */
  @SuppressLint("UseCompatLoadingForDrawables") // 用于兼容旧版Drawable加载API
  @Override
  public void onBindData(ConversationBean data, int position) {
    // 调用父类方法绑定通用UI（如置顶状态、未读计数、时间等）
    super.onBindData(data, position);

    // 设置群组头像：使用群组头像URL、显示名称及基于群组ID生成的头像颜色
    viewBinding.avatarView.setData(
        data.infoData.getAvatar(), // 群组头像URL
        data.getAvatarName(), // 头像显示名称（通常为群组名称）
        AvatarColor.avatarColor(data.getTargetId())); // 基于群组ID生成的头像背景色

    // 设置群组名称文本
    viewBinding.nameTv.setText(data.getConversationName());

    // 控制@提及提示的显示逻辑
    // 条件：当前为群组视图类型 + 存在未读消息 + 会话包含@提及信息
    if (data.viewType == ConversationConstant.ViewType.TEAM_VIEW
        && data.infoData.getUnreadCount() > 0 // 未读消息数量 > 0
        && ConversationHelper.hasAit(data.infoData.getConversationId())) { // 检测到@提及
      viewBinding.aitTv.setVisibility(View.VISIBLE); // 显示@提示标记
    } else {
      viewBinding.aitTv.setVisibility(View.GONE); // 隐藏@提示标记
    }
  }
}
