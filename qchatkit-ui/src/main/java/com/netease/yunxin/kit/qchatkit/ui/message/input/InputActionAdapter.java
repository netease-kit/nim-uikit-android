// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.input;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatMessageActionItemBinding;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IItemActionListener;
import java.util.ArrayList;
import java.util.List;

public class InputActionAdapter extends RecyclerView.Adapter<InputActionAdapter.ItemHolder> {

  private final List<ActionItem> mItems = new ArrayList<>();
  private final IItemActionListener onItemClick;
  private boolean disableAll = false;

  public InputActionAdapter(List<ActionItem> itemList, IItemActionListener listener) {
    if (itemList != null && itemList.size() > 0) {
      mItems.addAll(itemList);
    }
    onItemClick = listener;
  }

  public void disableAll(boolean disableAll) {
    this.disableAll = disableAll;
    notifyItemRangeChanged(0, getItemCount());
  }

  public void updateItemState(String type, boolean select) {
    for (int i = 0; i < getItemCount(); ++i) {
      ActionItem item = mItems.get(i);
      if (TextUtils.equals(item.getAction(), type)) {
        item.setSelected(select);
        notifyItemChanged(i);
        break;
      }
    }
  }

  @NonNull
  @Override
  public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    QChatMessageActionItemBinding binding =
        QChatMessageActionItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    int width = ScreenUtils.getDisplayWidth() / mItems.size();
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
    binding.chatMessageActionItem.setLayoutParams(params);
    return new ItemHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
    ActionItem item = mItems.get(position);
    if (item == null) {
      return;
    }
    holder.binding.qChatMessageActionItemBtn.setBackgroundResource(item.getIconResId());
    holder.binding.qChatMessageActionItemBtn.setChecked(item.isSelected());
    holder.binding.qChatMessageActionItemBtn.setEnabled(!disableAll);
    holder.binding.qChatMessageActionItemBtn.setAlpha(disableAll ? 0.5f : 1f);
    holder.binding.chatMessageActionItem.setEnabled(!disableAll);
    holder.binding.chatMessageActionItem.setOnClickListener(
        v -> {
          if (onItemClick != null) {
            item.onClick(v);
            onItemClick.onClick(v, position, item);
          }
        });
  }

  @Override
  public int getItemCount() {
    return mItems == null ? 0 : mItems.size();
  }

  static class ItemHolder extends RecyclerView.ViewHolder {
    QChatMessageActionItemBinding binding;

    public ItemHolder(@NonNull QChatMessageActionItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
