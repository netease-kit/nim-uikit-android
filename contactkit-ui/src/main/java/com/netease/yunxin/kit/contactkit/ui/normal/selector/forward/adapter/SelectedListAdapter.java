// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector.forward.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.contactkit.ui.databinding.ForwardSelectedListSelectorViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.SelectedViewBean;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.BaseSelectableViewHolder;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter.BaseSelectedListAdapter;

public class SelectedListAdapter
    extends BaseSelectedListAdapter<ForwardSelectedListSelectorViewHolderBinding> {
  @Override
  protected ForwardSelectedListSelectorViewHolderBinding provideViewBinding(
      @NonNull ViewGroup parent, int viewType) {
    return ForwardSelectedListSelectorViewHolderBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false);
  }

  @Override
  protected void handleBindViewHolder(
      BaseSelectableViewHolder<ForwardSelectedListSelectorViewHolderBinding> holder,
      SelectedViewBean bean) {
    holder.binding.avatarView.setData(
        bean.getAvatar(),
        bean.getName(),
        AvatarColor.avatarColor(
            V2NIMConversationIdUtil.conversationTargetId(bean.getConversationId())));
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
