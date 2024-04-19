// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.fun.viewholder;

import android.annotation.SuppressLint;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationHelper;
import com.netease.yunxin.kit.conversationkit.ui.databinding.FunConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;

/** 会话列表群组会话ViewHolder，用于加载群组会话的UI 头像、会话名称 */
public class FunConversationTeamViewHolder extends FunConversationBaseViewHolder {

  public FunConversationTeamViewHolder(@NonNull FunConversationViewHolderBinding binding) {
    super(binding);
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  @Override
  public void onBindData(ConversationBean data, int position) {
    super.onBindData(data, position);
    viewBinding.avatarView.setData(
        data.infoData.getAvatar(),
        data.getAvatarName(),
        AvatarColor.avatarColor(data.getTargetId()));
    viewBinding.nameTv.setText(data.getConversationName());
    if (data.viewType == ConversationConstant.ViewType.TEAM_VIEW
        && data.infoData.getUnreadCount() > 0
        && ConversationHelper.hasAit(data.infoData.getConversationId())) {
      viewBinding.aitTv.setVisibility(View.VISIBLE);
    } else {
      viewBinding.aitTv.setVisibility(View.GONE);
    }
  }
}
