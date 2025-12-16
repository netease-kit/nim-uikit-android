// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.selector.forward.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.coexist.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunForwardSelectedSelectorViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.SelectedViewBean;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectableViewHolder;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectedSelectorAdapter;

/** 已选中的Adapter */
public class FunForwardSelectedAdapter
    extends BaseSelectedSelectorAdapter<FunForwardSelectedSelectorViewHolderBinding> {
  @Override
  protected FunForwardSelectedSelectorViewHolderBinding provideViewBinding(
      @NonNull ViewGroup parent, int viewType) {
    return FunForwardSelectedSelectorViewHolderBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false);
  }

  @Override
  protected void handleBindViewHolder(
      BaseSelectableViewHolder<FunForwardSelectedSelectorViewHolderBinding> holder,
      SelectedViewBean bean) {
    holder.binding.avatarView.setCornerRadius(SizeUtils.dp2px(4));
    holder.binding.avatarView.setData(
        bean.getAvatar(),
        bean.getName(),
        AvatarColor.avatarColor(V2NIMConversationIdUtil.conversationTargetId(bean.getTargetId())));
  }
}
