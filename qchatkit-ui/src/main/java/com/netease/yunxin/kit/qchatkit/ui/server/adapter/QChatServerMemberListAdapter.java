// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonMoreAdapter;
import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseMoreViewHolder;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleMemberInfo;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatRoleMemberViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.server.viewholder.QChatServerMemberViewHolder;

public class QChatServerMemberListAdapter
    extends CommonMoreAdapter<QChatServerRoleMemberInfo, QChatRoleMemberViewHolderBinding> {

  private QChatServerMemberViewHolder.DeleteListener deleteListener;

  private QChatServerMemberViewHolder.SelectListener selectListener;

  private boolean isDelete;

  private boolean isSelect;

  public QChatServerMemberListAdapter(QChatServerMemberViewHolder.DeleteListener deleteListener) {
    this.deleteListener = deleteListener;
    isDelete = true;
  }

  public QChatServerMemberListAdapter(QChatServerMemberViewHolder.SelectListener selectListener) {
    this.selectListener = selectListener;
    isSelect = true;
  }

  public void setSelect(QChatServerRoleMemberInfo item, boolean isSelected) {
    item.setSelected(isSelected);
    int pos = getDataList().indexOf(item);
    if (pos >= 0) {
      notifyItemChanged(pos);
    }
  }

  public void setDeleteListener(QChatServerMemberViewHolder.DeleteListener deleteListener) {
    this.deleteListener = deleteListener;
  }

  public void setSelectListener(QChatServerMemberViewHolder.SelectListener selectListener) {
    this.selectListener = selectListener;
  }

  @NonNull
  @Override
  public BaseMoreViewHolder<QChatServerRoleMemberInfo, QChatRoleMemberViewHolderBinding>
      getViewHolder(@NonNull ViewGroup parent, int viewType) {
    QChatRoleMemberViewHolderBinding binding =
        QChatRoleMemberViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    QChatServerMemberViewHolder viewHolder = new QChatServerMemberViewHolder(binding);
    if (deleteListener != null) {
      viewHolder.setDeleteListener(deleteListener);
    }
    if (selectListener != null) {
      viewHolder.setSelectListener(selectListener);
    }
    viewHolder.setSelect(isSelect);
    viewHolder.setDelete(isDelete);
    return viewHolder;
  }
}
