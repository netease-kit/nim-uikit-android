// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.fun.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.OnlineStatusManager;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.conversationkit.local.ui.R;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.FunLocalConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationBean;
/** 会话列表P2P会话ViewHolder，用于加载P2P会话的UI 头像、会话名称 */
public class FunConversationP2PViewHolder extends FunConversationBaseViewHolder {

  /**
   * 构造函数，初始化P2P会话ViewHolder
   *
   * @param binding P2P会话项的视图绑定对象
   */
  public FunConversationP2PViewHolder(@NonNull FunLocalConversationViewHolderBinding binding) {
    super(binding);
  }

  /**
   * 绑定P2P会话数据到视图
   *
   * @param data 会话数据模型，包含P2P会话的详细信息
   * @param position 当前项在列表中的位置
   */
  @Override
  public void onBindData(ConversationBean data, int position) {
    // 调用父类方法绑定通用UI（如置顶、未读、时间等）
    super.onBindData(data, position);

    // 获取会话名称
    String name = data.getConversationName();

    // 设置会话头像：使用会话头像URL、显示名称及基于目标ID生成的头像颜色
    viewBinding.avatarView.setData(
        data.infoData.getAvatar(),
        data.getAvatarName(),
        AvatarColor.avatarColor(data.getTargetId()));

    // 设置会话名称文本
    viewBinding.nameTv.setText(name);

    // 处理在线状态显示逻辑
    // 条件：在线状态功能启用 且 当前用户不是AI用户
    if (IMKitConfigCenter.getEnableOnlineStatus() && !AIUserManager.isAIUser(data.getTargetId())) {
      viewBinding.onlineView.setVisibility(View.VISIBLE); // 显示在线状态指示器

      // 根据用户在线状态设置不同的指示器图标
      if (OnlineStatusManager.isOnlineSubscribe(data.getTargetId())) {
        viewBinding.onlineView.setBackgroundResource(R.drawable.ic_online_status); // 在线状态图标
      } else {
        viewBinding.onlineView.setBackgroundResource(R.drawable.ic_dis_online_status); // 离线状态图标
      }
    } else {
      viewBinding.onlineView.setVisibility(View.GONE); // 不满足条件时隐藏在线状态指示器
    }
  }
}
