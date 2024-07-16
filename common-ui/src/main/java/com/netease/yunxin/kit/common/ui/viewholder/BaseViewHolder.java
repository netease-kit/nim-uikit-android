// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import java.util.List;

public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {

  public T data;

  public int position;

  public boolean editStatus = false;

  public ViewHolderClickListener itemListener;

  public BaseViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public BaseViewHolder(@NonNull ViewBinding viewBinding) {
    this(viewBinding.getRoot());
  }

  public abstract void onBindData(T data, int position);

  public void onBindData(T data, int position, @NonNull List<Object> payloads) {}

  public void setItemOnClickListener(ViewHolderClickListener listener) {
    itemListener = listener;
  }
}
