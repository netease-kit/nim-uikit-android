// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.viewholder;

import android.view.ViewGroup;
import androidx.annotation.NonNull;

public interface IViewHolderFactory {

  BaseViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType);
}
