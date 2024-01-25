// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.teamkit.ui.dialog.BaseTeamIdentifyDialog.TYPE_TEAM_ALL_MEMBER;
import static com.netease.yunxin.kit.teamkit.ui.utils.NetworkUtilsWrapper.handleNetworkBrokenResult;
import static com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant.TYPE_EXTENSION_NOTIFY_ALL;
import static com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant.TYPE_EXTENSION_NOTIFY_MANAGER;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamUpdateModeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.kit.chatkit.model.TeamWithCurrentMember;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.dialog.BaseTeamIdentifyDialog;
import com.netease.yunxin.kit.teamkit.ui.normal.dialog.TeamIdentifyDialog;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;
import java.util.List;
import java.util.Objects;

/** team manager activity */
public abstract class BaseTeamManagerActivity extends BaseActivity {
  protected final TeamSettingViewModel model = new TeamSettingViewModel();
  protected String teamId;
  protected Team teamInfo;
  protected TeamMember teamMember;
  private View rootView;

  protected View backView;
  protected View viewEditManager;
  protected TextView tvManagerCount;
  protected View viewInvite;
  protected TextView tvInviteValue;

  protected View viewUpdate;

  protected TextView tvUpdateValue;

  protected View viewAit;

  protected TextView tvAitValue;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView(savedInstanceState);
    setContentView(rootView);
    changeStatusBarColor(R.color.color_eff1f4);
    initData();
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  protected void checkViews() {
    Objects.requireNonNull(viewEditManager);
    Objects.requireNonNull(tvManagerCount);
    Objects.requireNonNull(viewInvite);
    Objects.requireNonNull(tvInviteValue);
    Objects.requireNonNull(viewUpdate);
    Objects.requireNonNull(tvUpdateValue);
    Objects.requireNonNull(viewAit);
    Objects.requireNonNull(tvAitValue);
  }

  protected void initData() {
    teamId = getIntent().getStringExtra(KEY_TEAM_ID);
    model.configTeamId(teamId);
    model.getUpdateInvitePrivilegeData().observeForever(observerForUpdateInviteModePrivilege);
    model.getUpdateInfoPrivilegeData().observeForever(observerForUpdateInfoPrivilege);
    model.getUpdateNotifyAllPrivilegeData().observeForever(observerForTeamNotifyAllPrivilege);
    model.getTeamWithMemberData().observeForever(observerForTeamData);
    model.getTeamMemberUpdateData().observeForever(observerForTeamMemberUpdateData);
    model.getTeamUpdateData().observeForever(observerForTeamUpdateData);
    model.getUserInfoData().observeForever(observerTeamMemberList);
    model.requestTeamData(teamId);
    model.requestTeamMembers(teamId);
  }

  protected void refreshUI(Team teamInfo) {
    tvInviteValue.setText(
        teamInfo.getTeamInviteMode() == TeamInviteModeEnum.All
            ? R.string.team_all_member
            : R.string.team_owner_and_manager);
    viewInvite.setOnClickListener(
        v -> getTeamIdentifyDialog().show(type -> model.updateInvitePrivilege(teamId, type)));

    tvUpdateValue.setText(
        teamInfo.getTeamUpdateMode() == TeamUpdateModeEnum.All
            ? R.string.team_all_member
            : R.string.team_owner_and_manager);
    viewUpdate.setOnClickListener(
        v -> getTeamIdentifyDialog().show(type -> model.updateInfoPrivilege(teamId, type)));

    tvAitValue.setText(
        Objects.equals(TeamUtils.getTeamNotifyAllMode(teamInfo), TYPE_EXTENSION_NOTIFY_ALL)
            ? R.string.team_all_member
            : R.string.team_owner_and_manager);
    viewAit.setOnClickListener(
        v ->
            getTeamIdentifyDialog()
                .show(
                    type ->
                        model.updateNotifyAllPrivilege(
                            teamInfo,
                            type == TYPE_TEAM_ALL_MEMBER
                                ? TYPE_EXTENSION_NOTIFY_ALL
                                : TYPE_EXTENSION_NOTIFY_MANAGER)));

    if (TextUtils.equals(IMKitClient.account(), teamInfo.getCreator())) {
      viewEditManager.setVisibility(View.VISIBLE);
    } else {
      viewEditManager.setVisibility(View.GONE);
    }

    viewEditManager.setOnClickListener(v -> startTeamManagerListActivity());
    if (backView != null) {
      backView.setOnClickListener(v -> finish());
    }
  }

  protected void startTeamManagerListActivity() {}

  @Override
  protected void onDestroy() {
    super.onDestroy();
    model.getUpdateInvitePrivilegeData().removeObserver(observerForUpdateInviteModePrivilege);
    model.getUpdateInfoPrivilegeData().removeObserver(observerForUpdateInfoPrivilege);
    model.getUpdateNotifyAllPrivilegeData().removeObserver(observerForTeamNotifyAllPrivilege);
    model.getTeamMemberUpdateData().observeForever(observerForTeamMemberUpdateData);
    model.getTeamWithMemberData().removeObserver(observerForTeamData);
    model.getTeamUpdateData().removeObserver(observerForTeamUpdateData);
    model.getUserInfoData().removeObserver(observerTeamMemberList);
  }

  protected BaseTeamIdentifyDialog getTeamIdentifyDialog() {
    return new TeamIdentifyDialog(this);
  }

  private final Observer<ResultInfo<Team>> observerForTeamUpdateData =
      teamResultInfo -> {
        dismissLoading();
        if (teamResultInfo.getValue() == null || !teamResultInfo.getSuccess()) {
          finish();
          return;
        }
        teamInfo = teamResultInfo.getValue();
        refreshUI(teamInfo);
      };
  private final Observer<ResultInfo<List<TeamMember>>> observerForTeamMemberUpdateData =
      teamResultInfo -> {
        dismissLoading();
        if (teamResultInfo.getValue() == null || !teamResultInfo.getSuccess()) {
          finish();
          return;
        }
        model.requestTeamMembers(teamId);
      };
  private final Observer<ResultInfo<TeamWithCurrentMember>> observerForTeamData =
      teamResultInfo -> {
        dismissLoading();
        if (teamResultInfo.getValue() == null || !teamResultInfo.getSuccess()) {
          finish();
          return;
        }
        teamInfo = teamResultInfo.getValue().getTeam();
        teamMember = teamResultInfo.getValue().getTeamMember();
        refreshUI(teamInfo);
      };
  private final Observer<ResultInfo<String>> observerForTeamNotifyAllPrivilege =
      stringResultInfo -> {
        if (!stringResultInfo.getSuccess() || stringResultInfo.getValue() == null) {
          handleNetworkBrokenResult(this, stringResultInfo);
          return;
        }
        tvAitValue.setText(
            (Objects.equals(stringResultInfo.getValue(), TYPE_EXTENSION_NOTIFY_ALL))
                ? R.string.team_all_member
                : R.string.team_owner_and_manager);
      };
  private final Observer<ResultInfo<Integer>> observerForUpdateInfoPrivilege =
      resultInfo -> {
        if (!resultInfo.getSuccess() || resultInfo.getValue() == null) {
          handleNetworkBrokenResult(this, resultInfo);
          return;
        }
        tvUpdateValue.setText(
            (resultInfo.getValue() == TeamInviteModeEnum.All.getValue())
                ? R.string.team_all_member
                : R.string.team_owner_and_manager);
      };
  private final Observer<ResultInfo<Integer>> observerForUpdateInviteModePrivilege =
      resultInfo -> {
        if (!resultInfo.getSuccess() || resultInfo.getValue() == null) {
          handleNetworkBrokenResult(this, resultInfo);
          return;
        }
        tvInviteValue.setText(
            (resultInfo.getValue() == TeamInviteModeEnum.All.getValue())
                ? R.string.team_all_member
                : R.string.team_owner_and_manager);
      };

  private final Observer<ResultInfo<List<UserInfoWithTeam>>> observerTeamMemberList =
      resultInfo -> {
        if (!resultInfo.getSuccess() || resultInfo.getValue() == null) {
          handleNetworkBrokenResult(this, resultInfo);
          return;
        }
        tvManagerCount.setText(String.valueOf(TeamUtils.getManagerCount(resultInfo.getValue())));
      };
}
