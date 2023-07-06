// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateNicknameActivity;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamUpdateNicknameActivityBinding;

/** set nick name activity */
public class FunTeamUpdateNicknameActivity extends BaseTeamUpdateNicknameActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_ededed);
  }

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunTeamUpdateNicknameActivityBinding binding =
        FunTeamUpdateNicknameActivityBinding.inflate(getLayoutInflater());
    ivClear = binding.ivClear;
    cancelView = binding.ivCancel;
    tvFlag = binding.tvFlag;
    tvSave = binding.tvSave;
    etNickname = binding.etNickname;
    return binding.getRoot();
  }
}
