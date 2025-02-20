// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.normal.viewholder;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.LocalSelectorViewHolderLayoutBinding;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationBean;

/** 选择器ViewHolder 用于选择器模式下的会话列表 */
public class SelectorViewHolder extends BaseViewHolder<ConversationBean> {

  private LocalSelectorViewHolderLayoutBinding viewBinding;

  public SelectorViewHolder(@NonNull ViewGroup itemView) {
    super(itemView);
  }

  public SelectorViewHolder(@NonNull LocalSelectorViewHolderLayoutBinding binding) {
    this(binding.getRoot());
    viewBinding = binding;
  }

  @Override
  public void onBindData(ConversationBean data, int position) {
    viewBinding.avatarView.setData(
        data.infoData.getAvatar(),
        data.infoData.getName(),
        AvatarColor.avatarColor(data.infoData.getConversationId()));
    viewBinding.conversationNameTv.setText(data.infoData.getName());
    viewBinding.rootView.setOnClickListener(
        v -> {
          viewBinding.conversationSelectorCb.setChecked(
              !viewBinding.conversationSelectorCb.isChecked());
          if (itemListener != null) {
            int check = viewBinding.conversationSelectorCb.isChecked() ? 1 : 0;
            itemListener.onClick(v, data, check);
          }
        });
    viewBinding.rootView.setOnLongClickListener(
        v -> {
          if (itemListener != null) {
            return itemListener.onLongClick(v, data, position);
          }
          return false;
        });
  }
}
