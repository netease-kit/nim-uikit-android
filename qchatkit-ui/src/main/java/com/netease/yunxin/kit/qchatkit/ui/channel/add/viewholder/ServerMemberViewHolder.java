// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.add.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelMemberViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatServerMemberBean;

public class ServerMemberViewHolder extends CommonViewHolder<QChatBaseBean> {

  private QChatChannelMemberViewHolderBinding binding;

  public ServerMemberViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public ServerMemberViewHolder(QChatChannelMemberViewHolderBinding viewBinding) {
    this(viewBinding.getRoot());
    binding = viewBinding;
    binding
        .getRoot()
        .setOnClickListener(
            v -> {
              if (itemListener != null) {
                itemListener.onClick(this.data, this.position);
              }
            });
  }

  @Override
  protected void onBindData(QChatBaseBean data, int position) {
    this.data = data;
    this.position = position;
    if (data instanceof QChatServerMemberBean) {
      QChatServerMemberBean bean = (QChatServerMemberBean) data;
      binding.qChatMemberNameTv.setText(bean.serverMember.getNickName());
      binding.qChatMemberAvatarIv.setData(
          bean.serverMember.getAvatarUrl(),
          bean.serverMember.getNickName(),
          AvatarColor.avatarColor(bean.serverMember.getAccId()));
    }
  }
}
