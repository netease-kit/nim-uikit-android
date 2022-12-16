// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.map.ChatLocationBean;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.SearchLocationViewHolderBinding;
import java.util.ArrayList;
import java.util.List;

public class SearchLocationAdapter
    extends RecyclerView.Adapter<SearchLocationAdapter.SearchLocationViewHolder> {
  private final List<ChatLocationBean> data = new ArrayList<>();
  private OnLocationSelected onLocationSelected;
  private ChatLocationBean selectBean;
  private int selectPosition;

  public void setData(List<ChatLocationBean> data, OnLocationSelected onLocationSelected) {
    this.data.clear();
    this.onLocationSelected = onLocationSelected;
    if (data != null) {
      this.data.addAll(data);
      for (int index = 0; index < data.size(); index++) {
        ChatLocationBean item = data.get(index);
        if (item.getSelected()) {
          selectBean = item;
          selectPosition = index;
          break;
        }
      }
      notifyDataSetChanged();
    }
  }

  @NonNull
  @Override
  public SearchLocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new SearchLocationViewHolder(
        SearchLocationViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull SearchLocationViewHolder holder, int position) {
    ChatLocationBean item = data.get(position);
    if (item != null) {
      holder.binding.locationTitle.setText(item.getTitle());
      String desc = item.getAddress();
      if (item.getDistance() != null && item.getDistance() > 0) {
        desc =
            holder
                .itemView
                .getContext()
                .getString(
                    R.string.chat_message_location_distance, Math.ceil(item.getDistance()), desc);
      }
      holder.binding.locationDesc.setText(desc);
      holder.binding.locationSelected.setVisibility(item.getSelected() ? View.VISIBLE : View.GONE);
      holder.itemView.setOnClickListener(
          v -> {
            selectBean.setSelected(false);
            notifyItemChanged(selectPosition);
            item.setSelected(true);
            selectBean = item;
            selectPosition = holder.getBindingAdapterPosition();
            if (onLocationSelected != null) {
              onLocationSelected.onSelected(item);
            }
            notifyItemChanged(selectPosition);
          });
    }
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  public interface OnLocationSelected {
    void onSelected(ChatLocationBean bean);
  }

  static class SearchLocationViewHolder extends RecyclerView.ViewHolder {
    SearchLocationViewHolderBinding binding;

    public SearchLocationViewHolder(@NonNull SearchLocationViewHolderBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
