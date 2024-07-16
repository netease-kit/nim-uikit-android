// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector.forward.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.contactkit.ui.databinding.ForwardSelectedSelectorViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.SelectedViewBean;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectableViewHolder;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectedSelectorAdapter;

/** 已选中的Adapter */
public class SelectedAdapter
    extends BaseSelectedSelectorAdapter<ForwardSelectedSelectorViewHolderBinding> {
  @Override
  protected ForwardSelectedSelectorViewHolderBinding provideViewBinding(
      @NonNull ViewGroup parent, int viewType) {
    return ForwardSelectedSelectorViewHolderBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false);
  }

  @Override
  protected void handleBindViewHolder(
      BaseSelectableViewHolder<ForwardSelectedSelectorViewHolderBinding> holder,
      SelectedViewBean bean) {
    holder.binding.avatarView.setData(
        bean.getAvatar(), bean.getName(), AvatarColor.avatarColor(bean.getAccountId()));
  }
}
