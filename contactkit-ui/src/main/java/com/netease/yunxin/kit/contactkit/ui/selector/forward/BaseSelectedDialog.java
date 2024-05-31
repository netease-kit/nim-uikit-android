// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector.forward;

import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.dialog.BaseBottomDialog;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter.BaseSelectedListAdapter;

/** 已选择列表基类 */
public abstract class BaseSelectedDialog extends BaseBottomDialog {

  protected BackTitleBar titleBar;

  protected RecyclerView recyclerView;

  protected BaseSelectedListAdapter.OnDeletedListener deletedListener;

  protected BaseSelectedListAdapter selectedAdapter;

  protected ContactSelectorViewModel viewModel;

  @Override
  protected void setStyle() {}

  @Override
  protected void initParams() {
    Window window = getDialog().getWindow();
    if (window != null) {
      WindowManager.LayoutParams params = window.getAttributes();
      params.gravity = Gravity.BOTTOM;
      params.width = ViewGroup.LayoutParams.MATCH_PARENT;
      params.height = getDialogHeight();
      window.setAttributes(params);
      window.setBackgroundDrawableResource(R.drawable.bg_selected_dialog);
    }
    this.setCancelable(true);
    setAdapter();
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setAdapter(selectedAdapter);
    titleBar.setOnBackIconClickListener(v -> getDialog().onBackPressed());
  }

  protected abstract int getDialogHeight();

  @Override
  protected void initData() {
    super.initData();
    viewModel = new ViewModelProvider(requireActivity()).get(ContactSelectorViewModel.class);
    selectedAdapter.setData(viewModel.getSelectedList());
    deletedListener =
        data -> {
          if (viewModel != null) {
            viewModel.removeSelectedItem(data);
          }
        };
    selectedAdapter.setOnDeletedListener(deletedListener);
  }

  protected abstract void setAdapter();
}
