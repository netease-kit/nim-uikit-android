// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.contactkit.ui.databinding.FriendSelectedViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectedListAdapter;
import com.netease.yunxin.kit.contactkit.ui.v2model.V2ContactFriendBean;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;

public class SelectedListAdapter extends BaseSelectedListAdapter<FriendSelectedViewHolderBinding> {

  @Override
  protected FriendSelectedViewHolderBinding provideViewBinding(
      @NonNull ViewGroup parent, int viewType) {
    return FriendSelectedViewHolderBinding.inflate(
        LayoutInflater.from(parent.getContext()), parent, false);
  }

  @Override
  protected void handleBindViewHolder(
      SelectedViewHolder<FriendSelectedViewHolderBinding> holder,
      V2ContactFriendBean bean,
      UserWithFriend friendData) {
    holder.binding.avatarView.setData(
        friendData.getAvatar(),
        friendData.getName(),
        AvatarColor.avatarColor(friendData.getAccount()));
    holder.itemView.setOnClickListener(
        v -> {
          removeFriend(bean);
          if (itemClickListener != null) {
            itemClickListener.onItemClick(bean);
          }
        });
  }
}
