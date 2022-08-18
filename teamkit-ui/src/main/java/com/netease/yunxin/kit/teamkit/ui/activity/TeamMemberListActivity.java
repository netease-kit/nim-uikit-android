// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.adapter.TeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamMemberListActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamMemberListItemBinding;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;

/** team member list activity */
public class TeamMemberListActivity extends BaseActivity {
  public static final String KEY_TEAM_INFO = "team_info";
  private final TeamSettingViewModel model = new TeamSettingViewModel();
  private TeamMemberListActivityBinding binding;
  private String teamId;
  private TeamMemberListAdapter adapter;
  private TeamTypeEnum teamTypeEnum;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = TeamMemberListActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    changeStatusBarColor(R.color.color_white);

    Team teamInfo = (Team) getIntent().getSerializableExtra(KEY_TEAM_INFO);
    teamId = teamInfo.getId();
    teamTypeEnum = teamInfo.getType();

    initUI();
    configViewModel();
  }

  private void initUI() {
    binding.ivBack.setOnClickListener(v -> finish());
    binding.rvMemberList.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    adapter = new TeamMemberListAdapter(this, teamTypeEnum, TeamMemberListItemBinding.class);
    binding.rvMemberList.setAdapter(adapter);
    binding.ivClear.setOnClickListener(v -> binding.etSearch.setText(null));
    binding.etSearch.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (adapter != null) {
              adapter.filter(s);
            }

            if (TextUtils.isEmpty(String.valueOf(s))) {
              binding.ivClear.setVisibility(View.GONE);
            } else {
              binding.ivClear.setVisibility(View.VISIBLE);
            }
          }
        });
  }

  private void configViewModel() {
    showLoading();
    model.requestTeamMembers(teamId);
    model
        .getUserInfoData()
        .observe(
            this,
            listResultInfo -> {
              dismissLoading();
              if (listResultInfo.getSuccess()) {
                adapter.addDataList(listResultInfo.getValue(), true);
              }
            });
  }

  public static void launch(Context context, Team team) {
    Intent intent = new Intent(context, TeamMemberListActivity.class);
    intent.putExtra(KEY_TEAM_INFO, team);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }
}
