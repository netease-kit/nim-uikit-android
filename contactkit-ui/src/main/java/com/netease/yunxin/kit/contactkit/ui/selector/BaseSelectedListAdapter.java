// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.corekit.coexist.im2.model.UserWithFriend;
import java.util.LinkedList;

public abstract class BaseSelectedListAdapter<R extends ViewBinding>
    extends RecyclerView.Adapter<BaseSelectedListAdapter.SelectedViewHolder<R>> {

  protected final LinkedList<ContactFriendBean> selectedFriends;

  protected ItemClickListener itemClickListener;

  public BaseSelectedListAdapter() {
    selectedFriends = new LinkedList<>();
  }

  protected abstract R provideViewBinding(@NonNull ViewGroup parent, int viewType);

  @NonNull
  @Override
  public final SelectedViewHolder<R> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new SelectedViewHolder<>(provideViewBinding(parent, viewType));
  }

  @Override
  public final void onBindViewHolder(@NonNull SelectedViewHolder<R> holder, int position) {
    ContactFriendBean bean = selectedFriends.get(position);
    if (bean == null) {
      return;
    }
    UserWithFriend friendData = bean.data;
    handleBindViewHolder(holder, bean, friendData);
  }

  protected abstract void handleBindViewHolder(
      SelectedViewHolder<R> holder, ContactFriendBean bean, UserWithFriend friendData);

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

  protected static class SelectedViewHolder<R extends ViewBinding> extends RecyclerView.ViewHolder {

    public R binding;

    public SelectedViewHolder(@NonNull R binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }

  public interface ItemClickListener {
    void onItemClick(ContactFriendBean item);
  }
}
