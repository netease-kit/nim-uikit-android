// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.add.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatAddRoleViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatArrowBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;

public class RoleViewHolder extends CommonViewHolder<QChatBaseBean> {

  private QChatAddRoleViewHolderBinding binding;

  public RoleViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public RoleViewHolder(QChatAddRoleViewHolderBinding viewBinding) {
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
    if (data instanceof QChatArrowBean) {
      this.data = data;
      this.position = position;
      QChatArrowBean bean = ((QChatArrowBean) data);
      binding.qChatVhRoleTitleTv.setText(bean.title);
      binding.qChatVhRoleTitleTv.setTopRadius(bean.topRadius);
      binding.qChatVhRoleTitleTv.setBottomRadius(bean.bottomRadius);
    }
  }
}
