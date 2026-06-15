// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.view.viewholder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactAiUserViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.AIUserInfoBean;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;

public class AIUserViewHolder extends BaseContactViewHolder {

  private ItemClickListener itemClickListener;

  private ContactAiUserViewHolderBinding binding;

  private final boolean showRoundAvatar;

  public AIUserViewHolder(@NonNull ViewGroup itemView, boolean showRoundAvatar) {
    super(itemView);
    this.showRoundAvatar = showRoundAvatar;
  }

  @Override
  public void initViewBinding(LayoutInflater layoutInflater, ViewGroup container) {
    binding = ContactAiUserViewHolderBinding.inflate(layoutInflater, container, true);
  }

  @Override
  public void onBind(BaseContactBean bean, int position, ContactListViewAttrs attrs) {
    AIUserInfoBean userBean = (AIUserInfoBean) bean;
    binding.tvName.setText(userBean.getName());

    if (showRoundAvatar) {
      binding.avatarView.setCornerRadius(SizeUtils.dp2px(4));
    }

    binding.avatarView.setData(
        userBean.getAvatar(), userBean.getName(), ColorUtils.avatarColor(userBean.getAccountId()));
    binding.rootView.setOnClickListener(
        v -> {
          if (itemClickListener != null) {
            itemClickListener.onClick((AIUserInfoBean) bean);
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

  public void setItemClickListener(ItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  public interface ItemClickListener {
    void onClick(AIUserInfoBean data);
  }
}
