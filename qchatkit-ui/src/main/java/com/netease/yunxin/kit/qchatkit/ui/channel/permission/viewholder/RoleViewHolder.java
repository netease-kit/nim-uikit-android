// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.permission.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonClickListener;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelRoleViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatChannelRoleBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;

public class RoleViewHolder extends CommonViewHolder<QChatBaseBean> {

  private QChatChannelRoleViewHolderBinding viewBinding;
  private CommonClickListener deleteListener;

  public RoleViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public RoleViewHolder(QChatChannelRoleViewHolderBinding viewBinding) {
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
    viewBinding.qChatVhRoleEditIv.setOnClickListener(
        view -> {
          if (deleteListener != null && editStatus) {
            deleteListener.onClick(this.data, this.position);
          }
        });
  }

  public void setRoundRadius(float radius) {
    viewBinding.qChatVhRoleTitleTv.setRadius(radius);
  }

  @Override
  protected void onBindData(QChatBaseBean data, int position) {
    if (data instanceof QChatChannelRoleBean) {
      this.data = data;
      this.position = position;
      QChatChannelRoleBean bean = ((QChatChannelRoleBean) data);
      viewBinding.qChatVhRoleTitleTv.setText(bean.channelRole.getName());
      viewBinding.qChatVhRoleTitleTv.setTopRadius(bean.topRadius);
      viewBinding.qChatVhRoleTitleTv.setBottomRadius(bean.bottomRadius);

      if (editStatus) {
        if (bean.channelRole.getType() == QChatConstant.EVERYONE_TYPE) {
          viewBinding.qChatVhRoleEditIv.setImageDrawable(null);
        } else {
          viewBinding.qChatVhRoleEditIv.setImageResource(R.drawable.ic_delete);
        }
      } else {
        viewBinding.qChatVhRoleEditIv.setImageResource(R.drawable.ic_arrow_right);
      }
    }
  }

  public void setOnDeleteClickListener(CommonClickListener listener) {
    deleteListener = listener;
  }
}
