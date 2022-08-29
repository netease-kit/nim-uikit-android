// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.contactkit.ui.databinding.FriendSelectedViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import java.util.LinkedList;

public class SelectedListAdapter
    extends RecyclerView.Adapter<SelectedListAdapter.SelectedViewHolder> {

  private final LinkedList<ContactFriendBean> selectedFriends;

  private ItemClickListener itemClickListener;

  public SelectedListAdapter() {
    selectedFriends = new LinkedList<>();
  }

  @NonNull
  @Override
  public SelectedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    FriendSelectedViewHolderBinding binding =
        FriendSelectedViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    return new SelectedViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull SelectedViewHolder holder, int position) {
    ContactFriendBean bean = selectedFriends.get(position);
    if (bean == null) {
      return;
    }
    FriendInfo friendData = bean.data;
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

  public void setItemClickListener(ItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  @Override
  public int getItemCount() {
    return selectedFriends.size();
  }

  public LinkedList<ContactFriendBean> getSelectedFriends() {
    return selectedFriends;
  }

  public void addFriend(ContactFriendBean friend) {
    int index = selectedFriends.size();
    selectedFriends.add(friend);
    notifyItemInserted(index);
  }

  public void removeFriend(ContactFriendBean friend) {
    int pos = selectedFriends.indexOf(friend);
    if (pos >= 0) {
      selectedFriends.remove(friend);
      notifyItemRemoved(pos);
    }
  }

  static class SelectedViewHolder extends RecyclerView.ViewHolder {

    FriendSelectedViewHolderBinding binding;

    public SelectedViewHolder(@NonNull FriendSelectedViewHolderBinding binding) {
      super(binding.rootView);
      this.binding = binding;
    }
  }

  public interface ItemClickListener {
    void onItemClick(ContactFriendBean item);
  }
}
