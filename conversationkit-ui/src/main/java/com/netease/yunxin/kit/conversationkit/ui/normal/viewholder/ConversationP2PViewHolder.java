// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.normal.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.OnlineStatusManager;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;

/** 普通版会话列表P2P会话ViewHolder 加载P2P会话列表的UI，包括头像、名称 */
public class ConversationP2PViewHolder extends ConversationBaseViewHolder {
  public ConversationP2PViewHolder(@NonNull ConversationViewHolderBinding binding) {
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
