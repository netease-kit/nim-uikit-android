// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.selector.ai;

import static com.netease.yunxin.kit.contactkit.ui.utils.TextUtils.getSelectSpanText;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.coexist.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunAiUserSelectorViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.SelectableBean;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectableViewHolder;
import com.netease.yunxin.kit.contactkit.ui.selector.ai.BaseAIUserSelectorAdapter;

public class FunAIUserSelectorAdapter
    extends BaseAIUserSelectorAdapter<FunAiUserSelectorViewHolderBinding> {

  @Override
  protected FunAiUserSelectorViewHolderBinding provideViewBinding(
      @NonNull ViewGroup parent, int viewType) {
    return FunAiUserSelectorViewHolderBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false);
  }

  @Override
  protected void handleBindViewHolder(
      BaseSelectableViewHolder<FunAiUserSelectorViewHolderBinding> holder,
      SelectableBean<V2NIMAIUser> bean) {
    if (isMultiSelectMode) {
      holder.binding.rbSelector.setVisibility(View.VISIBLE);
      holder.binding.rbSelector.setChecked(bean.isSelected);
    } else {
      holder.binding.rbSelector.setVisibility(View.GONE);
    }
    holder.binding.tvName.setText(
        getSelectSpanText(
            holder.itemView.getContext().getResources().getColor(R.color.color_337eff),
            bean.data.getName(),
            bean.recordHitInfo));
    holder.binding.avatarView.setCornerRadius(SizeUtils.dp2px(4));
    holder.binding.avatarView.setData(bean.data.getAvatar(), bean.data.getName());
  }
}
