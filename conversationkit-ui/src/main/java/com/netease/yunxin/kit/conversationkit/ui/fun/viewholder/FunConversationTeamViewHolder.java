// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.fun.viewholder;

import android.annotation.SuppressLint;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationHelper;
import com.netease.yunxin.kit.conversationkit.ui.databinding.FunConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;

public class FunConversationTeamViewHolder extends FunConversationBaseViewHolder {

  public FunConversationTeamViewHolder(@NonNull FunConversationViewHolderBinding binding) {
    super(binding);
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  @Override
  public void onBindData(ConversationBean data, int position) {
    super.onBindData(data, position);
    if (data.infoData.getTeamInfo() != null) {
      Team teamInfo = data.infoData.getTeamInfo();
      viewBinding.avatarView.setData(
          teamInfo.getIcon(), teamInfo.getName(), AvatarColor.avatarColor(teamInfo.getId()));
      viewBinding.nameTv.setText(teamInfo.getName());
    }
    if (data.viewType == ConversationConstant.ViewType.TEAM_VIEW
        && data.infoData.getUnreadCount() > 0
        && ConversationHelper.hasAit(data.infoData.getContactId())) {
      viewBinding.aitTv.setVisibility(View.VISIBLE);
    } else {
      viewBinding.aitTv.setVisibility(View.GONE);
    }
  }
}
