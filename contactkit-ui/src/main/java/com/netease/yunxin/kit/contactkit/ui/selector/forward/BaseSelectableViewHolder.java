// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector.forward;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

public class BaseSelectableViewHolder<R extends ViewBinding> extends RecyclerView.ViewHolder {

  public R binding;

  public BaseSelectableViewHolder(@NonNull R binding) {
    super(binding.getRoot());
    this.binding = binding;
  }
}
