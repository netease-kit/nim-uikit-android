// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.common;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

public abstract class CommonViewHolder<T> extends RecyclerView.ViewHolder {

  public T data;

  public int position;

  public boolean editStatus = false;

  public CommonClickListener itemListener;

  public CommonViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public CommonViewHolder(@NonNull ViewBinding viewBinding) {
    this(viewBinding.getRoot());
  }

  protected abstract void onBindData(T data, int position);

  public void setItemOnClickListener(CommonClickListener listener) {
    itemListener = listener;
  }
}
