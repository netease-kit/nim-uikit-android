// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.view.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.OnlineStatusManager;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunFriendContactViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.corekit.coexist.im2.model.UserWithFriend;

public class FunContactViewHolder extends BaseContactViewHolder {

  FunFriendContactViewHolderBinding binding;
  private final int cornerSize = SizeUtils.dp2px(4);

  public FunContactViewHolder(@NonNull ViewGroup itemView) {
    super(itemView);
  }

  @Override
  public void initViewBinding(LayoutInflater layoutInflater, ViewGroup container) {
    binding = FunFriendContactViewHolderBinding.inflate(layoutInflater, container, true);
  }

  @Override
  public void onBind(BaseContactBean bean, int position, ContactListViewAttrs attrs) {
    UserWithFriend friendInfo = ((ContactFriendBean) bean).data;
    String nickName = friendInfo.getName();
    String avatarName = friendInfo.getAvatarName();
    binding.tvName.setText(nickName);

    binding.avatarView.setCornerRadius(cornerSize);
    binding.avatarView.setData(
        friendInfo.getAvatar(), avatarName, ColorUtils.avatarColor(friendInfo.getAccount()));

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
      binding.onlineView.setVisibility(View.GONE);
    } else {
      binding.rbSelector.setVisibility(View.GONE);
      binding.rootView.setOnClickListener(
          v -> {
            if (actions != null && actions.getContactListener(bean.viewType) != null) {
              actions.getContactListener(bean.viewType).onClick(position, bean);
            }
          });
      if (IMKitConfigCenter.getEnableOnlineStatus()) {
        binding.onlineView.setVisibility(View.VISIBLE);
        if (OnlineStatusManager.isOnlineSubscribe(friendInfo.getAccount())) {
          binding.onlineView.setBackgroundResource(R.drawable.ic_online_status);
        } else {
          binding.onlineView.setBackgroundResource(R.drawable.ic_dis_online_status);
        }
      } else {
        binding.onlineView.setVisibility(View.GONE);
      }
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
