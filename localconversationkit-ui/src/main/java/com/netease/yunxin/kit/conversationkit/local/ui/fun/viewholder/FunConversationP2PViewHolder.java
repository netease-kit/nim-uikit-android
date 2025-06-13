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

  public FunConversationP2PViewHolder(@NonNull FunLocalConversationViewHolderBinding binding) {
    super(binding);
  }

  @Override
  public void onBindData(ConversationBean data, int position) {
    super.onBindData(data, position);
    String name = data.getConversationName();
    viewBinding.avatarView.setData(
        data.infoData.getAvatar(),
        data.getAvatarName(),
        AvatarColor.avatarColor(data.getTargetId()));
    viewBinding.nameTv.setText(name);
    if (IMKitConfigCenter.getEnableOnlineStatus() && !AIUserManager.isAIUser(data.getTargetId())) {
      viewBinding.onlineView.setVisibility(View.VISIBLE);
      if (OnlineStatusManager.isOnlineSubscribe(data.getTargetId())) {
        viewBinding.onlineView.setBackgroundResource(R.drawable.ic_online_status);
      } else {
        viewBinding.onlineView.setBackgroundResource(R.drawable.ic_dis_online_status);
      }
    } else {
      viewBinding.onlineView.setVisibility(View.GONE);
    }
  }
}
