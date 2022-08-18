// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.view.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.contactkit.ui.databinding.FriendContactViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;

public class ContactViewHolder extends BaseContactViewHolder {

  FriendContactViewHolderBinding binding;

  public ContactViewHolder(@NonNull ViewGroup itemView) {
    super(itemView);
  }

  @Override
  public void initViewBinding(LayoutInflater layoutInflater, ViewGroup container) {
    binding = FriendContactViewHolderBinding.inflate(layoutInflater, container, true);
  }

  @Override
  public void onBind(BaseContactBean bean, int position, ContactListViewAttrs attrs) {
    FriendInfo friendInfo = ((ContactFriendBean) bean).data;
    String nickName = friendInfo.getName();
    binding.tvName.setText(nickName);

    binding.avatarView.setData(
        friendInfo.getAvatar(), nickName, ColorUtils.avatarColor(friendInfo.getAccount()));

    if (attrs.getShowSelector()) {
      binding.rbSelector.setChecked(((ContactFriendBean) bean).isSelected());
      binding.rbSelector.setVisibility(View.VISIBLE);
      binding.rootView.setOnClickListener(
          v -> {
            boolean newStatue = !binding.rbSelector.isChecked();
            ((ContactFriendBean) bean).setSelected(newStatue);
            binding.rbSelector.setChecked(newStatue);
            if (actions != null && actions.getSelectorListener(bean.viewType) != null) {
              actions.getSelectorListener(bean.viewType).onSelector(newStatue, bean);
            }
          });
    } else {
      binding.rbSelector.setVisibility(View.GONE);
      binding.rootView.setOnClickListener(
          v -> {
            if (actions != null && actions.getContactListener(bean.viewType) != null) {
              actions.getContactListener(bean.viewType).onClick(position, bean);
            }
          });
    }
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
}
