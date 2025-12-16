// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.activity;

import android.os.Bundle;
import android.view.View;
import androidx.viewbinding.ViewBinding;
import com.netease.nimlib.coexist.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamMemberListActivity;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamMemberListActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamMemberListItemBinding;
import com.netease.yunxin.kit.teamkit.ui.normal.adapter.TeamMemberListAdapter;

/**
 * 群成员列表页面,差异化UI展示
 *
 * <p>
 */
public class TeamMemberListActivity extends BaseTeamMemberListActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    TeamMemberListActivityBinding binding =
        TeamMemberListActivityBinding.inflate(getLayoutInflater());
    ivBack = binding.viewTitle.getBackImageView();
    ivClear = binding.ivClear;
    rvMemberList = binding.rvMemberList;
    groupEmpty = binding.groupEmtpy;
    etSearch = binding.etSearch;
    tvTitle = binding.viewTitle.getTitleTextView();
    return binding.getRoot();
  }

  @Override
  protected BaseTeamMemberListAdapter<? extends ViewBinding> getMemberListAdapter(
      V2NIMTeamType typeEnum) {
    BaseTeamMemberListAdapter listAdapter =
        new TeamMemberListAdapter(this, typeEnum, TeamMemberListItemBinding.class);
    return listAdapter;
  }
}
