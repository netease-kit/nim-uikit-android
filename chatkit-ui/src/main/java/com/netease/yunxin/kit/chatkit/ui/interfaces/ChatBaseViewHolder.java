// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.interfaces;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import java.util.List;

/**
 * Pin列表界面的ViewHolder基类
 *
 * @param <T> 数据类型
 */
public abstract class ChatBaseViewHolder<T> extends RecyclerView.ViewHolder {

  public T data;

  public int position;

  public boolean editStatus = false;

  public IItemClickListener itemListener;

  public ChatBaseViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public ChatBaseViewHolder(@NonNull ViewBinding viewBinding) {
    this(viewBinding.getRoot());
  }

  public abstract void onBindData(T data, int position, @NonNull List<?> payload);

  public abstract void onBindData(T data, int position);

  public void setItemOnClickListener(IItemClickListener listener) {
    itemListener = listener;
  }

  public void onDetachedFromWindow() {}

  public void onAttachedToWindow() {}
}
