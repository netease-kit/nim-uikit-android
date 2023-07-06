// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.activity;

import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateNameActivity;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamUpdateNameActivityBinding;

/** set team name activity */
public class TeamUpdateNameActivity extends BaseTeamUpdateNameActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    TeamUpdateNameActivityBinding binding =
        TeamUpdateNameActivityBinding.inflate(getLayoutInflater());
    cancelView = binding.tvCancel;
    ivClear = binding.ivClear;
    tvTitle = binding.tvTitle;
    tvFlag = binding.tvFlag;
    tvSave = binding.tvSave;
    etName = binding.etName;
    return binding.getRoot();
  }
}
