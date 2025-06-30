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
import com.netease.yunxin.kit.teamkit.ui.dialog.BaseTeamIdentifyDialog;
import com.netease.yunxin.kit.teamkit.ui.fun.dialog.FunTeamIdentifyDialog;

/**
 * 娱乐版群管理页面，差异化UI展示
 *
 * <p>
 */
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
    viewTopSticky = binding.tvTopStickyPermission;
    tvAitValue = binding.tvNotifyAllMembersValue;
    tvTopStickyValue = binding.tvTopStickyValue;
    swAgreeMode = binding.swTeamAgree;
    swJoinMode = binding.swTeamJoin;
    joinAgreeGroup = binding.joinAgreeModelGroup;
    return binding.getRoot();
  }

  @Override
  protected BaseTeamIdentifyDialog getTeamIdentifyDialog() {
    return new FunTeamIdentifyDialog(this);
  }

  @Override
  protected void startTeamManagerListActivity() {
    Intent intent = new Intent(this, FunTeamManagerListActivity.class);
    intent.putExtra(KEY_TEAM_INFO, teamInfo);
    startActivity(intent);
  }
}
