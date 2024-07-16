// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.selector;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunContactSelectedListDialogViewBinding;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectedDialog;

public class FunSelectedListDialog extends BaseSelectedDialog {

  public static final String TAG = "FunSelectedListDialog";

  FunContactSelectedListDialogViewBinding binding;

  @Nullable
  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = FunContactSelectedListDialogViewBinding.inflate(inflater, container, false);
    recyclerView = binding.rvSelected;
    titleBar = binding.title;
    titleBar.setLeftText(R.string.cancel);
    return binding.getRoot();
  }

  @Override
  protected int getDialogHeight() {
    return ScreenUtils.getDisplayHeight() * 2 / 5;
  }

  @Override
  protected void setAdapter() {
    selectedAdapter = new FunSelectedListDialogAdapter();
  }
}
