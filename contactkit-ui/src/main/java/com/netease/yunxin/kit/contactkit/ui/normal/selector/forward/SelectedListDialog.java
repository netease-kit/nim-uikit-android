// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector.forward;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.databinding.ForwardSelectedListDialogViewBinding;
import com.netease.yunxin.kit.contactkit.ui.normal.selector.forward.adapter.SelectedListAdapter;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.BaseSelectedDialog;

public class SelectedListDialog extends BaseSelectedDialog {

  public static final String TAG = "SelectedListDialog";

  ForwardSelectedListDialogViewBinding binding;

  @Nullable
  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = ForwardSelectedListDialogViewBinding.inflate(inflater, container, false);
    recyclerView = binding.rvSelected;
    titleBar = binding.title;
    return binding.getRoot();
  }

  @Override
  protected int getDialogHeight() {
    return ScreenUtils.getDisplayHeight() - SizeUtils.dp2px(10);
  }

  @Override
  protected void setAdapter() {
    selectedAdapter = new SelectedListAdapter();
  }
}
