// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.permission.viewholder;

import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatTitleViewholderLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatTitleBean;

public class TitleViewHolder extends CommonViewHolder<QChatBaseBean> {

  private QChatTitleViewholderLayoutBinding viewBinding;

  public TitleViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public TitleViewHolder(QChatTitleViewholderLayoutBinding viewBinding) {
    this(viewBinding.getRoot());
    this.viewBinding = viewBinding;
  }

  @Override
  protected void onBindData(QChatBaseBean data, int position) {
    if (data instanceof QChatTitleBean) {
      QChatTitleBean bean = (QChatTitleBean) data;
      if (!TextUtils.isEmpty(bean.title)) {
        viewBinding.qChatVhTitleTv.setText(bean.title);
      } else {

        viewBinding.qChatVhTitleTv.setText(bean.titleRes);
      }
    }
  }
}
