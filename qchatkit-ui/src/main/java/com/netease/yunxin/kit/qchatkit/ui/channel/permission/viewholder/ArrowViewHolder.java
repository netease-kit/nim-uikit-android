// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.permission.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatArrowViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatArrowBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;

public class ArrowViewHolder extends CommonViewHolder<QChatBaseBean> {

  private QChatArrowViewHolderBinding viewBinding;

  public ArrowViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public ArrowViewHolder(QChatArrowViewHolderBinding viewBinding) {
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
  }

  public void setRoundRadius(float radius) {
    viewBinding.qChatVhArrowTitleTv.setRadius(radius);
  }

  @Override
  protected void onBindData(QChatBaseBean data, int position) {
    if (data instanceof QChatArrowBean) {
      this.data = data;
      this.position = position;
      QChatArrowBean bean = ((QChatArrowBean) data);
      viewBinding.qChatVhArrowTitleTv.setText(bean.title);
      viewBinding.qChatVhArrowTitleTv.setTopRadius(bean.topRadius);
      viewBinding.qChatVhArrowTitleTv.setBottomRadius(bean.bottomRadius);
    }
  }
}
