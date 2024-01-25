// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import static com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant.KEY_TEAM_INFO;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamManagerActivity;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamManagerActivityBinding;

public class FunTeamManagerActivity extends BaseTeamManagerActivity {
  private FunTeamManagerActivityBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_ededed);
  }

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    binding = FunTeamManagerActivityBinding.inflate(getLayoutInflater());
    viewEditManager = binding.tvEditManager;
    backView = binding.ivBack;
    tvManagerCount = binding.tvManagerCount;
    viewInvite = binding.tvInviteOtherPermission;
    tvInviteValue = binding.tvInviteOtherValue;
    viewUpdate = binding.tvUpdateInfoPermission;
    tvUpdateValue = binding.tvUpdateInfoValue;
    viewAit = binding.tvNotifyAllMembersPermission;
    tvAitValue = binding.tvNotifyAllMembersValue;
    return binding.getRoot();
  }

  @Override
  protected void startTeamManagerListActivity() {
    Intent intent = new Intent(this, FunTeamManagerListActivity.class);
    intent.putExtra(KEY_TEAM_INFO, teamInfo);
    startActivity(intent);
  }
}
