// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.activity;

import android.os.Bundle;
import android.view.View;
import androidx.viewbinding.ViewBinding;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamMemberListActivity;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamMemberListActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamMemberListItemBinding;
import com.netease.yunxin.kit.teamkit.ui.normal.adapter.TeamMemberListAdapter;

/** team member list activity */
public class TeamMemberListActivity extends BaseTeamMemberListActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    TeamMemberListActivityBinding binding =
        TeamMemberListActivityBinding.inflate(getLayoutInflater());
    ivBack = binding.ivBack;
    ivClear = binding.ivClear;
    rvMemberList = binding.rvMemberList;
    groupEmtpy = binding.groupEmtpy;
    etSearch = binding.etSearch;
    return binding.getRoot();
  }

  @Override
  protected BaseTeamMemberListAdapter<? extends ViewBinding> getMemberListAdapter(
      TeamTypeEnum typeEnum) {
    return new TeamMemberListAdapter(this, typeEnum, TeamMemberListItemBinding.class);
  }
}
