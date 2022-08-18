// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.searchkit.ui.page;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.common.ui.viewholder.IViewHolderFactory;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<BaseViewHolder> {

  private final String TAG = "SearchAdapter";
  private IViewHolderFactory viewHolderFactory;
  private final List<BaseBean> dataList = new ArrayList<>();
  private ViewHolderClickListener clickListener;

  public void setData(List<BaseBean> data) {
    dataList.clear();
    if (data != null) {
      dataList.addAll(data);
      notifyDataSetChanged();
    }
  }

  public void addForwardData(List<BaseBean> data) {
    if (data != null) {
      dataList.addAll(0, data);
    }
  }

  public void appendData(List<BaseBean> data) {
    if (data != null) {
      dataList.addAll(data);
    }
  }

  public void removeData(BaseBean data) {
    if (data == null) {
      return;
    }
    int index = -1;
    for (int j = 0; j < dataList.size(); j++) {
      if (data.equals(dataList.get(j))) {
        index = j;
        break;
      }
    }
    if (index > -1) {
      removeData(index);
    }
  }

  public void removeData(int position) {
    if (position >= 0 && position < dataList.size()) {
      dataList.remove(position);
      notifyItemRemoved(position);
    }
  }

  public void setViewHolderFactory(IViewHolderFactory factory) {
    this.viewHolderFactory = factory;
  }

  public void setViewHolderClickListener(ViewHolderClickListener listener) {
    this.clickListener = listener;
  }

  @NonNull
  @Override
  public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    BaseViewHolder viewHolder = null;
    if (viewHolderFactory != null) {
      viewHolder = viewHolderFactory.createViewHolder(parent, viewType);
    }
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
    holder.onBindData(dataList.get(position), position);
    holder.setItemOnClickListener(clickListener);
  }

  @Override
  public int getItemViewType(int position) {
    return dataList.get(position).viewType;
  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }

  public BaseBean getData(int index) {
    if (index >= 0 && index < dataList.size()) {
      return dataList.get(index);
    }
    return null;
  }
}
