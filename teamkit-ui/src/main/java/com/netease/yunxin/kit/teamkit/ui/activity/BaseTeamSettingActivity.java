// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ICON;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_NAME;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;
import static com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateIntroduceActivity.KEY_TEAM_INTRODUCE;
import static com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateNicknameActivity.KEY_TEAM_MY_NICKNAME;
import static com.netease.yunxin.kit.teamkit.ui.utils.NetworkUtilsWrapper.doActionAndFilterNetworkBroken;
import static com.netease.yunxin.kit.teamkit.ui.utils.NetworkUtilsWrapper.handleNetworkBrokenResult;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.team.constant.TeamBeInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.constant.TeamUpdateModeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.event.BaseEvent;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.adapter.TeamCommonAdapter;
import com.netease.yunxin.kit.teamkit.ui.custom.TeamConfigManager;
import com.netease.yunxin.kit.teamkit.ui.dialog.BaseTeamIdentifyDialog;
import com.netease.yunxin.kit.teamkit.ui.model.EventDef;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import com.netease.yunxin.kit.teamkit.ui.utils.viewmodel.TeamSettingViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** team setting activity */
public abstract class BaseTeamSettingActivity extends BaseActivity {
  protected final TeamSettingViewModel model = new TeamSettingViewModel();
  private View rootView;
  protected View bg3;
  protected View bg4;
  protected ContactAvatarView ivIcon;
  protected TextView tvName;
  protected TextView tvInviteOtherValue;
  protected TextView tvUpdateInfoValue;
  protected TextView tvHistory;
  protected TextView tvMark;
  protected TextView tvCount;
  protected TextView tvMember;
  protected TextView tvQuit;
  protected TextView tvTeamNickname;
  protected TextView tvUpdateInfoPermission;
  protected TextView tvInviteOtherPermission;
  protected Group nicknameGroup;
  protected Group teamMuteGroup;
  protected Group inviteGroup;
  protected Group updateGroup;
  protected SwitchCompat swSessionPin;
  protected SwitchCompat swMessageTip;
  protected SwitchCompat swTeamMute;
  protected ImageView ivBack;
  protected ImageView ivAdd;
  protected RecyclerView rvMemberList;

  protected Team teamInfo;
  protected TeamMember teamMember;

  protected ActivityResultLauncher<Intent> launcher;
  protected TeamCommonAdapter<UserInfoWithTeam, ?> adapter;
  protected RecyclerView.ItemDecoration itemDecoration;

  protected String teamId;
  protected String teamName;
  protected String teamIntroduce;
  protected String myTeamNickname;
  protected String teamIcon;
  protected boolean isStickTop;
  protected TeamBeInviteModeEnum beInviteModeEnum;

  protected List<UserInfoWithTeam> teamMemberInfoList;

  private boolean otherPageEnterFlag = false;

  protected final EventNotify<BaseEvent> closeEventNotify =
      new EventNotify<BaseEvent>() {
        @Override
        public void onNotify(@NonNull BaseEvent message) {
          finish();
        }

        @NonNull
        @Override
        public String getEventType() {
          return EventDef.EVENT_TYPE_CLOSE_CHAT_PAGE;
        }
      };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EventCenter.registerEventNotify(closeEventNotify);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);

    changeStatusBarColor(R.color.color_eff1f4);
    showLoading();
    if (!prepareData()) {
      ALog.e("BaseTeamSettingActivity", "prepare data failed.");
      finish();
    }
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
                ivIcon.setData(teamIcon, teamName, ColorUtils.avatarColor(teamId));
              }
              Object nameObj = intent.getStringExtra(KEY_TEAM_NAME);
              if (nameObj != null) {
                teamName = String.valueOf(nameObj);
                tvName.setText(teamName);
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

  @Override
  protected void onResume() {
    super.onResume();
    if (TeamConfigManager.REFRESH_MEMBER_DATA_REAL_TIME_FOR_BACK
        && otherPageEnterFlag
        && model != null
        && !TextUtils.isEmpty(teamId)) {
      prepareData();
      otherPageEnterFlag = false;
    }
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(bg3);
    Objects.requireNonNull(bg4);
    Objects.requireNonNull(ivIcon);
    Objects.requireNonNull(tvName);
    Objects.requireNonNull(tvInviteOtherValue);
    Objects.requireNonNull(tvUpdateInfoValue);
    Objects.requireNonNull(tvHistory);
    Objects.requireNonNull(tvMark);
    Objects.requireNonNull(tvCount);
    Objects.requireNonNull(tvMember);
    Objects.requireNonNull(tvQuit);
    Objects.requireNonNull(tvTeamNickname);
    Objects.requireNonNull(tvUpdateInfoPermission);
    Objects.requireNonNull(tvInviteOtherPermission);
    Objects.requireNonNull(nicknameGroup);
    Objects.requireNonNull(teamMuteGroup);
    Objects.requireNonNull(inviteGroup);
    Objects.requireNonNull(updateGroup);
    Objects.requireNonNull(swSessionPin);
    Objects.requireNonNull(swMessageTip);
    Objects.requireNonNull(swTeamMute);
    Objects.requireNonNull(ivBack);
    Objects.requireNonNull(ivAdd);
    Objects.requireNonNull(rvMemberList);
  }

  private boolean prepareData() {
    teamId = getIntent().getStringExtra(KEY_TEAM_ID);
    if (teamId == null) {
      return false;
    }
    model.requestTeamData(teamId);
    model.requestTeamMembers(teamId);
    return true;
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
              isStickTop = teamResultInfo.getValue().isStickTop();
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
    model
        .getQuitTeamData()
        .observe(
            this,
            voidResultInfo -> {
              if (voidResultInfo.getSuccess()) {
                finish();
                return;
              }
              handleNetworkBrokenResult(this, voidResultInfo);
            });
    model
        .getDismissTeamData()
        .observe(
            this,
            voidResultInfo -> {
              if (voidResultInfo.getSuccess()) {
                finish();
                return;
              }
              handleNetworkBrokenResult(this, voidResultInfo);
            });
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
                handleNetworkBrokenResult(this, integerResultInfo);
                return;
              }
              tvInviteOtherValue.setText(
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
                handleNetworkBrokenResult(this, integerResultInfo);
                return;
              }
              tvUpdateInfoValue.setText(
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
              handleNetworkBrokenResult(this, booleanResultInfo);
              swSessionPin.toggle();
            });
    model
        .getMuteTeamData()
        .observe(
            this,
            booleanResultInfo -> {
              if (booleanResultInfo.getSuccess()) {
                return;
              }
              handleNetworkBrokenResult(this, booleanResultInfo);
              swMessageTip.toggle();
            });
    model
        .getMuteTeamAllMemberData()
        .observe(
            this,
            booleanResultInfo -> {
              if (booleanResultInfo.getSuccess()) {
                return;
              }
              handleNetworkBrokenResult(this, booleanResultInfo);
              swTeamMute.toggle();
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
              }
              //              swInviteAgree.toggle();
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
    ivBack.setOnClickListener(v -> finish());
    ivIcon.setData(teamIcon, teamName, ColorUtils.avatarColor(teamId));
    tvName.setText(team.getName());

    boolean hasPrivilegeToUpdateInfo =
        (team.getTeamUpdateMode() == TeamUpdateModeEnum.All)
            || (teamMember.getType() != TeamMemberType.Normal
                && teamMember.getType() != TeamMemberType.Apply)
            || team.getType() == TeamTypeEnum.Normal;
    tvName.setOnClickListener(
        v -> {
          BaseTeamInfoActivity.launch(
              BaseTeamSettingActivity.this,
              getTeamInfoActivity(),
              hasPrivilegeToUpdateInfo,
              teamInfo.getType(),
              teamId,
              teamName,
              teamIntroduce,
              teamIcon,
              TeamUtils.isTeamGroup(teamInfo),
              launcher);
          otherPageEnterFlag = true;
        });

    tvHistory.setOnClickListener(
        v -> {
          XKitRouter.withKey(getHistoryRouterPath())
              .withParam(RouterConstant.CHAT_KRY, team)
              .withContext(BaseTeamSettingActivity.this)
              .navigate();
          otherPageEnterFlag = true;
        });

    tvMark.setOnClickListener(
        v -> {
          XKitRouter.withKey(getPinRouterPath())
              .withParam(RouterConstant.KEY_SESSION_TYPE, SessionTypeEnum.Team.getValue())
              .withParam(RouterConstant.KEY_SESSION_ID, teamId)
              .withContext(BaseTeamSettingActivity.this)
              .navigate();
          otherPageEnterFlag = true;
        });
    swMessageTip.setChecked(team.getMessageNotifyType() == TeamMessageNotifyTypeEnum.All);
    swMessageTip.setOnClickListener(v -> model.muteTeam(teamId, !swMessageTip.isChecked()));
    swSessionPin.setChecked(isStickTop);
    swSessionPin.setOnClickListener(v -> model.configStick(teamId, swSessionPin.isChecked()));
    tvCount.setText(String.valueOf(team.getMemberCount()));
    tvCount.setOnClickListener(
        v -> {
          BaseTeamMemberListActivity.launch(
              BaseTeamSettingActivity.this, getTeamMemberListActivity(), teamInfo);
          otherPageEnterFlag = true;
        });

    boolean hasPrivilegeToInvite =
        (team.getTeamInviteMode() == TeamInviteModeEnum.All)
            || (teamMember.getType() != TeamMemberType.Normal
                && teamMember.getType() != TeamMemberType.Apply)
            || team.getType() == TeamTypeEnum.Normal;
    ConstraintLayout.LayoutParams params =
        (ConstraintLayout.LayoutParams) rvMemberList.getLayoutParams();
    if (hasPrivilegeToInvite
        && teamInfo != null
        && teamInfo.getMemberCount() < teamInfo.getMemberLimit()) {
      ivAdd.setVisibility(View.VISIBLE);
      ivAdd.setOnClickListener(
          v ->
              doActionAndFilterNetworkBroken(
                  this,
                  () -> {
                    XKitRouter.withKey(getContactSelectorRouterPath())
                        .withParam(
                            RouterConstant.SELECTOR_CONTACT_FILTER_KEY,
                            getAccIdListFromInfoList(teamMemberInfoList))
                        // max count of the team is 200， 199 exclude self.
                        .withParam(
                            RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT,
                            teamInfo.getMemberLimit() - teamInfo.getMemberCount())
                        .withParam(
                            RouterConstant.KEY_CONTACT_SELECTOR_FINAL_CHECK_COUNT_ENABLE, true)
                        .withContext(BaseTeamSettingActivity.this)
                        .navigate(launcher);
                    otherPageEnterFlag = true;
                  }));
      params.setMarginStart(SizeUtils.dp2px(6));
    } else {
      params.setMarginStart(SizeUtils.dp2px(10));
      ivAdd.setVisibility(View.GONE);
    }
    rvMemberList.setLayoutParams(params);
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
    return null;
  }

  protected Class<? extends Activity> getTeamMemberListActivity() {
    return null;
  }

  protected Class<? extends Activity> getTeamInfoActivity() {
    return null;
  }

  private void refreshMember(List<UserInfoWithTeam> list) {
    tvCount.setText(String.valueOf(list.size()));
    rvMemberList.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    if (adapter == null) {
      adapter = getTeamMemberAdapter();
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
      rvMemberList.addItemDecoration(itemDecoration);
    }

    rvMemberList.setAdapter(adapter);
  }

  protected TeamCommonAdapter<UserInfoWithTeam, ?> getTeamMemberAdapter() {
    return null;
  }

  private void initForNormal() {
    tvMember.setText(R.string.team_group_member_title);
    tvQuit.setText(R.string.team_group_quit);
    tvQuit.setOnClickListener(
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

    nicknameGroup.setVisibility(View.GONE);
    teamMuteGroup.setVisibility(View.GONE);
    bg3.setVisibility(View.GONE);
    inviteGroup.setVisibility(View.GONE);
    updateGroup.setVisibility(View.GONE);
    //    inviteAgreeGroup.setVisibility(View.GONE);
    bg4.setVisibility(View.GONE);
  }

  private void initForAdvanced(TeamMember teamMember) {
    tvMember.setText(R.string.team_member_title);
    nicknameGroup.setVisibility(View.VISIBLE);
    bg3.setVisibility(View.VISIBLE);
    tvTeamNickname.setOnClickListener(
        v -> {
          BaseTeamUpdateNicknameActivity.launch(
              BaseTeamSettingActivity.this,
              getUpdateNickNameActivity(),
              teamId,
              myTeamNickname,
              launcher);
          otherPageEnterFlag = true;
        });

    if (teamMember.getType() == TeamMemberType.Owner) {
      initForOwner();
    } else {
      initForAllUser();
    }
  }

  private void initForOwner() {
    inviteGroup.setVisibility(View.VISIBLE);
    tvInviteOtherValue.setText(
        teamInfo.getTeamInviteMode() == TeamInviteModeEnum.All
            ? R.string.team_all_member
            : R.string.team_owner);
    tvInviteOtherPermission.setOnClickListener(
        v -> getTeamIdentifyDialog().show(type -> model.updateInvitePrivilege(teamId, type)));
    updateGroup.setVisibility(View.VISIBLE);
    tvUpdateInfoValue.setText(
        teamInfo.getTeamUpdateMode() == TeamUpdateModeEnum.All
            ? R.string.team_all_member
            : R.string.team_owner);
    tvUpdateInfoPermission.setOnClickListener(
        v -> getTeamIdentifyDialog().show(type -> model.updateInfoPrivilege(teamId, type)));
    teamMuteGroup.setVisibility(View.VISIBLE);
    swTeamMute.setChecked(teamInfo.isAllMute());
    swTeamMute.setOnClickListener(v -> model.muteTeamAllMember(teamId, swTeamMute.isChecked()));
    //    inviteAgreeGroup.setVisibility(View.VISIBLE);
    bg4.setVisibility(View.VISIBLE);
    //    swInviteAgree.setChecked(
    //        teamInfo.getTeamBeInviteMode() == TeamBeInviteModeEnum.NeedAuth);
    //    swInviteAgree.setOnClickListener(
    //        v -> model.updateBeInviteMode(teamId, swInviteAgree.isChecked()));
    tvQuit.setText(R.string.team_advanced_dismiss);
    tvQuit.setOnClickListener(
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

  protected BaseTeamIdentifyDialog getTeamIdentifyDialog() {
    return null;
  }

  private void initForAllUser() {
    teamMuteGroup.setVisibility(View.GONE);
    updateGroup.setVisibility(View.GONE);
    inviteGroup.setVisibility(View.GONE);
    //    inviteAgreeGroup.setVisibility(View.GONE);
    bg4.setVisibility(View.GONE);
    tvQuit.setText(R.string.team_advanced_quit);
    tvQuit.setOnClickListener(
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
