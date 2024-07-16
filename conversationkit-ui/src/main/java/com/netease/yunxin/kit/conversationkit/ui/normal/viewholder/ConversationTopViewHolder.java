// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.normal.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationTopItemBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.AIUserBean;

public class ConversationTopViewHolder extends BaseViewHolder<AIUserBean> {

  protected ConversationTopItemBinding viewBinding;

  public ConversationTopViewHolder(@NonNull ConversationTopItemBinding binding) {
    super(binding.getRoot());
    viewBinding = binding;
  }

  @Override
  public void onBindData(AIUserBean data, int position) {
    if (data == null || data.getAiUser() == null) {
      return;
    }
    viewBinding.avatarView.setData(
        data.getAvatar(), data.getName(), AvatarColor.avatarColor(data.getAccountId()));
    viewBinding.nameView.setText(data.getName());
    viewBinding
        .getRoot()
        .setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                if (itemListener != null) {
                  itemListener.onClick(v, data, position);
                }
              }
            });
  }
}
