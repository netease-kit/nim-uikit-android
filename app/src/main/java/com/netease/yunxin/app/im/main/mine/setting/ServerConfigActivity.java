// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityServerConfigBinding;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;

public class ServerConfigActivity extends BaseActivity {

  private ActivityServerConfigBinding viewBinding;
  private ServerConfigViewModel viewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityServerConfigBinding.inflate(getLayoutInflater());
    viewModel = new ViewModelProvider(this).get(ServerConfigViewModel.class);
    setContentView(viewBinding.getRoot());
  }
}
