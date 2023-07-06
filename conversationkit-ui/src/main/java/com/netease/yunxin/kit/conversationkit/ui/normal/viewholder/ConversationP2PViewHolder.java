// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.normal.viewholder;

import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;

public class ConversationP2PViewHolder extends ConversationBaseViewHolder {
  public ConversationP2PViewHolder(@NonNull ConversationViewHolderBinding binding) {
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
