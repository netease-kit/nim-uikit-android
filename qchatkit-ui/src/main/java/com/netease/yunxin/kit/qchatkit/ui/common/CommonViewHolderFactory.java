// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.common;

import android.view.ViewGroup;
import androidx.annotation.NonNull;

public interface CommonViewHolderFactory<T> {
  CommonViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType);
}
