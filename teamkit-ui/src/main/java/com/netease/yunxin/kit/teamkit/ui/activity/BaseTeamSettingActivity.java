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
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
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
import com.netease.yunxin.kit.teamkit.ui.model.EventDef;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** team setting activity */
public abstract class BaseTeamSettingActivity extends BaseActivity {
  protected final TeamSettingViewModel model = new TeamSettingViewModel();
  private View rootView;
  protected View bg3;
  protected ContactAvatarView ivIcon;
  protected TextView tvName;
  protected TextView tvHistory;
  protected TextView tvMark;
  protected TextView tvCount;
  protected TextView tvMember;
  protected TextView tvQuit;
  protected TextView tvTeamNickname;
  protected TextView tvTeamManager;
  protected Group nicknameGroup;
  protected Group teamMuteGroup;
  protected SwitchCompat swStickTop;
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
    Objects.requireNonNull(ivIcon);
    Objects.requireNonNull(tvName);
    Objects.requireNonNull(tvHistory);
    Objects.requireNonNull(tvMark);
    Objects.requireNonNull(tvCount);
    Objects.requireNonNull(tvMember);
    Objects.requireNonNull(tvQuit);
    Objects.requireNonNull(tvTeamNickname);
    Objects.requireNonNull(tvTeamManager);
    Objects.requireNonNull(nicknameGroup);
    Objects.requireNonNull(teamMuteGroup);
    Objects.requireNonNull(swStickTop);
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
    model.configTeamId(teamId);
    // 查询群信息和本人群成员信息回调
    model
        .getTeamWithMemberData()
        .observeForever(
            teamResultInfo -> {
              dismissLoading();
              if (teamResultInfo.getValue() == null || !teamResultInfo.getSuccess()) {
                finish();
                return;
              }
              teamInfo = teamResultInfo.getValue().getTeam();
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
    // 查询群成员信息监听
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
    // 退出群聊回调
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
    // 解散群回调
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
    // 群成员变更监听
    model
        .getAddRemoveMembersData()
        .observeForever(
            listResultInfo -> {
              if (listResultInfo.getLoadStatus() == LoadStatus.Success) {
                model.requestTeamMembers(teamId);
              }
            });

    // 群置顶开关监听
    model
        .getStickData()
        .observeForever(
            booleanResultInfo -> {
              if (booleanResultInfo.getSuccess()) {
                return;
              }
              handleNetworkBrokenResult(this, booleanResultInfo);
              swStickTop.toggle();
            });
    // 群消息提醒开关监听
    model
        .getNotifyData()
        .observeForever(
            booleanResultInfo -> {
              if (booleanResultInfo.getSuccess()) {
                return;
              }
              handleNetworkBrokenResult(this, booleanResultInfo);
              swMessageTip.toggle();
            });
    // 群禁言开关监听
    model
        .getMuteTeamAllMemberData()
        .observeForever(
            booleanResultInfo -> {
              if (booleanResultInfo.getSuccess()) {
                return;
              }
              handleNetworkBrokenResult(this, booleanResultInfo);
              swTeamMute.toggle();
            });
    // 监听群成员信息变更，如果当前账号在群众身份发送变化，筛选UI，管理员变更成普通成员
    model
        .getTeamMemberUpdateData()
        .observeForever(
            listResultInfo -> {
              if (teamMember != null
                  && listResultInfo.getSuccess()
                  && listResultInfo.getValue() != null) {
                for (TeamMember member : Objects.requireNonNull(listResultInfo.getValue())) {
                  if (member != null
                      && member.getAccount().equals(teamMember.getAccount())
                      && member.getType() != teamMember.getType()) {
                    teamMember = member;
                    refreshUI(teamInfo, teamMember);
                    break;
                  }
                }
              }
            });
  }

  private void refreshUI(Team team, TeamMember teamMember) {
    initForCommon(team, teamMember);
    if (TeamUtils.isTeamGroup(team)) {
      // 讨论组UI设置
      initForNormal();
    } else {
      // 高级群UI设置
      initForAdvanced(teamMember);
    }
  }

  // 初始化UI 讨论组和高级群公用UI
  private void initForCommon(Team team, TeamMember teamMember) {
    ivBack.setOnClickListener(v -> finish());
    ivIcon.setData(teamIcon, teamName, ColorUtils.avatarColor(teamId));
    tvName.setText(team.getName());

    boolean hasPrivilegeToUpdateInfo =
        (team.getTeamUpdateMode() == TeamUpdateModeEnum.All)
            || (teamMember != null
                && teamMember.getType() != TeamMemberType.Normal
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
              .withParam(RouterConstant.KEY_SESSION_NAME, teamName)
              .withContext(BaseTeamSettingActivity.this)
              .navigate();
          otherPageEnterFlag = true;
        });
    swMessageTip.setChecked(team.getMessageNotifyType() == TeamMessageNotifyTypeEnum.All);
    swMessageTip.setOnClickListener(v -> model.setTeamNotify(teamId, !swMessageTip.isChecked()));
    swStickTop.setChecked(isStickTop);
    swStickTop.setOnClickListener(v -> model.configStick(teamId, swStickTop.isChecked()));
    tvCount.setText(String.valueOf(team.getMemberCount()));
    tvCount.setOnClickListener(
        v -> {
          BaseTeamMemberListActivity.launch(
              BaseTeamSettingActivity.this, getTeamMemberListActivity(), teamInfo);
          otherPageEnterFlag = true;
        });

    boolean hasPrivilegeToInvite =
        (team.getTeamInviteMode() == TeamInviteModeEnum.All)
            || (teamMember != null
                && teamMember.getType() != TeamMemberType.Normal
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
                            TeamUtils.getAccIdListFromInfoList(teamMemberInfoList))
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

  protected void toManagerPage() {}

  // 刷新群成员信息
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

  /** 讨论组UI设置 */
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
  }

  /**
   * 高级群UI设置
   *
   * @param teamMember
   */
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

    TeamMemberType type = teamMember != null ? teamMember.getType() : TeamMemberType.Normal;
    if (type == TeamMemberType.Owner) {
      initForOwner();
    } else if (type == TeamMemberType.Manager) {
      initForManager();
    } else {
      initForAllUser();
    }
  }

  /** 群主UI设置 */
  private void initForOwner() {
    tvTeamManager.setVisibility(View.VISIBLE);
    tvTeamManager.setOnClickListener(v -> toManagerPage());
    teamMuteGroup.setVisibility(View.VISIBLE);
    swTeamMute.setChecked(teamInfo.isAllMute());
    swTeamMute.setOnClickListener(v -> model.muteTeamAllMember(teamId, swTeamMute.isChecked()));
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

  /** 管理员UI设置 */
  private void initForManager() {
    tvTeamManager.setVisibility(View.VISIBLE);
    tvTeamManager.setOnClickListener(v -> toManagerPage());
    teamMuteGroup.setVisibility(View.GONE);
    swTeamMute.setChecked(teamInfo.isAllMute());
    swTeamMute.setOnClickListener(v -> model.muteTeamAllMember(teamId, swTeamMute.isChecked()));
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

  /** 普通成员UI设置 */
  private void initForAllUser() {
    teamMuteGroup.setVisibility(View.GONE);
    tvTeamManager.setVisibility(View.GONE);
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
}
