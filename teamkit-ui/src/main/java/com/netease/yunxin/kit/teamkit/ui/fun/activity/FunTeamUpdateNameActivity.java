// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateNameActivity;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamUpdateNameActivityBinding;

/**
 * 娱乐版群名称修改页面，差异化UI展示
 *
 * <p>
 */
public class FunTeamUpdateNameActivity extends BaseTeamUpdateNameActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_ededed);
  }

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunTeamUpdateNameActivityBinding binding =
        FunTeamUpdateNameActivityBinding.inflate(getLayoutInflater());
    cancelView = binding.ivCancel;
    ivClear = binding.ivClear;
    tvTitle = binding.tvTitle;
    tvFlag = binding.tvFlag;
    tvSave = binding.tvSave;
    etName = binding.etName;
    return binding.getRoot();
  }
}
