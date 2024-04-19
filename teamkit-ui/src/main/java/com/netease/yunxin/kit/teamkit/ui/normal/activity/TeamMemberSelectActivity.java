// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.activity;

import android.os.Bundle;
import android.view.View;
import androidx.viewbinding.ViewBinding;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamMemberSelectActivity;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamMemberListItemBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamMemberSelectActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.normal.adapter.TeamMemberListAdapter;

/**
 * 群成员选择页面,差异化UI展示
 *
 * <p>
 */
public class TeamMemberSelectActivity extends BaseTeamMemberSelectActivity {

  private TeamMemberSelectActivityBinding viewBinding;

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    viewBinding = TeamMemberSelectActivityBinding.inflate(getLayoutInflater());
    ivBack = viewBinding.tvCancel;
    ivClear = viewBinding.ivClear;
    tvSure = viewBinding.tvSure;
    rvMemberList = viewBinding.rvMemberList;
    groupEmpty = viewBinding.groupEmtpy;
    etSearch = viewBinding.etSearch;

    return viewBinding.getRoot();
  }

  @Override
  protected BaseTeamMemberListAdapter<? extends ViewBinding> getMemberListAdapter(
      V2NIMTeamType typeEnum) {
    TeamMemberListAdapter adapter =
        new TeamMemberListAdapter(this, typeEnum, TeamMemberListItemBinding.class);
    adapter.showSelect(true);
    adapter.setItemClickListener(
        (action, view, data, position) -> {
          updateMemberSelect();
        });
    return adapter;
  }

  @Override
  protected void teamMemberUpdate() {
    super.teamMemberUpdate();
    updateMemberSelect();
  }

  private void updateMemberSelect() {
    int count = adapter.getSelectData().size();
    if (count > 0) {
      viewBinding.tvSure.setText(
          String.format(getString(R.string.team_sure_with_count), String.valueOf(count)));
    } else {
      viewBinding.tvSure.setText(getString(R.string.team_sure));
    }
  }
}
