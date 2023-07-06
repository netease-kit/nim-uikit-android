// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateIntroduceActivity;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamUpdateIntroduceActivityBinding;

/** set team introduce activity */
public class FunTeamUpdateIntroduceActivity extends BaseTeamUpdateIntroduceActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_ededed);
  }

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunTeamUpdateIntroduceActivityBinding binding =
        FunTeamUpdateIntroduceActivityBinding.inflate(getLayoutInflater());
    cancelView = binding.ivCancel;
    ivClear = binding.ivClear;
    tvFlag = binding.tvFlag;
    tvSave = binding.tvSave;
    etIntroduce = binding.etIntroduce;
    return binding.getRoot();
  }
}
