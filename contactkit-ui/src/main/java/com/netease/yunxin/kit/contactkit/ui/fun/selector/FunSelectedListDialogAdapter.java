// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.selector;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunSelectedListDialogSelectorViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.SelectedViewBean;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectableViewHolder;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectedDialogAdapter;

public class FunSelectedListDialogAdapter
    extends BaseSelectedDialogAdapter<FunSelectedListDialogSelectorViewHolderBinding> {
  @Override
  protected FunSelectedListDialogSelectorViewHolderBinding provideViewBinding(
      @NonNull ViewGroup parent, int viewType) {
    return FunSelectedListDialogSelectorViewHolderBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false);
  }

  @Override
  protected void handleBindViewHolder(
      BaseSelectableViewHolder<FunSelectedListDialogSelectorViewHolderBinding> holder,
      SelectedViewBean bean) {
    holder.binding.avatarView.setCornerRadius(SizeUtils.dp2px(4));
    holder.binding.avatarView.setData(
        bean.getAvatar(), bean.getName(), AvatarColor.avatarColor(bean.getAccountId()));
    holder.binding.tvName.setText(bean.getName());
    if (bean.getMemberCount() > 0) {
      holder.binding.tvCount.setVisibility(android.view.View.VISIBLE);
      holder.binding.tvCount.setText("(" + bean.getMemberCount() + ")");
    } else {
      holder.binding.tvCount.setVisibility(android.view.View.GONE);
    }
    holder.binding.ivDelete.setOnClickListener(v -> removeSelected(bean));
  }
}
