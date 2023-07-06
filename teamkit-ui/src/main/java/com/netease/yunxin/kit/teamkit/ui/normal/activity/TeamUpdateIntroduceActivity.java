// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.activity;

import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateIntroduceActivity;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamUpdateIntroduceActivityBinding;

/** set team introduce activity */
public class TeamUpdateIntroduceActivity extends BaseTeamUpdateIntroduceActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    TeamUpdateIntroduceActivityBinding binding =
        TeamUpdateIntroduceActivityBinding.inflate(getLayoutInflater());
    cancelView = binding.tvCancel;
    ivClear = binding.ivClear;
    tvFlag = binding.tvFlag;
    tvSave = binding.tvSave;
    etIntroduce = binding.etIntroduce;
    return binding.getRoot();
  }
}
