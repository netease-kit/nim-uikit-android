// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector.forward.adapter;

import static com.netease.yunxin.kit.contactkit.ui.utils.TextUtils.getSelectSpanText;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.ForwardTeamSelectorViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.SelectableBean;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.BaseSelectableViewHolder;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter.BaseTeamSelectorAdapter;

public class TeamSelectorAdapter
    extends BaseTeamSelectorAdapter<ForwardTeamSelectorViewHolderBinding> {

  @Override
  protected ForwardTeamSelectorViewHolderBinding provideViewBinding(
      @NonNull ViewGroup parent, int viewType) {
    return ForwardTeamSelectorViewHolderBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false);
  }

  @Override
  protected void handleBindViewHolder(
      BaseSelectableViewHolder<ForwardTeamSelectorViewHolderBinding> holder,
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
    holder.binding.avatarView.setData(bean.data.getAvatar(), bean.data.getName());
  }
}
