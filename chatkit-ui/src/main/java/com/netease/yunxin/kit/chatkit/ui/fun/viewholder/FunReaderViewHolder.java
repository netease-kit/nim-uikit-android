// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatReaderViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatReadUserBean;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;

public class FunReaderViewHolder extends BaseViewHolder<ChatReadUserBean> {

  private FunChatReaderViewHolderBinding viewBinding;

  public FunReaderViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public FunReaderViewHolder(@NonNull FunChatReaderViewHolderBinding viewBinding) {
    this(viewBinding.getRoot());
    this.viewBinding = viewBinding;
  }

  @Override
  public void onBindData(ChatReadUserBean data, int position) {
    if (data != null) {
      String name = MessageHelper.getTeamReaderDisplayName(data.userInfo.getAccount());
      viewBinding.avatar.setData(
          data.userInfo.getAvatar(), name, AvatarColor.avatarColor(data.userInfo.getAccount()));
      viewBinding.nickname.setText(name);
      viewBinding
          .getRoot()
          .setOnClickListener(
              v -> {
                if (itemListener != null) {
                  itemListener.onClick(v, data, position);
                }
              });
    }
  }
}
