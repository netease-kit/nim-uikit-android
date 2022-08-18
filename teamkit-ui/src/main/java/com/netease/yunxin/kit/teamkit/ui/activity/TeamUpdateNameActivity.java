// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_NAME;
import static com.netease.yunxin.kit.teamkit.ui.activity.TeamInfoActivity.KEY_TEAM_TYPE;
import static com.netease.yunxin.kit.teamkit.ui.activity.TeamInfoActivity.KEY_TEAM_UPDATE_INFO_PRIVILEGE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamUpdateNameActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;

/** set team name activity */
public class TeamUpdateNameActivity extends BaseActivity {
  private TeamUpdateNameActivityBinding binding;
  private static final String MAX_COUNT_STR = "/30";
  private final TeamSettingViewModel model = new TeamSettingViewModel();
  private boolean canUpdate = false;
  private String lastTeamName;
  private String teamId;

  private String teamName;
  private boolean hasPrivilege;

  @SuppressLint("SetTextI18n")
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = TeamUpdateNameActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    changeStatusBarColor(R.color.color_eff1f4);
    hasPrivilege = getIntent().getBooleanExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, false);
    teamId = getIntent().getStringExtra(KEY_TEAM_ID);
    lastTeamName = getIntent().getStringExtra(KEY_TEAM_NAME);
    TeamTypeEnum typeEnum = (TeamTypeEnum) getIntent().getSerializableExtra(KEY_TEAM_TYPE);

    if (typeEnum == TeamTypeEnum.Advanced) {
      binding.tvTitle.setText(R.string.team_name_title);
    } else {
      binding.tvTitle.setText(R.string.team_group_name_title);
    }
    teamName = lastTeamName;
    binding.tvCancel.setOnClickListener(v -> finish());
    if (!TextUtils.isEmpty(lastTeamName)) {
      binding.etName.setText(lastTeamName);
      binding.ivClear.setVisibility(View.VISIBLE);
      binding.tvFlag.setText(lastTeamName.length() + MAX_COUNT_STR);
    }
    if (!hasPrivilege) {
      binding.tvSave.setVisibility(View.GONE);
      binding.etName.setEnabled(false);
      binding.ivClear.setVisibility(View.GONE);
    }
    binding.etName.requestFocus();
    binding.ivClear.setOnClickListener(v -> binding.etName.setText(""));
    binding.etName.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(String.valueOf(s))) {
              binding.ivClear.setVisibility(View.GONE);
              binding.tvSave.setAlpha(0.5f);
              binding.tvSave.setEnabled(false);
            } else {
              binding.ivClear.setVisibility(View.VISIBLE);
              binding.tvSave.setAlpha(1f);
              binding.tvSave.setEnabled(true);
            }
            binding.tvFlag.setText(String.valueOf(s).length() + MAX_COUNT_STR);
          }
        });
    binding.tvSave.setOnClickListener(
        v -> model.updateName(teamId, String.valueOf(binding.etName.getText())));
    model
        .getNameData()
        .observe(
            this,
            voidResultInfo -> {
              if (!voidResultInfo.getSuccess()) {
                return;
              }
              if (!TextUtils.equals(lastTeamName, voidResultInfo.getValue())) {
                canUpdate = true;
              }
              teamName = String.valueOf(binding.etName.getText());
              finish();
            });
  }

  @Override
  public void finish() {
    if (hasPrivilege && canUpdate) {
      Intent intent = new Intent();
      intent.putExtra(KEY_TEAM_NAME, teamName);
      setResult(RESULT_OK, intent);
    }
    super.finish();
  }

  public static void launch(
      Context context,
      boolean hasPrivilege,
      TeamTypeEnum typeEnum,
      String teamId,
      String name,
      ActivityResultLauncher<Intent> launcher) {
    Intent intent = new Intent(context, TeamUpdateNameActivity.class);
    intent.putExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, hasPrivilege);
    intent.putExtra(KEY_TEAM_TYPE, typeEnum);
    intent.putExtra(KEY_TEAM_NAME, name);
    intent.putExtra(KEY_TEAM_ID, teamId);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    launcher.launch(intent);
  }
}
