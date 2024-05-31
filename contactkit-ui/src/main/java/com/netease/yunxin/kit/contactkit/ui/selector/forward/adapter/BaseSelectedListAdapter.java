// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.netease.yunxin.kit.contactkit.ui.model.SelectedViewBean;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.BaseSelectableViewHolder;
import java.util.LinkedList;
import java.util.List;

/** 已选中的Adapter 用于在选中列表中展示，有删除能力 */
public abstract class BaseSelectedListAdapter<R extends ViewBinding>
    extends RecyclerView.Adapter<BaseSelectableViewHolder<R>> {

  protected abstract R provideViewBinding(@NonNull ViewGroup parent, int viewType);

  protected LinkedList<SelectedViewBean> selectedItems = new LinkedList<>();

  protected OnDeletedListener onDeletedListener;

  public void setOnDeletedListener(OnDeletedListener onDeletedListener) {
    this.onDeletedListener = onDeletedListener;
  }

  /**
   * 设置数据
   *
   * @param data 数据
   */
  public void setData(List<SelectedViewBean> data) {
    selectedItems.clear();
    if (data != null) {
      selectedItems.addAll(data);
      notifyDataSetChanged();
    }
  }

  /**
   * 移除数据
   *
   * @param data 数据
   */
  protected void removeSelected(SelectedViewBean data) {
    int index = selectedItems.indexOf(data);
    if (index != -1) {
      selectedItems.remove(index);
      notifyItemRemoved(index);
      if (onDeletedListener != null) {
        onDeletedListener.onDeleted(data);
      }
    }
  }

  @NonNull
  @Override
  public BaseSelectableViewHolder<R> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new BaseSelectableViewHolder<R>(provideViewBinding(parent, viewType));
  }

  @Override
  public void onBindViewHolder(@NonNull BaseSelectableViewHolder<R> holder, int position) {
    SelectedViewBean bean = selectedItems.get(position);
    if (bean == null) {
      return;
    }
    handleBindViewHolder(holder, bean);
  }

  @Override
  public int getItemCount() {
    return selectedItems.size();
  }

  protected abstract void handleBindViewHolder(
      BaseSelectableViewHolder<R> holder, SelectedViewBean bean);

  public interface OnDeletedListener {
    void onDeleted(SelectedViewBean bean);
  }
}
