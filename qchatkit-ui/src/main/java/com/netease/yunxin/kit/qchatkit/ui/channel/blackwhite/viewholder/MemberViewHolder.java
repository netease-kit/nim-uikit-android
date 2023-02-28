// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.blackwhite.viewholder;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonClickListener;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelNameListMemberViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatServerMemberBean;

public class MemberViewHolder extends CommonViewHolder<QChatBaseBean> {

  private TextView nameTv;
  private ContactAvatarView avatarView;
  private CommonClickListener arrowListener;
  private QChatChannelNameListMemberViewHolderBinding binding;
  private CommonClickListener deleteListener;

  public MemberViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public MemberViewHolder(QChatChannelNameListMemberViewHolderBinding viewBinding) {
    this(viewBinding.getRoot());
    binding = viewBinding;
    nameTv = viewBinding.qChatNameListNameTv;
    avatarView = viewBinding.qChatNameListAvatarIv;
    viewBinding
        .getRoot()
        .setOnClickListener(
            v -> {
              if (arrowListener != null) {
                arrowListener.onClick(this.data, this.position);
              }
            });
    viewBinding.qChatNameListDelete.setOnClickListener(
        view -> {
          if (deleteListener != null) {
            deleteListener.onClick(this.data, this.position);
          }
        });
  }

  @Override
  protected void onBindData(QChatBaseBean data, int position) {
    this.data = data;
    this.position = position;
    if (data instanceof QChatServerMemberBean) {
      QChatServerMemberBean bean = (QChatServerMemberBean) data;
      nameTv.setText(bean.serverMember.getNickName());
      avatarView.setData(
          bean.serverMember.getAvatarUrl(),
          bean.serverMember.getNickName(),
          AvatarColor.avatarColor(bean.serverMember.getAccId()));
      if (editStatus) {
        binding.qChatNameListDelete.setVisibility(View.VISIBLE);
      } else {
        binding.qChatNameListDelete.setVisibility(View.GONE);
      }
    }
  }

  public void setOnClickListener(CommonClickListener listener) {
    arrowListener = listener;
  }

  public void setDeleteClickListener(CommonClickListener listener) {

    deleteListener = listener;
  }
}
