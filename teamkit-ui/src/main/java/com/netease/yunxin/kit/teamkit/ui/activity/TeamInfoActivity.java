// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ICON;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_NAME;
import static com.netease.yunxin.kit.teamkit.ui.activity.TeamUpdateIntroduceActivity.KEY_TEAM_INTRODUCE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamInfoActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;

/** team info activity */
public class TeamInfoActivity extends BaseActivity {
  public static final String KEY_TEAM_UPDATE_INFO_PRIVILEGE = "update_info_privilege";
  public static final String KEY_TEAM_TYPE = "team_type";
  public static final String KEY_TEAM_IS_GROUP = "team_group_tag";
  private TeamInfoActivityBinding binding;
  private ActivityResultLauncher<Intent> launcher;

  private String teamId;
  private String teamIconUrl;
  private String teamIntroduce;
  private String teamName;
  private TeamTypeEnum teamTypeEnum;
  private boolean isGroup = false;

  private boolean canUpdate = false;
  private boolean hasUpdatePrivilege = false;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = TeamInfoActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    launcher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              Intent data = result.getData();
              if (result.getResultCode() != RESULT_OK || data == null) {
                return;
              }
              canUpdate = true;
              Object iconObj = data.getStringExtra(KEY_TEAM_ICON);
              if (iconObj != null) {
                teamIconUrl = String.valueOf(iconObj);
                binding.ivIcon.setData(teamIconUrl, teamName, ColorUtils.avatarColor(teamId));
              }
              Object introduceObj = data.getStringExtra(KEY_TEAM_INTRODUCE);
              if (introduceObj != null) {
                teamIntroduce = String.valueOf(introduceObj);
              }
              Object nameObj = data.getStringExtra(KEY_TEAM_NAME);
              if (nameObj != null) {
                teamName = String.valueOf(nameObj);
              }
            });

    changeStatusBarColor(R.color.color_eff1f4);

    Intent intent = getIntent();
    hasUpdatePrivilege = intent.getBooleanExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, false);
    teamId = intent.getStringExtra(KEY_TEAM_ID);
    teamIconUrl = intent.getStringExtra(KEY_TEAM_ICON);
    teamIntroduce = intent.getStringExtra(KEY_TEAM_INTRODUCE);
    teamName = intent.getStringExtra(KEY_TEAM_NAME);
    isGroup = intent.getBooleanExtra(KEY_TEAM_IS_GROUP, false);
    teamTypeEnum = (TeamTypeEnum) intent.getSerializableExtra(KEY_TEAM_TYPE);

    binding.ivIcon.setData(teamIconUrl, teamName, ColorUtils.avatarColor(teamId));
    if (teamTypeEnum == TeamTypeEnum.Advanced && !isGroup) {
      binding.tvTitle.setText(R.string.team_info_title);
      binding.tvIcon.setText(R.string.team_icon_title);
      binding.tvName.setText(R.string.team_name_title);
      binding.tvIntroduce.setVisibility(View.VISIBLE);
      binding.line2.setVisibility(View.VISIBLE);
    } else {
      binding.tvTitle.setText(R.string.team_group_info_title);
      binding.tvIcon.setText(R.string.team_group_icon_title);
      binding.tvName.setText(R.string.team_group_name_title);
      binding.tvIntroduce.setVisibility(View.GONE);
      binding.line2.setVisibility(View.GONE);
    }
    binding.tvIcon.setOnClickListener(
        v ->
            TeamUpdateIconActivity.launch(
                TeamInfoActivity.this, hasUpdatePrivilege, teamId, teamIconUrl, launcher));
    binding.tvName.setOnClickListener(
        v ->
            TeamUpdateNameActivity.launch(
                TeamInfoActivity.this,
                hasUpdatePrivilege,
                teamTypeEnum,
                teamId,
                teamName,
                isGroup,
                launcher));
    binding.tvIntroduce.setOnClickListener(
        v ->
            TeamUpdateIntroduceActivity.launch(
                TeamInfoActivity.this, hasUpdatePrivilege, teamId, teamIntroduce, launcher));
    binding.ivBack.setOnClickListener(v -> finish());
  }

  @Override
  public void finish() {
    if (canUpdate) {
      Intent intent = new Intent();
      intent.putExtra(KEY_TEAM_ICON, teamIconUrl);
      intent.putExtra(KEY_TEAM_NAME, teamName);
      intent.putExtra(KEY_TEAM_INTRODUCE, teamIntroduce);
      setResult(RESULT_OK, intent);
    }
    super.finish();
  }

  public static void launch(
      Context context,
      boolean hasUpdatePrivilege,
      TeamTypeEnum teamTypeEnum,
      String teamId,
      String teamName,
      String teamIntroduce,
      String teamIcon,
      boolean isGroup,
      ActivityResultLauncher<Intent> launcher) {
    Intent intent = new Intent(context, TeamInfoActivity.class);
    intent.putExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, hasUpdatePrivilege);
    intent.putExtra(KEY_TEAM_TYPE, teamTypeEnum);
    intent.putExtra(KEY_TEAM_ID, teamId);
    intent.putExtra(KEY_TEAM_ICON, teamIcon);
    intent.putExtra(KEY_TEAM_NAME, teamName);
    intent.putExtra(KEY_TEAM_INTRODUCE, teamIntroduce);
    intent.putExtra(KEY_TEAM_IS_GROUP, isGroup);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    launcher.launch(intent);
  }
}
