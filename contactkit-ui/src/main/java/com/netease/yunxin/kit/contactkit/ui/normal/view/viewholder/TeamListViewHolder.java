// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.view.viewholder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.contactkit.ui.databinding.TeamListViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactTeamBean;
import com.netease.yunxin.kit.contactkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;

public class TeamListViewHolder extends BaseContactViewHolder {

  private itemClickListener itemClickListener;

  private TeamListViewHolderBinding binding;

  public TeamListViewHolder(@NonNull ViewGroup itemView) {
    super(itemView);
  }

  @Override
  public void initViewBinding(LayoutInflater layoutInflater, ViewGroup container) {
    binding = TeamListViewHolderBinding.inflate(layoutInflater, container, true);
  }

  @Override
  public void onBind(BaseContactBean bean, int position, ContactListViewAttrs attrs) {
    Team teamInfo = ((ContactTeamBean) bean).data;
    binding.tvName.setText(teamInfo.getName());

    binding.avatarView.setData(
        teamInfo.getIcon(), teamInfo.getName(), ColorUtils.avatarColor(teamInfo.getId()));

    binding.rootView.setOnClickListener(
        v -> {
          if (itemClickListener != null) {
            itemClickListener.onClick((ContactTeamBean) bean);
          }
        });

    loadConfig(attrs);
  }

  private void loadConfig(ContactListViewAttrs attrs) {
    if (attrs == null) {
      return;
    }
    if (attrs.getNameTextColor() != ContactListViewAttrs.INT_NULL) {
      binding.tvName.setTextColor(attrs.getNameTextColor());
    }
    if (attrs.getNameTextSize() != ContactListViewAttrs.INT_NULL) {
      binding.tvName.setTextSize(attrs.getNameTextSize());
    }

    if (attrs.getAvatarCornerRadius() != ContactListViewAttrs.INT_NULL) {
      binding.avatarView.setCornerRadius(attrs.getAvatarCornerRadius());
    }
  }

  public void setItemClickListener(TeamListViewHolder.itemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  public interface itemClickListener {
    void onClick(ContactTeamBean data);
  }
}
