// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector.forward.adapter;

import static com.netease.yunxin.kit.contactkit.ui.utils.TextUtils.getSelectSpanText;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.coexist.sdk.v2.conversation.model.V2NIMBaseConversation;
import com.netease.nimlib.coexist.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.ForwardConversationSelectorViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.SelectableBean;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectableViewHolder;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter.BaseConversationSelectorAdapter;

public class ConversationSelectorAdapter
    extends BaseConversationSelectorAdapter<ForwardConversationSelectorViewHolderBinding> {

  @Override
  protected ForwardConversationSelectorViewHolderBinding provideViewBinding(
      @NonNull ViewGroup parent, int viewType) {
    return ForwardConversationSelectorViewHolderBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false);
  }

  @Override
  protected void handleBindViewHolder(
      BaseSelectableViewHolder<ForwardConversationSelectorViewHolderBinding> holder,
      SelectableBean<V2NIMBaseConversation> bean) {
    if (isMultiSelectMode) {
      holder.binding.rbSelector.setVisibility(View.VISIBLE);
      holder.binding.rbSelector.setChecked(bean.isSelected);
    } else {
      holder.binding.rbSelector.setVisibility(View.GONE);
    }

    String conversationName =
        bean.data.getName() == null
            ? V2NIMConversationIdUtil.conversationTargetId(bean.data.getConversationId())
            : bean.data.getName();

    holder.binding.tvName.setText(
        getSelectSpanText(
            holder.itemView.getContext().getResources().getColor(R.color.color_337eff),
            conversationName,
            bean.recordHitInfo));
    if (bean.memberCount > 0) {
      holder.binding.tvCount.setVisibility(View.VISIBLE);
      holder.binding.tvCount.setText("(" + bean.memberCount + ")");
    } else {
      holder.binding.tvCount.setVisibility(View.GONE);
    }
    holder.binding.avatarView.setData(
        bean.data.getAvatar(),
        conversationName,
        AvatarColor.avatarColor(
            V2NIMConversationIdUtil.conversationTargetId(bean.data.getConversationId())));
  }
}
