// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.selector.forward.adapter;

import static com.netease.yunxin.kit.contactkit.ui.utils.TextUtils.getSelectSpanText;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunForwardTeamSelectorViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.SelectableBean;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectableViewHolder;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter.BaseTeamSelectorAdapter;

public class FunTeamSelectorAdapter
    extends BaseTeamSelectorAdapter<FunForwardTeamSelectorViewHolderBinding> {

  @Override
  protected FunForwardTeamSelectorViewHolderBinding provideViewBinding(
      @NonNull ViewGroup parent, int viewType) {
    return FunForwardTeamSelectorViewHolderBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false);
  }

  @Override
  protected void handleBindViewHolder(
      BaseSelectableViewHolder<FunForwardTeamSelectorViewHolderBinding> holder,
      SelectableBean<V2NIMTeam> bean) {
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
    holder.binding.tvCount.setText("(" + bean.data.getMemberCount() + ")");
    holder.binding.avatarView.setCornerRadius(SizeUtils.dp2px(4));
    holder.binding.avatarView.setData(bean.data.getAvatar(), bean.data.getName());
  }
}
