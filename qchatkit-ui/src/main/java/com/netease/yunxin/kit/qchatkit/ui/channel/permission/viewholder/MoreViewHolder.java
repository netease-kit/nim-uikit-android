// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.permission.viewholder;

import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatMoreViewholderLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatMoreBean;

public class MoreViewHolder extends CommonViewHolder<QChatBaseBean> {

  private QChatMoreViewholderLayoutBinding viewBinding;

  public MoreViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public MoreViewHolder(QChatMoreViewholderLayoutBinding viewBinding) {
    this(viewBinding.getRoot());
    this.viewBinding = viewBinding;
    this.viewBinding
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
    if (data instanceof QChatMoreBean) {
      this.data = data;
      this.position = position;
      QChatMoreBean bean = (QChatMoreBean) data;
      if (!TextUtils.isEmpty(bean.title)) {
        String title = bean.title;
        if (bean.extend != null) {
          title = String.format(bean.title, bean.extend);
        }
        viewBinding.qChatVhMoreTv.setText(title);
      } else {
        String title = viewBinding.getRoot().getContext().getString(bean.titleRes);
        if (bean.extend != null) {
          title = String.format(title, bean.extend);
        }
        viewBinding.qChatVhMoreTv.setText(title);
      }
    }
  }
}
