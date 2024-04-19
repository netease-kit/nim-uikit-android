// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.normal.viewholder;

import android.annotation.SuppressLint;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationHelper;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;

/** 普通版会话列表团队会话ViewHolder 加载团队会话列表的UI，包括头像、名称 */
public class ConversationTeamViewHolder extends ConversationBaseViewHolder {

  public ConversationTeamViewHolder(@NonNull ConversationViewHolderBinding binding) {
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
    if (ConversationHelper.hasAit(data.infoData.getConversationId())
        && data.infoData.getUnreadCount() > 0) {
      viewBinding.aitTv.setVisibility(View.VISIBLE);
    } else {
      viewBinding.aitTv.setVisibility(View.GONE);
    }
  }
}
