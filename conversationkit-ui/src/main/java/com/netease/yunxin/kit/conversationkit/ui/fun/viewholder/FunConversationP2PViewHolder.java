// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.fun.viewholder;

import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.conversationkit.ui.databinding.FunConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;

public class FunConversationP2PViewHolder extends FunConversationBaseViewHolder {

  public FunConversationP2PViewHolder(@NonNull FunConversationViewHolderBinding binding) {
    super(binding);
  }

  @Override
  public void onBindData(ConversationBean data, int position) {
    super.onBindData(data, position);
    String name = data.infoData.getName();
    viewBinding.avatarView.setData(
        data.infoData.getAvatar(),
        data.infoData.getAvatarName(),
        AvatarColor.avatarColor(data.infoData.getContactId()));
    viewBinding.nameTv.setText(name);
  }
}
