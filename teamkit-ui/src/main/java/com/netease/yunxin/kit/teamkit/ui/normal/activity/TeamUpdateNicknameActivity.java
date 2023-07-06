// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.activity;

import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateNicknameActivity;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamUpdateNicknameActivityBinding;

/** set nick name activity */
public class TeamUpdateNicknameActivity extends BaseTeamUpdateNicknameActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    TeamUpdateNicknameActivityBinding binding =
        TeamUpdateNicknameActivityBinding.inflate(getLayoutInflater());
    ivClear = binding.ivClear;
    cancelView = binding.tvCancel;
    tvFlag = binding.tvFlag;
    tvSave = binding.tvSave;
    etNickname = binding.etNickname;
    return binding.getRoot();
  }
}
