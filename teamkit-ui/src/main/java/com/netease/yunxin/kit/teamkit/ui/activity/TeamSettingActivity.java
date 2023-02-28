// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ICON;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_NAME;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;
import static com.netease.yunxin.kit.teamkit.ui.activity.TeamUpdateIntroduceActivity.KEY_TEAM_INTRODUCE;
import static com.netease.yunxin.kit.teamkit.ui.activity.TeamUpdateNicknameActivity.KEY_TEAM_MY_NICKNAME;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.team.constant.TeamBeInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.constant.TeamUpdateModeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.adapter.TeamSettingMemberAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamSettingActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamSettingUserItemBinding;
import com.netease.yunxin.kit.teamkit.ui.dialog.TeamIdentifyDialog;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** team setting activity */
public class TeamSettingActivity extends BaseActivity {

  private TeamSettingActivityBinding binding;
  private final TeamSettingViewModel model = new TeamSettingViewModel();

  private Team teamInfo;
  private TeamMember teamMember;

  private ActivityResultLauncher<Intent> launcher;
  private TeamSettingMemberAdapter adapter;
  private RecyclerView.ItemDecoration itemDecoration;

  private String teamId;
  private String teamName;
  private String teamIntroduce;
  private String myTeamNickname;
  private String teamIcon;
  private TeamBeInviteModeEnum beInviteModeEnum;

  private List<UserInfoWithTeam> teamMemberInfoList;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = TeamSettingActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    changeStatusBarColor(R.color.color_eff1f4);
    showLoading();
    prepareData();
    configModelObserver();

    launcher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                return;
              }
              Intent intent = result.getData();
              ArrayList<String> memberList =
                  intent.getStringArrayListExtra(REQUEST_CONTACT_SELECTOR_KEY);
              if (memberList != null) {
                model.addMembers(teamId, memberList);
              }
              Object iconObj = intent.getStringExtra(KEY_TEAM_ICON);
              if (iconObj != null) {
                teamIcon = String.valueOf(iconObj);
                binding.ivIcon.setData(teamIcon, teamName, ColorUtils.avatarColor(teamId));
              }
              Object nameObj = intent.getStringExtra(KEY_TEAM_NAME);
              if (nameObj != null) {
                teamName = String.valueOf(nameObj);
                binding.tvName.setText(teamName);
              }
              Object introduceObj = intent.getStringExtra(KEY_TEAM_INTRODUCE);
              if (introduceObj != null) {
                teamIntroduce = String.valueOf(introduceObj);
              }
              Object nicknameObj = intent.getStringExtra(KEY_TEAM_MY_NICKNAME);
              if (nicknameObj != null) {
                myTeamNickname = String.valueOf(nicknameObj);
              }
            });
  }

  private void prepareData() {
    teamId = getIntent().getStringExtra(KEY_TEAM_ID);
    if (teamId == null) {
      finish();
      return;
    }
    model.requestTeamData(teamId);
    model.requestTeamMembers(teamId);
  }

  private void configModelObserver() {
    model
        .getTeamWithMemberData()
        .observe(
            this,
            teamResultInfo -> {
              dismissLoading();
              if (teamResultInfo.getValue() == null || !teamResultInfo.getSuccess()) {
                finish();
                return;
              }
              teamInfo = teamResultInfo.getValue().getTeam();
              beInviteModeEnum = teamInfo.getTeamBeInviteMode();
              teamName = teamInfo.getName();
              teamIntroduce = teamInfo.getIntroduce();
              teamIcon = teamInfo.getIcon();
              teamMember = teamResultInfo.getValue().getTeamMember();
              if (teamMember != null) {
                myTeamNickname = teamMember.getTeamNick();
              }
              refreshUI(teamInfo, teamMember);
            });
    model
        .getUserInfoData()
        .observe(
            this,
            userInfoWithTeamResultInfo -> {
              if (userInfoWithTeamResultInfo.getValue() == null
                  || !userInfoWithTeamResultInfo.getSuccess()) {
                return;
              }
              teamMemberInfoList = userInfoWithTeamResultInfo.getValue();
              refreshMember(teamMemberInfoList);
            });
    model.getQuitTeamData().observe(this, voidResultInfo -> finish());
    model.getDismissTeamData().observe(this, voidResultInfo -> finish());
    model
        .getAddMembersData()
        .observe(
            this,
            listResultInfo -> {
              if (teamInfo.getType() == TeamTypeEnum.Normal
                  || beInviteModeEnum == TeamBeInviteModeEnum.NoAuth) {
                model.requestTeamMembers(teamId);
              }
            });
    model
        .getUpdateInvitePrivilegeData()
        .observe(
            this,
            integerResultInfo -> {
              if (!integerResultInfo.getSuccess() || integerResultInfo.getValue() == null) {
                return;
              }
              binding.tvInviteOtherValue.setText(
                  (integerResultInfo.getValue() == TeamInviteModeEnum.All.getValue())
                      ? R.string.team_all_member
                      : R.string.team_owner);
            });
    model
        .getUpdateInfoPrivilegeData()
        .observe(
            this,
            integerResultInfo -> {
              if (!integerResultInfo.getSuccess() || integerResultInfo.getValue() == null) {
                return;
              }
              binding.tvUpdateInfoValue.setText(
                  (integerResultInfo.getValue() == TeamInviteModeEnum.All.getValue())
                      ? R.string.team_all_member
                      : R.string.team_owner);
            });

    model
        .getStickData()
        .observe(
            this,
            booleanResultInfo -> {
              if (booleanResultInfo.getSuccess()) {
                return;
              }
              binding.swSessionPin.toggle();
            });
    model
        .getMuteTeamData()
        .observe(
            this,
            booleanResultInfo -> {
              if (booleanResultInfo.getSuccess()) {
                return;
              }
              binding.swMessageTip.toggle();
            });
    model
        .getMuteTeamAllMemberData()
        .observe(
            this,
            booleanResultInfo -> {
              if (booleanResultInfo.getSuccess()) {
                return;
              }
              binding.swTeamMute.toggle();
            });
    model
        .getBeInvitedNeedAgreedData()
        .observe(
            this,
            booleanResultInfo -> {
              if (booleanResultInfo.getSuccess()) {
                beInviteModeEnum =
                    booleanResultInfo.getValue() == Boolean.TRUE
                        ? TeamBeInviteModeEnum.NeedAuth
                        : TeamBeInviteModeEnum.NoAuth;
                return;
              }
              //              binding.swInviteAgree.toggle();
            });
  }

  private void refreshUI(Team team, TeamMember teamMember) {
    initForCommon(team, teamMember);
    if (TeamUtils.isTeamGroup(team)) {
      initForNormal();
    } else {
      initForAdvanced(teamMember);
    }
  }

  private void initForCommon(Team team, TeamMember teamMember) {
    binding.ivBack.setOnClickListener(v -> finish());
    binding.ivIcon.setData(teamIcon, teamName, ColorUtils.avatarColor(teamId));
    binding.tvName.setText(team.getName());

    boolean hasPrivilegeToUpdateInfo =
        (team.getTeamUpdateMode() == TeamUpdateModeEnum.All)
            || (teamMember.getType() != TeamMemberType.Normal
                && teamMember.getType() != TeamMemberType.Apply)
            || team.getType() == TeamTypeEnum.Normal;
    binding.tvName.setOnClickListener(
        v ->
            TeamInfoActivity.launch(
                TeamSettingActivity.this,
                hasPrivilegeToUpdateInfo,
                teamInfo.getType(),
                teamId,
                teamName,
                teamIntroduce,
                teamIcon,
                TeamUtils.isTeamGroup(teamInfo),
                launcher));

    binding.tvHistory.setOnClickListener(
        v ->
            XKitRouter.withKey(RouterConstant.PATH_CHAT_SEARCH_PAGE)
                .withParam(RouterConstant.CHAT_KRY, team)
                .withContext(TeamSettingActivity.this)
                .navigate());
    binding.swMessageTip.setChecked(team.getMessageNotifyType() == TeamMessageNotifyTypeEnum.All);
    binding.swMessageTip.setOnClickListener(
        v -> model.muteTeam(teamId, !binding.swMessageTip.isChecked()));
    binding.swSessionPin.setChecked(model.isStick(teamId));
    binding.swSessionPin.setOnClickListener(
        v -> model.configStick(teamId, binding.swSessionPin.isChecked()));
    binding.tvCount.setText(String.valueOf(team.getMemberCount()));
    binding.tvCount.setOnClickListener(
        v -> TeamMemberListActivity.launch(TeamSettingActivity.this, teamInfo));

    boolean hasPrivilegeToInvite =
        (team.getTeamInviteMode() == TeamInviteModeEnum.All)
            || (teamMember.getType() != TeamMemberType.Normal
                && teamMember.getType() != TeamMemberType.Apply)
            || team.getType() == TeamTypeEnum.Normal;
    ConstraintLayout.LayoutParams params =
        (ConstraintLayout.LayoutParams) binding.rvMemberList.getLayoutParams();
    if (hasPrivilegeToInvite) {
      binding.ivAdd.setVisibility(View.VISIBLE);
      binding.ivAdd.setOnClickListener(
          v ->
              XKitRouter.withKey(RouterConstant.PATH_CONTACT_SELECTOR_PAGE)
                  .withParam(
                      RouterConstant.SELECTOR_CONTACT_FILTER_KEY,
                      getAccIdListFromInfoList(teamMemberInfoList))
                  // max count of the team is 200， 199 exclude self.
                  .withParam(
                      RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT,
                      teamMemberInfoList == null ? 199 : 200 - teamMemberInfoList.size())
                  .withContext(TeamSettingActivity.this)
                  .navigate(launcher));
      params.setMarginStart(SizeUtils.dp2px(6));
    } else {
      params.setMarginStart(SizeUtils.dp2px(10));
      binding.ivAdd.setVisibility(View.GONE);
    }
    binding.rvMemberList.setLayoutParams(params);
  }

  private void refreshMember(List<UserInfoWithTeam> list) {
    binding.tvCount.setText(String.valueOf(list.size()));
    binding.rvMemberList.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    if (adapter == null) {
      adapter = new TeamSettingMemberAdapter(this, TeamSettingUserItemBinding.class);
    }
    adapter.addDataList(list, true);
    if (itemDecoration == null) {
      int padding = SizeUtils.dp2px(6);
      itemDecoration =
          new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(
                @NonNull Rect outRect,
                @NonNull View view,
                @NonNull RecyclerView parent,
                @NonNull RecyclerView.State state) {
              outRect.set(padding, 0, padding, 0);
            }
          };
      binding.rvMemberList.addItemDecoration(itemDecoration);
    }

    binding.rvMemberList.setAdapter(adapter);
  }

  private void initForNormal() {
    binding.tvMember.setText(R.string.team_group_member_title);
    binding.tvQuit.setText(R.string.team_group_quit);
    binding.tvQuit.setOnClickListener(
        v -> {
          CommonChoiceDialog dialog = new CommonChoiceDialog();
          dialog
              .setTitleStr(getString(R.string.team_group_quit))
              .setContentStr(getString(R.string.team_quit_group_team_query))
              .setNegativeStr(getString(R.string.team_cancel))
              .setPositiveStr(getString(R.string.team_confirm))
              .setConfirmListener(
                  new ChoiceListener() {
                    @Override
                    public void onPositive() {
                      model.quitTeam(teamInfo);
                    }

                    @Override
                    public void onNegative() {}
                  })
              .show(getSupportFragmentManager());
        });
    binding.nicknameGroup.setVisibility(View.GONE);
    binding.teamMuteGroup.setVisibility(View.GONE);
    binding.bg3.setVisibility(View.GONE);
    binding.inviteGroup.setVisibility(View.GONE);
    binding.updateGroup.setVisibility(View.GONE);
    //    binding.inviteAgreeGroup.setVisibility(View.GONE);
    binding.bg4.setVisibility(View.GONE);
  }

  private void initForAdvanced(TeamMember teamMember) {
    binding.tvMember.setText(R.string.team_member_title);
    binding.nicknameGroup.setVisibility(View.VISIBLE);
    binding.bg3.setVisibility(View.VISIBLE);
    binding.tvTeamNickname.setOnClickListener(
        v ->
            TeamUpdateNicknameActivity.launch(
                TeamSettingActivity.this, teamId, myTeamNickname, launcher));

    if (teamMember.getType() == TeamMemberType.Owner) {
      initForOwner();
    } else {
      initForAllUser();
    }
  }

  private void initForOwner() {
    binding.inviteGroup.setVisibility(View.VISIBLE);
    binding.tvInviteOtherValue.setText(
        teamInfo.getTeamInviteMode() == TeamInviteModeEnum.All
            ? R.string.team_all_member
            : R.string.team_owner);
    binding.tvInviteOtherPermission.setOnClickListener(
        v ->
            new TeamIdentifyDialog(TeamSettingActivity.this)
                .show(type -> model.updateInvitePrivilege(teamId, type)));
    binding.updateGroup.setVisibility(View.VISIBLE);
    binding.tvUpdateInfoValue.setText(
        teamInfo.getTeamUpdateMode() == TeamUpdateModeEnum.All
            ? R.string.team_all_member
            : R.string.team_owner);
    binding.tvUpdateInfoPermission.setOnClickListener(
        v ->
            new TeamIdentifyDialog(TeamSettingActivity.this)
                .show(type -> model.updateInfoPrivilege(teamId, type)));
    binding.teamMuteGroup.setVisibility(View.VISIBLE);
    binding.swTeamMute.setChecked(teamInfo.isAllMute());
    binding.swTeamMute.setOnClickListener(
        v -> model.muteTeamAllMember(teamId, binding.swTeamMute.isChecked()));
    //    binding.inviteAgreeGroup.setVisibility(View.VISIBLE);
    binding.bg4.setVisibility(View.VISIBLE);
    //    binding.swInviteAgree.setChecked(
    //        teamInfo.getTeamBeInviteMode() == TeamBeInviteModeEnum.NeedAuth);
    //    binding.swInviteAgree.setOnClickListener(
    //        v -> model.updateBeInviteMode(teamId, binding.swInviteAgree.isChecked()));
    binding.tvQuit.setText(R.string.team_advanced_dismiss);
    binding.tvQuit.setOnClickListener(
        v -> {
          CommonChoiceDialog dialog = new CommonChoiceDialog();
          dialog
              .setTitleStr(getString(R.string.team_advanced_dismiss))
              .setContentStr(getString(R.string.team_dismiss_advanced_team_query))
              .setNegativeStr(getString(R.string.team_cancel))
              .setPositiveStr(getString(R.string.team_confirm))
              .setConfirmListener(
                  new ChoiceListener() {
                    @Override
                    public void onPositive() {
                      model.dismissTeam(teamId);
                    }

                    @Override
                    public void onNegative() {}
                  })
              .show(getSupportFragmentManager());
        });
  }

  private void initForAllUser() {
    binding.teamMuteGroup.setVisibility(View.GONE);
    binding.updateGroup.setVisibility(View.GONE);
    binding.inviteGroup.setVisibility(View.GONE);
    //    binding.inviteAgreeGroup.setVisibility(View.GONE);
    binding.bg4.setVisibility(View.GONE);
    binding.tvQuit.setText(R.string.team_advanced_quit);
    binding.tvQuit.setOnClickListener(
        v -> {
          CommonChoiceDialog dialog = new CommonChoiceDialog();
          dialog
              .setTitleStr(getString(R.string.team_advanced_quit))
              .setContentStr(getString(R.string.team_quit_advanced_team_query))
              .setNegativeStr(getString(R.string.team_cancel))
              .setPositiveStr(getString(R.string.team_confirm))
              .setConfirmListener(
                  new ChoiceListener() {
                    @Override
                    public void onPositive() {
                      model.quitTeam(teamId);
                    }

                    @Override
                    public void onNegative() {}
                  })
              .show(getSupportFragmentManager());
        });
  }

  private List<String> getAccIdListFromInfoList(List<UserInfoWithTeam> sourceList) {
    if (sourceList == null || sourceList.isEmpty()) {
      return Collections.emptyList();
    }
    List<String> result = new ArrayList<>(sourceList.size());

    for (UserInfoWithTeam item : sourceList) {
      if (item == null || item.getUserInfo() == null) {
        continue;
      }
      result.add(item.getUserInfo().getAccount());
    }

    return result;
  }
}
