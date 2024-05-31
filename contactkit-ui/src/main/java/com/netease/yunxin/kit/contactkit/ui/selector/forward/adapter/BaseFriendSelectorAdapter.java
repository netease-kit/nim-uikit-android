// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.BaseSelectableViewHolder;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseFriendSelectorAdapter<R extends ViewBinding>
    extends RecyclerView.Adapter<BaseSelectableViewHolder<R>> {

  protected abstract R provideViewBinding(@NonNull ViewGroup parent, int viewType);

  protected LinkedList<ContactFriendBean> selectableBeans = new LinkedList<>();

  protected FriendSelectorListener selectableListener;

  protected boolean isMultiSelectMode = false;

  public void setSelectableListener(FriendSelectorListener selectableListener) {
    this.selectableListener = selectableListener;
  }

  /**
   * 设置数据
   *
   * @param data 数据
   */
  public void setData(List<ContactFriendBean> data) {
    selectableBeans.clear();
    if (data != null) {
      selectableBeans.addAll(data);
      notifyDataSetChanged();
    }
  }

  public void setMultiSelectMode(boolean isMultiSelectMode) {
    this.isMultiSelectMode = isMultiSelectMode;
    notifyDataSetChanged();
  }

  /**
   * 更新数据
   *
   * @param data 数据
   */
  public void updateData(ContactFriendBean data) {
    if (data != null) {
      int index = selectableBeans.indexOf(data);
      if (index != -1) {
        selectableBeans.set(index, data);
        notifyItemChanged(index);
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
    ContactFriendBean bean = selectableBeans.get(position);
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
      BaseSelectableViewHolder<R> holder, ContactFriendBean bean);

  /** 好友选择监听 */
  public interface FriendSelectorListener {
    void onSelected(ContactFriendBean bean, boolean selected);
  }
}
