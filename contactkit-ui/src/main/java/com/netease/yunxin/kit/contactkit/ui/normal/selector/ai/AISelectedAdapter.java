// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactAiSelectedSelectorViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.SelectedViewBean;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectableViewHolder;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectedSelectorAdapter;

/** 已选中的Adapter */
public class AISelectedAdapter
    extends BaseSelectedSelectorAdapter<ContactAiSelectedSelectorViewHolderBinding> {
  @Override
  protected ContactAiSelectedSelectorViewHolderBinding provideViewBinding(
      @NonNull ViewGroup parent, int viewType) {
    return ContactAiSelectedSelectorViewHolderBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false);
  }

  @Override
  protected void handleBindViewHolder(
      BaseSelectableViewHolder<ContactAiSelectedSelectorViewHolderBinding> holder,
      SelectedViewBean bean) {
    holder.binding.avatarView.setData(
        bean.getAvatar(), bean.getName(), AvatarColor.avatarColor(bean.getTargetId()));
    holder
        .binding
        .getRoot()
        .setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                if (itemClickListener != null) {
                  itemClickListener.onItemClick(bean);
                }
              }
            });
  }
}
