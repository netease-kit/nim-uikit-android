// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.permission.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatCornerViewholderLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;

public class CornerViewHolder extends CommonViewHolder<QChatBaseBean> {

  private QChatCornerViewholderLayoutBinding viewBinding;

  public CornerViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public CornerViewHolder(QChatCornerViewholderLayoutBinding viewBinding) {
    this(viewBinding.getRoot());
    this.viewBinding = viewBinding;
  }

  @Override
  protected void onBindData(QChatBaseBean data, int position) {}
}
