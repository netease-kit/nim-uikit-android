// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.contactkit.ui.databinding.SelectedListDialogSelectorViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.SelectedViewBean;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectableViewHolder;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectedDialogAdapter;

public class SelectedListDialogAdapter
    extends BaseSelectedDialogAdapter<SelectedListDialogSelectorViewHolderBinding> {
  @Override
  protected SelectedListDialogSelectorViewHolderBinding provideViewBinding(
      @NonNull ViewGroup parent, int viewType) {
    return SelectedListDialogSelectorViewHolderBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false);
  }

  @Override
  protected void handleBindViewHolder(
      BaseSelectableViewHolder<SelectedListDialogSelectorViewHolderBinding> holder,
      SelectedViewBean bean) {

    holder.binding.avatarView.setData(
        bean.getAvatar(), bean.getName(), AvatarColor.avatarColor(bean.getAccountId()));
    holder.binding.tvName.setText(bean.getName());
    if (bean.getMemberCount() > 0) {
      holder.binding.tvCount.setText("(" + bean.getMemberCount() + ")");
      holder.binding.tvCount.setVisibility(android.view.View.VISIBLE);
    } else {
      holder.binding.tvCount.setVisibility(android.view.View.GONE);
    }
    holder.binding.ivDelete.setOnClickListener(
        v -> {
          removeSelected(bean);
        });
  }
}
