// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_ID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamSettingActivity;
import com.netease.yunxin.kit.teamkit.ui.adapter.TeamCommonAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingUserItemBinding;
import com.netease.yunxin.kit.teamkit.ui.fun.adapter.FunTeamSettingMemberAdapter;

/**
 * 娱乐版群设置页面，差异化UI展示
 *
 * <p>
 */
public class FunTeamSettingActivity extends BaseTeamSettingActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_white);
  }

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunTeamSettingActivityBinding binding =
        FunTeamSettingActivityBinding.inflate(getLayoutInflater());
    bg3 = binding.bg3;
    ivIcon = binding.ivIcon;
    tvName = binding.tvName;
    tvId = binding.tvSubscribe;
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
    ivBack = binding.viewTitle.getBackImageView();
    ivAdd = binding.ivAdd;
    rvMemberList = binding.rvMemberList;
    toTeamDetail = binding.llName;
    return binding.getRoot();
  }

  @Override
  protected TeamCommonAdapter<TeamMemberWithUserInfo, ?> getTeamMemberAdapter() {
    return new FunTeamSettingMemberAdapter(this, FunTeamSettingUserItemBinding.class);
  }

  protected String getContactSelectorRouterPath() {
    String path = RouterConstant.PATH_FUN_CONTACT_SELECTOR_PAGE;
    if (IMKitConfigCenter.getEnableAIUser()) {
      path = RouterConstant.PATH_FUN_CONTACT_AI_SELECTOR_PAGE;
    }
    return path;
  }

  protected String getPinRouterPath() {
    return RouterConstant.PATH_FUN_CHAT_PIN_PAGE;
  }

  protected String getHistoryRouterPath() {
    return RouterConstant.PATH_FUN_CHAT_SEARCH_PAGE;
  }

  protected Class<? extends Activity> getUpdateNickNameActivity() {
    return FunTeamUpdateNicknameActivity.class;
  }

  protected Class<? extends Activity> getTeamMemberListActivity() {
    return FunTeamMemberListActivity.class;
  }

  protected Class<? extends Activity> getTeamInfoActivity() {
    return FunTeamInfoActivity.class;
  }

  @Override
  protected void toManagerPage() {
    Intent intent = new Intent(this, FunTeamManagerActivity.class);
    intent.putExtra(KEY_TEAM_ID, teamId);
    startActivity(intent);
  }
}
