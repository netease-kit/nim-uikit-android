// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamManagerListActivity;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamManagerListActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamMemberListItemBinding;
import com.netease.yunxin.kit.teamkit.ui.normal.adapter.TeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import java.util.ArrayList;

/**
 * 群管理员列表页面,差异化UI展示
 *
 * <p>
 */
public class TeamManagerListActivity extends BaseTeamManagerListActivity {
  private TeamManagerListActivityBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(viewBinding.getRoot());
  }

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    viewBinding = TeamManagerListActivityBinding.inflate(getLayoutInflater());
    ivBack = viewBinding.ivBack;
    tvAddManager = viewBinding.tvManager;
    rvMemberList = viewBinding.rvMemberList;
    groupEmpty = viewBinding.groupEmtpy;
    return viewBinding.getRoot();
  }

  @Override
  protected BaseTeamMemberListAdapter<? extends ViewBinding> getMemberListAdapter(
      V2NIMTeamType typeEnum) {
    return new TeamMemberListAdapter(this, typeEnum, TeamMemberListItemBinding.class);
  }

  @Override
  protected void startTeamMemberSelector(ActivityResultLauncher launcher) {
    Intent intent = new Intent(this, TeamMemberSelectActivity.class);
    intent.putExtra(TeamUIKitConstant.KEY_TEAM_INFO, teamInfo);
    ArrayList<String> filterList = TeamUtils.getAccIdListFromInfoList(managerList);
    filterList.add(IMKitClient.account());
    intent.putExtra(RouterConstant.SELECTOR_CONTACT_FILTER_KEY, filterList);
    intent.putExtra(
        RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT,
        TeamUIKitConstant.KEY_MANAGER_MAX_COUNT - managerList.size());
    launcher.launch(intent);
  }
}
