// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.netease.yunxin.kit.chatkit.model.RecentForward;
import com.netease.yunxin.kit.contactkit.ui.model.SelectableBean;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectableViewHolder;
import com.netease.yunxin.kit.contactkit.ui.selector.SelectableListener;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseRecentForwardSelectorAdapter<R extends ViewBinding>
    extends RecyclerView.Adapter<BaseSelectableViewHolder<R>> {

  protected abstract R provideViewBinding(@NonNull ViewGroup parent, int viewType);

  protected SelectableListener<RecentForward> selectableListener;

  protected LinkedList<SelectableBean<RecentForward>> selectableBeans = new LinkedList<>();

  //是否多选模式
  protected boolean isMultiSelectMode = false;

  /**
   * 设置数据
   *
   * @param data 数据
   */
  public void setData(List<SelectableBean<RecentForward>> data) {
    selectableBeans.clear();
    if (data != null) {
      selectableBeans.addAll(data);
      notifyDataSetChanged();
    }
  }

  public void updateData(SelectableBean<RecentForward> data) {
    if (data != null) {
      int index = selectableBeans.indexOf(data);
      if (index != -1) {
        selectableBeans.set(index, data);
        notifyItemChanged(index);
      }
    }
  }

  public void setSelectableListener(SelectableListener<RecentForward> selectableListener) {
    this.selectableListener = selectableListener;
  }

  public void setMultiSelectMode(boolean isMultiSelectMode) {
    this.isMultiSelectMode = isMultiSelectMode;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public BaseSelectableViewHolder<R> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new BaseSelectableViewHolder<R>(provideViewBinding(parent, viewType));
  }

  @Override
  public void onBindViewHolder(@NonNull BaseSelectableViewHolder<R> holder, int position) {
    SelectableBean<RecentForward> bean = selectableBeans.get(position);
    if (bean == null) {
      return;
    }
    holder
        .binding
        .getRoot()
        .setOnClickListener(
            v -> {
              if (selectableListener != null) {
                selectableListener.onSelected(bean, !bean.isSelected);
              }
            });
    handleBindViewHolder(holder, bean);
  }

  @Override
  public int getItemCount() {
    return selectableBeans.size();
  }

  protected abstract void handleBindViewHolder(
      BaseSelectableViewHolder<R> holder, SelectableBean<RecentForward> bean);
}
