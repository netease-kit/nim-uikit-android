// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.common;

import android.util.SparseArray;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import java.util.ArrayList;
import java.util.List;

public class CommonRecyclerViewAdapter
    extends RecyclerView.Adapter<CommonViewHolder<QChatBaseBean>> {

  private final List<QChatBaseBean> dataList = new ArrayList<>();
  private final SparseArray<Class<CommonViewHolder<QChatBaseBean>>> viewHolderArray =
      new SparseArray<>();
  private CommonViewHolderFactory<QChatBaseBean> viewHolderFactory;
  private boolean editStatus = false;

  public void setData(List<? extends QChatBaseBean> data) {
    dataList.clear();
    if (data == null) {
      return;
    }
    dataList.addAll(data);
    notifyDataSetChanged();
  }

  public void addData(int index, List<? extends QChatBaseBean> data) {
    if (data != null && data.size() > 0) {
      if (index > 0 && index < dataList.size()) {
        dataList.addAll(index, data);
      } else {
        index = dataList.size();
        dataList.addAll(data);
      }
      notifyDataSetChanged();
    }
  }

  public void addData(List<? extends QChatBaseBean> data) {
    if (data != null && data.size() > 0) {
      int size = dataList.size();
      dataList.addAll(data);
      notifyItemRangeChanged(size, data.size());
    }
  }

  public void removeData(int index) {
    if (index > 0 && index < dataList.size()) {
      dataList.remove(index);
      notifyItemRemoved(index);
    }
  }

  public QChatBaseBean getData(int index) {
    if (index < 0 || index > dataList.size() - 1) {
      return null;
    }
    return dataList.get(index);
  }

  public void setEditStatus(boolean edit) {
    if (edit != editStatus) {
      editStatus = edit;
      notifyDataSetChanged();
    }
  }

  public void addViewHolder(SparseArray<Class<CommonViewHolder<QChatBaseBean>>> holderList) {
    if (holderList != null) {
      for (int index = 0; index < holderList.size(); index++) {
        viewHolderArray.put(holderList.keyAt(index), holderList.valueAt(index));
      }
    }
  }

  public void setViewHolderFactory(CommonViewHolderFactory<QChatBaseBean> factory) {
    viewHolderFactory = factory;
  }

  @NonNull
  @Override
  public CommonViewHolder<QChatBaseBean> onCreateViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    CommonViewHolder<QChatBaseBean> viewHolder = null;
    if (viewHolderFactory != null) {
      viewHolder = viewHolderFactory.onCreateViewHolder(parent, viewType);
    }

    if (viewHolder == null) {
      Class<CommonViewHolder<QChatBaseBean>> clazz = viewHolderArray.get(viewType);
      try {
        viewHolder =
            (CommonViewHolder<QChatBaseBean>)
                clazz.getConstructor(ViewGroup.class, int.class).newInstance(parent, viewType);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    assert viewHolder != null;
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
    holder.editStatus = this.editStatus;
    holder.onBindData(dataList.get(position), position);
  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }

  @Override
  public int getItemViewType(int position) {
    return dataList.get(position).viewType;
  }
}
