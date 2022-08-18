// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseMoreViewHolder;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatServerRoleViewHolderBinding;

public class QChatServerRoleViewHolder
    extends BaseMoreViewHolder<QChatServerRoleInfo, QChatServerRoleViewHolderBinding> {

  private DeleteListener deleteListener;

  private boolean sortable;

  private boolean disableSort;

  public QChatServerRoleViewHolder(@NonNull QChatServerRoleViewHolderBinding binding) {
    super(binding);
  }

  @Override
  public void bind(QChatServerRoleInfo item) {
    getBinding().tvName.setText(item.getName());
    getBinding().tvMember.setText(String.format("%s人", item.getMemberCount()));
    if (deleteListener != null) {
      getBinding().ivDelete.setVisibility(View.VISIBLE);
      getBinding().ivArrow.setVisibility(View.GONE);
      getBinding()
          .ivDelete
          .setOnClickListener(
              new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  deleteListener.deleteClick(item);
                }
              });
    } else {
      getBinding().ivDelete.setVisibility(View.GONE);
      getBinding().ivArrow.setVisibility(View.VISIBLE);
    }
    if (sortable) {
      getBinding().ivMove.setVisibility(View.VISIBLE);
      if (disableSort) {
        getBinding().getRoot().setAlpha(0.5f);
        getBinding()
            .ivDelete
            .setImageDrawable(
                ResourcesCompat.getDrawable(
                    getBinding().ivDelete.getContext().getResources(), R.drawable.ic_lock, null));
      } else {
        getBinding()
            .ivDelete
            .setImageDrawable(
                ResourcesCompat.getDrawable(
                    getBinding().ivDelete.getContext().getResources(), R.drawable.ic_delete, null));
        getBinding().getRoot().setAlpha(1.0f);
      }
    } else {
      getBinding().ivMove.setVisibility(View.GONE);
    }
  }

  public boolean isDisableSort() {
    return disableSort;
  }

  public void setDeleteListener(DeleteListener deleteListener) {
    this.deleteListener = deleteListener;
  }

  public interface DeleteListener {
    void deleteClick(QChatServerRoleInfo item);
  }

  public void setDisableSort(boolean disableSort) {
    this.disableSort = disableSort;
  }

  public void setSortable(boolean sortable) {
    this.sortable = sortable;
  }
}
