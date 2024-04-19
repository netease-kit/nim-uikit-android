// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_ICON;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_NAME;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;
import static com.netease.yunxin.kit.teamkit.ui.TeamKitClient.LIB_TAG;
import static com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateIntroduceActivity.KEY_TEAM_INTRODUCE;
import static com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateNicknameActivity.KEY_TEAM_MY_NICKNAME;
import static com.netease.yunxin.kit.teamkit.ui.utils.NetworkUtilsWrapper.doActionAndFilterNetworkBroken;
import static com.netease.yunxin.kit.teamkit.ui.utils.NetworkUtilsWrapper.handleNetworkBrokenResult;

import android.app.Activity;
import android.content.Context;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamChatBannedMode;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamInviteMode;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.event.BaseEvent;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.adapter.TeamCommonAdapter;
import com.netease.yunxin.kit.teamkit.ui.custom.TeamConfigManager;
import com.netease.yunxin.kit.teamkit.ui.model.EventDef;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.teamkit.ui.utils.NetworkUtilsWrapper;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 群设置界面基类 子类需要实现{@link #initViewAndGetRootView(Bundle)}方法，返回界面的根布局 */
public abstract class BaseTeamSettingActivity extends BaseActivity {

  private static final String TAG = "BaseTeamSettingActivity";
  protected TeamSettingViewModel model;
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

  protected V2NIMTeam teamInfo;
  protected V2NIMTeamMember teamMember;

  protected ActivityResultLauncher<Intent> launcher;
  protected TeamCommonAdapter<TeamMemberWithUserInfo, ?> adapter;
  protected RecyclerView.ItemDecoration itemDecoration;

  protected String teamId;
  protected String teamName;
  protected String teamIntroduce;
  protected String myTeamNickname;
  protected String teamIcon;
  protected List<TeamMemberWithUserInfo> teamMemberInfoList;

  private boolean otherPageEnterFlag = false;

  private boolean pageFirstEnter = true;

  // 监听关闭页面事件，用于群解散或者被踢出群，相关页面需要关闭
  protected final EventNotify<BaseEvent> closeEventNotify =
      new EventNotify<>() {
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
    teamId = getIntent().getStringExtra(KEY_TEAM_ID);
    if (teamId == null) {
      ALog.e(LIB_TAG, "BaseTeamSettingActivity", "prepare data failed.");
      finish();
    }
    configModelObserver();

    // 群设置界面返回监听
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

    NetworkUtils.registerNetworkStatusChangedListener(networkStateListener);
  }

  // 网络状态监听，断网重连需要重新请求Team数据信息
  private final NetworkUtils.NetworkStateListener networkStateListener =
      new NetworkUtils.NetworkStateListener() {
        @Override
        public void onConnected(NetworkUtils.NetworkType networkType) {
          ALog.d(LIB_TAG, TAG, "onConnected");
          prepareData();
        }

        @Override
        public void onDisconnected() {
          ALog.d(LIB_TAG, TAG, "onDisconnected");
        }
      };

  @Override
  protected void onResume() {
    super.onResume();
    if (!pageFirstEnter
        && TeamConfigManager.REFRESH_MEMBER_DATA_REAL_TIME_FOR_BACK
        && otherPageEnterFlag
        && model != null
        && !TextUtils.isEmpty(teamId)) {
      prepareData();
      otherPageEnterFlag = false;
    }
    pageFirstEnter = false;
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

  private void prepareData() {
    model.requestTeamSettingData(teamId);
  }

  private void configModelObserver() {
    model = new ViewModelProvider(this).get(TeamSettingViewModel.class);
    model.configTeamId(teamId);
    // 查询群信息和本人群成员信息回调
    model
        .getTeamWitheMemberData()
        .observeForever(
            result -> {
              dismissLoading();
              if (result.getData() == null || !result.isSuccess()) {
                finish();
                return;
              }
              teamInfo = result.getData().getTeam();
              teamName = teamInfo.getName();
              teamIntroduce = teamInfo.getIntro();
              teamIcon = teamInfo.getAvatar();
              teamMember = result.getData().getTeamMember();
              if (teamMember != null) {
                myTeamNickname = teamMember.getTeamNick();
              }
              refreshUI(teamInfo, teamMember);
            });
    // 监听群信息变更，如果群的邀请模式和编辑群信息权限变更，需要UI进行更新
    model
        .getTeamUpdateData()
        .observeForever(
            result -> {
              if (result.getData() == null || !result.isSuccess()) {
                finish();
                return;
              }
              teamInfo = result.getData();
              teamName = teamInfo.getName();
              teamIntroduce = teamInfo.getIntro();
              teamIcon = teamInfo.getAvatar();
              refreshUI(teamInfo, teamMember);
            });
    // 查询群成员信息监听
    model
        .getTeamMemberListWithUserData()
        .observe(
            this,
            result -> {
              if (result.getData() == null || !result.isSuccess()) {
                return;
              }
              teamMemberInfoList = result.getData();
              refreshMember(teamMemberInfoList);
            });
    // 退出群聊回调
    model
        .getQuitTeamData()
        .observe(
            this,
            result -> {
              if (result.isSuccess()) {
                finish();
                return;
              }
              handleNetworkBrokenResult(this, result);
            });
    // 解散群回调
    model
        .getDismissTeamData()
        .observe(
            this,
            result -> {
              if (result.isSuccess()) {
                finish();
                return;
              }
              handleNetworkBrokenResult(this, result);
            });
    // 群成员变更监听
    model
        .getAddRemoveMembersData()
        .observeForever(
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                model.requestTeamMembers(teamId);
              }
            });

    // 群置顶开关监听
    model
        .getStickData()
        .observeForever(
            resultInfo -> {
              if (!resultInfo.isSuccess()) {
                handleNetworkBrokenResult(this, resultInfo);
                swStickTop.toggle();
              } else if (resultInfo.getType() == FetchResult.FetchType.Update) {
                ALog.d(
                    "BaseTeamSettingActivity", "stick top observe Update:" + resultInfo.getData());
                swStickTop.setChecked(Boolean.TRUE.equals(resultInfo.getData()));
              }
            });
    // 群消息提醒开关监听
    model
        .getNotifyData()
        .observeForever(
            resultInfo -> {
              if (!resultInfo.isSuccess()) {
                handleNetworkBrokenResult(this, resultInfo);
                swMessageTip.toggle();
              } else if (resultInfo.getType() == FetchResult.FetchType.Update) {
                swMessageTip.setChecked(Boolean.TRUE.equals(resultInfo.getData()));
              }
            });
    // 群禁言开关监听
    model
        .getMuteTeamAllMemberData()
        .observeForever(
            booleanResultInfo -> {
              if (booleanResultInfo.isSuccess()) {
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
                  && listResultInfo.isSuccess()
                  && listResultInfo.getData() != null) {
                for (V2NIMTeamMember member : Objects.requireNonNull(listResultInfo.getData())) {
                  if (member != null
                      && member.getAccountId().equals(teamMember.getAccountId())
                      && member.getMemberRole() != teamMember.getMemberRole()) {
                    teamMember = member;
                    refreshUI(teamInfo, teamMember);
                    break;
                  }
                }
              }
            });
  }

  private void refreshUI(V2NIMTeam team, V2NIMTeamMember teamMember) {
    initCommonUI(team, teamMember);
    if (TeamUtils.isTeamGroup(team)) {
      // 讨论组UI设置
      initForNormal();
    } else {
      // 高级群UI设置
      initForAdvanced();
    }
  }

  // 初始化UI 讨论组和高级群公用UI
  private void initCommonUI(V2NIMTeam team, V2NIMTeamMember teamMember) {
    ivBack.setOnClickListener(v -> finish());
    ivIcon.setData(teamIcon, teamName, ColorUtils.avatarColor(teamId));
    tvName.setText(team.getName());
    boolean hasPrivilegeToUpdateInfo = TeamUtils.hasUpdateTeamInfoPermission(team, teamMember);
    tvName.setOnClickListener(
        v -> {
          BaseTeamInfoActivity.launch(
              BaseTeamSettingActivity.this,
              getTeamInfoActivity(),
              hasPrivilegeToUpdateInfo,
              teamInfo.getTeamType(),
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
              .withParam(
                  RouterConstant.KEY_SESSION_TYPE,
                  V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM.getValue())
              .withParam(RouterConstant.KEY_SESSION_ID, teamId)
              .withParam(RouterConstant.KEY_SESSION_NAME, teamName)
              .withContext(BaseTeamSettingActivity.this)
              .navigate();
          otherPageEnterFlag = true;
        });
    swMessageTip.setOnClickListener(
        v -> {
          if (NetworkUtilsWrapper.checkNetworkAndToast(this)) {
            model.setTeamNotify(teamId, swMessageTip.isChecked());
          } else {
            swMessageTip.toggle();
          }
        });
    swStickTop.setOnClickListener(
        v -> {
          if (NetworkUtilsWrapper.checkNetworkAndToast(this)) {
            model.stickTop(teamId, swStickTop.isChecked());
          } else {
            swStickTop.toggle();
          }
        });
    tvCount.setText(String.valueOf(team.getMemberCount()));
    tvCount.setOnClickListener(
        v -> {
          BaseTeamMemberListActivity.launch(
              BaseTeamSettingActivity.this, getTeamMemberListActivity(), teamInfo);
          otherPageEnterFlag = true;
        });

    boolean hasPrivilegeToInvite =
        (team.getInviteMode() == V2NIMTeamInviteMode.V2NIM_TEAM_INVITE_MODE_ALL)
            || teamMember.getMemberRole() != V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_NORMAL
            || team.getTeamType() == V2NIMTeamType.V2NIM_TEAM_TYPE_INVALID;
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
  private void refreshMember(List<TeamMemberWithUserInfo> list) {
    rvMemberList.setLayoutManager(new NoScrollLayoutManager(this));
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

  protected TeamCommonAdapter<TeamMemberWithUserInfo, ?> getTeamMemberAdapter() {
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
                      doActionAndFilterNetworkBroken(
                          BaseTeamSettingActivity.this, () -> model.quitTeam(teamInfo));
                    }

                    @Override
                    public void onNegative() {}
                  })
              .show(getSupportFragmentManager());
        });

    nicknameGroup.setVisibility(View.GONE);
    teamMuteGroup.setVisibility(View.GONE);
  }

  /** 高级群UI设置 */
  private void initForAdvanced() {
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
    swTeamMute.setChecked(
        teamInfo.getChatBannedMode() != V2NIMTeamChatBannedMode.V2NIM_TEAM_CHAT_BANNED_MODE_UNBAN);

    V2NIMTeamMemberRole type = teamMember.getMemberRole();
    if (type == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_OWNER) {
      initForOwner();
    } else if (type == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER) {
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
                      doActionAndFilterNetworkBroken(
                          BaseTeamSettingActivity.this, () -> model.dismissTeam(teamId));
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
                      doActionAndFilterNetworkBroken(
                          BaseTeamSettingActivity.this, () -> model.quitTeam(teamId));
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
                      doActionAndFilterNetworkBroken(
                          BaseTeamSettingActivity.this, () -> model.quitTeam(teamId));
                    }

                    @Override
                    public void onNegative() {}
                  })
              .show(getSupportFragmentManager());
        });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    NetworkUtils.unregisterNetworkStatusChangedListener(networkStateListener);
  }

  public static class NoScrollLayoutManager extends LinearLayoutManager {
    public NoScrollLayoutManager(Context context) {
      super(context, RecyclerView.HORIZONTAL, false);
    }

    @Override
    public boolean canScrollHorizontally() {
      return false;
    }

    @Override
    public boolean canScrollVertically() {
      return false;
    }
  }
}
