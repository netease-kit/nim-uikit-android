// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.permission.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonClickListener;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelMemberConerViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatChannelMemberBean;

public class MemberViewHolder extends CommonViewHolder<QChatBaseBean> {

  private QChatChannelMemberConerViewHolderBinding viewBinding;
  private CommonClickListener deleteListener;

  public MemberViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public MemberViewHolder(QChatChannelMemberConerViewHolderBinding viewBinding) {
    this(viewBinding.getRoot());
    this.viewBinding = viewBinding;
    viewBinding
        .getRoot()
        .setOnClickListener(
            v -> {
              if (itemListener != null) {
                itemListener.onClick(this.data, this.position);
              }
            });

    viewBinding.qChatMemberEditIv.setOnClickListener(
        view -> {
          if (deleteListener != null && editStatus) {
            deleteListener.onClick(this.data, this.position);
          }
        });
  }

  @Override
  protected void onBindData(QChatBaseBean data, int position) {
    this.data = data;
    this.position = position;
    if (data instanceof QChatChannelMemberBean) {
      QChatChannelMemberBean bean = (QChatChannelMemberBean) data;
      viewBinding.qChatMemberNickTv.setText(bean.channelMember.getNickName());
      viewBinding.qChatMemberAvatarIv.setData(
          bean.channelMember.getAvatarUrl(),
          bean.channelMember.getNickName(),
          AvatarColor.avatarColor(bean.channelMember.getAccId()));
      viewBinding.qChatMemberCornerLayout.setTopRadius(bean.topRadius);
      viewBinding.qChatMemberCornerLayout.setBottomRadius(bean.bottomRadius);

      if (editStatus) {
        viewBinding.qChatMemberEditIv.setImageResource(R.drawable.ic_delete);
      } else {
        viewBinding.qChatMemberEditIv.setImageResource(R.drawable.ic_arrow_right);
      }
    }
  }

  public void setOnDeleteClickListener(CommonClickListener listener) {
    deleteListener = listener;
  }
}
