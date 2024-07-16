// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.activity;

import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_ID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamSettingActivity;
import com.netease.yunxin.kit.teamkit.ui.adapter.TeamCommonAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamSettingActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamSettingUserItemBinding;
import com.netease.yunxin.kit.teamkit.ui.normal.adapter.TeamSettingMemberAdapter;

/**
 * 普通版群设置页面，差异化UI展示
 *
 * <p>
 */
public class TeamSettingActivity extends BaseTeamSettingActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    TeamSettingActivityBinding binding = TeamSettingActivityBinding.inflate(getLayoutInflater());
    bg3 = binding.bg3;
    ivIcon = binding.ivIcon;
    tvName = binding.tvName;
    tvHistory = binding.tvHistory;
    tvMark = binding.tvMark;
    tvCount = binding.tvCount;
    tvMember = binding.tvMember;
    tvQuit = binding.tvQuit;
    tvTeamNickname = binding.tvTeamNickname;
    tvTeamManager = binding.tvManager;
    nicknameGroup = binding.nicknameGroup;
    teamMuteGroup = binding.teamMuteGroup;
    swStickTop = binding.swStickTop;
    swMessageTip = binding.swMessageTip;
    swTeamMute = binding.swTeamMute;
    ivBack = binding.ivBack;
    ivAdd = binding.ivAdd;
    rvMemberList = binding.rvMemberList;
    return binding.getRoot();
  }

  @Override
  protected TeamCommonAdapter<TeamMemberWithUserInfo, ?> getTeamMemberAdapter() {
    return new TeamSettingMemberAdapter(this, TeamSettingUserItemBinding.class);
  }

  protected String getContactSelectorRouterPath() {
    String path = RouterConstant.PATH_CONTACT_SELECTOR_PAGE;
    if (IMKitConfigCenter.getEnableAIUser()) {
      path = RouterConstant.PATH_CONTACT_AI_SELECTOR_PAGE;
    }
    return path;
  }

  protected String getPinRouterPath() {
    return RouterConstant.PATH_CHAT_PIN_PAGE;
  }

  protected String getHistoryRouterPath() {
    return RouterConstant.PATH_CHAT_SEARCH_PAGE;
  }

  protected Class<? extends Activity> getUpdateNickNameActivity() {
    return TeamUpdateNicknameActivity.class;
  }

  protected Class<? extends Activity> getTeamMemberListActivity() {
    return TeamMemberListActivity.class;
  }

  protected Class<? extends Activity> getTeamInfoActivity() {
    return TeamInfoActivity.class;
  }

  @Override
  protected void toManagerPage() {
    Intent intent = new Intent(this, TeamManagerActivity.class);
    intent.putExtra(KEY_TEAM_ID, teamId);
    startActivity(intent);
  }
}
