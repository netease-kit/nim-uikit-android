// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamSettingActivity;
import com.netease.yunxin.kit.teamkit.ui.adapter.TeamCommonAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamSettingActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamSettingUserItemBinding;
import com.netease.yunxin.kit.teamkit.ui.dialog.BaseTeamIdentifyDialog;
import com.netease.yunxin.kit.teamkit.ui.normal.adapter.TeamSettingMemberAdapter;
import com.netease.yunxin.kit.teamkit.ui.normal.dialog.TeamIdentifyDialog;

/** team setting activity */
public class TeamSettingActivity extends BaseTeamSettingActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    TeamSettingActivityBinding binding = TeamSettingActivityBinding.inflate(getLayoutInflater());
    bg3 = binding.bg3;
    bg4 = binding.bg4;
    ivIcon = binding.ivIcon;
    tvName = binding.tvName;
    tvInviteOtherValue = binding.tvInviteOtherValue;
    tvUpdateInfoValue = binding.tvUpdateInfoValue;
    tvHistory = binding.tvHistory;
    tvMark = binding.tvMark;
    tvCount = binding.tvCount;
    tvMember = binding.tvMember;
    tvQuit = binding.tvQuit;
    tvTeamNickname = binding.tvTeamNickname;
    tvUpdateInfoPermission = binding.tvUpdateInfoPermission;
    tvInviteOtherPermission = binding.tvInviteOtherPermission;
    nicknameGroup = binding.nicknameGroup;
    teamMuteGroup = binding.teamMuteGroup;
    inviteGroup = binding.inviteGroup;
    updateGroup = binding.updateGroup;
    swSessionPin = binding.swSessionPin;
    swMessageTip = binding.swMessageTip;
    swTeamMute = binding.swTeamMute;
    ivBack = binding.ivBack;
    ivAdd = binding.ivAdd;
    rvMemberList = binding.rvMemberList;
    return binding.getRoot();
  }

  @Override
  protected TeamCommonAdapter<UserInfoWithTeam, ?> getTeamMemberAdapter() {
    return new TeamSettingMemberAdapter(this, TeamSettingUserItemBinding.class);
  }

  protected String getContactSelectorRouterPath() {
    return RouterConstant.PATH_CONTACT_SELECTOR_PAGE;
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

  protected BaseTeamIdentifyDialog getTeamIdentifyDialog() {
    return new TeamIdentifyDialog(this);
  }
}
