// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.activity;

import static com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant.KEY_TEAM_INFO;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamManagerActivity;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamManagerActivityBinding;

/**
 * 普通版群管理界面，差异化UI展示
 *
 * <p>
 */
public class TeamManagerActivity extends BaseTeamManagerActivity {

  private TeamManagerActivityBinding binding;

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    binding = TeamManagerActivityBinding.inflate(getLayoutInflater());
    changeStatusBarColor(R.color.color_eff1f4);
    viewEditManager = binding.tvEditManager;
    backView = binding.ivBack;
    tvManagerCount = binding.tvManagerCount;
    viewInvite = binding.tvInviteOtherPermission;
    tvInviteValue = binding.tvInviteOtherValue;
    viewUpdate = binding.tvUpdateInfoPermission;
    tvUpdateValue = binding.tvUpdateInfoValue;
    viewAit = binding.tvNotifyAllMembersPermission;
    viewTopSticky = binding.tvTopStickyPermission;
    tvAitValue = binding.tvNotifyAllMembersValue;
    tvTopStickyValue = binding.tvTopStickyValue;
    return binding.getRoot();
  }

  @Override
  protected void startTeamManagerListActivity() {
    Intent intent = new Intent(this, TeamManagerListActivity.class);
    intent.putExtra(KEY_TEAM_INFO, teamInfo);
    startActivity(intent);
  }
}
