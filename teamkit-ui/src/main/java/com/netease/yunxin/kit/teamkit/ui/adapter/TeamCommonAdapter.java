// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.netease.yunxin.kit.alog.ALog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 通用适配器
 *
 * @param <T> 数据类型
 * @param <R> ViewBinding
 */
public class TeamCommonAdapter<T, R extends ViewBinding>
    extends RecyclerView.Adapter<TeamCommonAdapter.ItemViewHolder<R>> {
  private static final String TAG = "QChatCommonAdapter";
  private static final String NAME_METHOD_INFLATE = "inflate";
  protected final List<T> dataSource = new ArrayList<>();
  protected final Context context;
  protected OnClickListener<T, R> clickListener;
  protected Method inflateMethod;

  public TeamCommonAdapter(Context context, Class<R> viewBinding) {
    this.context = context;
    try {
      inflateMethod =
          viewBinding.getMethod(
              NAME_METHOD_INFLATE, LayoutInflater.class, ViewGroup.class, boolean.class);
    } catch (NoSuchMethodException e) {
      ALog.e(TAG, "Execute construction func fail with viewBinding");
      throw new IllegalStateException("create adapter error", e);
    }
  }

  @SuppressLint("NotifyDataSetChanged")
  public void setDataList(List<T> data) {

    if (data == null) {
      return;
    }
    dataSource.clear();
    dataSource.addAll(data);
    notifyDataSetChanged();
  }

  public void addData(List<T> dataList, Comparator<T> comparator) {
    if (dataList == null || dataList.isEmpty()) {
      return;
    }
    for (T data : dataList) {
      if (!dataSource.contains(data)) {
        dataSource.add(data);
      }
    }
    if (comparator != null) {
      Collections.sort(dataSource, comparator);
    }
    notifyDataSetChanged();
  }

  public void removeData(List<String> accountList) {}

  public void updateData(List<T> dataList) {
    if (dataList == null) {
      return;
    }
    for (T user : dataList) {
      for (int i = 0; i < dataSource.size(); i++) {
        if (dataSource.get(i).equals(user)) {
          dataSource.set(i, user);
          notifyItemChanged(i);
        }
      }
    }
  }

  public void setItemClickListener(OnClickListener<T, R> listener) {
    this.clickListener = listener;
  }

  @NonNull
  @Override
  @SuppressWarnings("unchecked")
  public ItemViewHolder<R> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    R binding;
    try {
      binding = (R) inflateMethod.invoke(null, LayoutInflater.from(context), parent, false);
    } catch (IllegalAccessException | InvocationTargetException e) {
      ALog.e(TAG, "Execute onCreateViewHolder func fail with viewBinding");
      throw new IllegalStateException("create view holder error", e);
    }
    return new ItemViewHolder<>(Objects.requireNonNull(binding));
  }

  @Override
  public void onBindViewHolder(@NonNull ItemViewHolder<R> holder, int position) {
    T item = getItemData(position);
    holder.itemView.setOnClickListener(
        v -> {
          if (clickListener != null) {
            clickListener.onClick(item, holder);
          }
        });
    if (item == null) {
      return;
    }
    onBindViewHolder(holder, position, item);
  }

  public void onBindViewHolder(@NonNull ItemViewHolder<R> holder, int position, T data) {
    onBindViewHolder(holder.binding, position, data, holder.getBindingAdapterPosition());
  }

  public void onBindViewHolder(R binding, int position, T data, int bingingAdapterPosition) {}

  @Override
  public int getItemCount() {
    return dataSource.size();
  }

  public T getItemData(int position) {
    if (position < 0 || position >= dataSource.size()) {
      return null;
    }
    return dataSource.get(position);
  }

  public static class ItemViewHolder<T extends ViewBinding> extends RecyclerView.ViewHolder {
    public final T binding;

    public ItemViewHolder(T binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }

  public interface OnClickListener<T, R extends ViewBinding> {
    void onClick(T data, ItemViewHolder<R> holder);
  }
}
