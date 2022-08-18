// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonMoreAdapter;
import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseMoreViewHolder;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleInfo;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatServerRoleViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.server.viewholder.QChatServerRoleViewHolder;
import java.util.Collections;

public class QChatServerRolesAdapter
    extends CommonMoreAdapter<QChatServerRoleInfo, QChatServerRoleViewHolderBinding> {

  private QChatServerRoleViewHolder.DeleteListener deleteListener;

  public QChatServerRolesAdapter() {}

  public void setDeleteListener(QChatServerRoleViewHolder.DeleteListener deleteListener) {
    this.deleteListener = deleteListener;
  }

  private boolean isSort;

  private long myPriority;

  private boolean isServerOwner;

  @NonNull
  @Override
  public BaseMoreViewHolder<QChatServerRoleInfo, QChatServerRoleViewHolderBinding> getViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    QChatServerRoleViewHolderBinding binding =
        QChatServerRoleViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    QChatServerRoleViewHolder holder = new QChatServerRoleViewHolder(binding);
    holder.setDeleteListener(deleteListener);
    return holder;
  }

  @Override
  public void onBindViewHolder(
      @NonNull BaseMoreViewHolder<QChatServerRoleInfo, QChatServerRoleViewHolderBinding> holder,
      int position) {
    QChatServerRoleViewHolder roleViewHolder = (QChatServerRoleViewHolder) holder;
    roleViewHolder.setSortable(isSort);
    QChatServerRoleInfo roleInfo = getDataList().get(position);
    if (isServerOwner) {
      roleViewHolder.setDisableSort(false);
    } else {
      roleViewHolder.setDisableSort(roleInfo.getPriority() <= myPriority);
    }
    holder.bind(roleInfo);
    if (getItemClickListener() != null) {
      holder.itemView.setOnClickListener(
          v -> {
            getItemClickListener().onItemClick(roleInfo, position);
          });
    }
  }

  public void setSort(boolean sort) {
    isSort = sort;
  }

  public void setMyPriority(long myPriority) {
    this.myPriority = myPriority;
  }

  public void setServerOwner(boolean serverOwner) {
    isServerOwner = serverOwner;
  }

  public long getTopPriority() {
    long topPriority = 0;
    for (QChatServerRoleInfo role : getDataList()) {
      if (isServerOwner || (role.getPriority() > myPriority)) {
        if (topPriority == 0 || topPriority > role.getPriority()) {
          topPriority = role.getPriority();
        }
      }
    }
    return topPriority;
  }

  public long getMyPriority() {
    return myPriority;
  }

  public void onMove(int prePosition, int postPosition) {
    Collections.swap(getDataList(), prePosition, postPosition);
    notifyItemMoved(prePosition, postPosition);
  }
}
