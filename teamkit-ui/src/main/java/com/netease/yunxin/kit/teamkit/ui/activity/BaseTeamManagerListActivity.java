// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;
import static com.netease.yunxin.kit.teamkit.ui.utils.NetworkUtilsWrapper.doActionAndFilterNetworkBroken;
import static com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant.LIB_TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.netease.nimlib.coexist.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.coexist.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserManager;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.event.BaseEvent;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.model.EventCloseChat;
import com.netease.yunxin.kit.teamkit.ui.normal.adapter.TeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamManagerListViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 群管理员列表页面, 包含添加管理员和删除管理员功能 子类需要实现{@link #initViewAndGetRootView(Bundle)}方法, 并在其中初始化页面布局 */
public abstract class BaseTeamManagerListActivity extends BaseLocalActivity {

  private final String TAG = "BaseTeamManagerListActivity";

  protected TeamManagerListViewModel viewModel;

  protected V2NIMTeam teamInfo;

  protected BaseTeamMemberListAdapter<? extends ViewBinding> adapter;
  protected V2NIMTeamType teamTypeEnum;
  protected List<TeamMemberWithUserInfo> managerList = new ArrayList<>();
  private View rootView;
  protected View ivBack;
  protected View tvAddManager;
  protected View groupEmpty;
  protected RecyclerView rvMemberList;
  protected ActivityResultLauncher<Intent> launcher;
  // 监听关闭页面事件，用于群解散或者被踢出群，相关页面需要关闭
  protected final EventNotify<BaseEvent> closeEventNotify =
      new EventNotify<BaseEvent>() {
        @Override
        public void onNotify(@NonNull BaseEvent message) {
          finish();
        }

        @NonNull
        @Override
        public String getEventType() {
          return EventCloseChat.EVENT_TYPE_CLOSE_CHAT_PAGE;
        }
      };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 注册关闭页面事件
    EventCenter.registerEventNotify(closeEventNotify);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);
    changeStatusBarColor(R.color.color_white);
    viewModel = new ViewModelProvider(this).get(TeamManagerListViewModel.class);
    teamInfo = (V2NIMTeam) getIntent().getSerializableExtra(TeamUIKitConstant.KEY_TEAM_INFO);
    if (teamInfo == null || TextUtils.isEmpty(teamInfo.getTeamId())) {
      finish();
      return;
    }
    initUI();
    initData();
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (!NetworkUtils.isConnected()) {
      dismissLoading();
      Toast.makeText(
              getApplicationContext(), getString(R.string.team_network_error), Toast.LENGTH_SHORT)
          .show();
    }
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  private void initUI() {
    ivBack.setOnClickListener(v -> finish());
    if (teamInfo != null && TextUtils.equals(teamInfo.getOwnerAccountId(), IMKitClient.account())) {
      tvAddManager.setVisibility(View.VISIBLE);
      tvAddManager.setOnClickListener(
          v -> doActionAndFilterNetworkBroken(this, () -> startTeamMemberSelector(launcher)));
    } else {
      tvAddManager.setVisibility(View.GONE);
    }

    rvMemberList.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    adapter = getMemberListAdapter(teamTypeEnum);
    if (teamInfo != null && TextUtils.equals(teamInfo.getOwnerAccountId(), IMKitClient.account())) {
      adapter.setShowRemoveTagWithMemberType(V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER);
      adapter.setItemClickListener(
          (action, view, data, position) -> {
            if (action.equals(TeamMemberListAdapter.ACTION_REMOVE)) {
              doActionAndFilterNetworkBroken(
                  this,
                  () -> {
                    List<String> accounts = new ArrayList<>();
                    accounts.add(data.getAccountId());
                    showDeleteConfirmDialog(accounts);
                  });
            }
          });
    }
    adapter.setGroupIdentify(false);
    rvMemberList.setAdapter(adapter);
  }

  protected BaseTeamMemberListAdapter<? extends ViewBinding> getMemberListAdapter(
      V2NIMTeamType typeEnum) {
    return null;
  }

  private void initData() {
    // 初始化配置ViewModel teamId
    viewModel.configTeamId(teamInfo.getTeamId());
    // 获取群成员列表，更加成员身份过滤管理员
    viewModel
        .getTeamMemberListWithUserData()
        .observe(
            this,
            resultInfo -> {
              dismissLoading();
              ALog.d(LIB_TAG, TAG, "getTeamMemberListWithUserData observe");
              if (resultInfo.isSuccess()) {
                if (resultInfo.getType() == FetchResult.FetchType.Init) {
                  managerList =
                      TeamUtils.filterMemberListWithRole(
                          resultInfo.getData(), V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER);
                  adapter.setDataList(managerList);
                } else if (resultInfo.getType() == FetchResult.FetchType.Add) {
                  List<TeamMemberWithUserInfo> addData =
                      TeamUtils.filterMemberListWithRole(
                          resultInfo.getData(), V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER);
                  managerList.addAll(addData);
                  adapter.addData(addData, TeamUtils.teamManagerComparator());
                } else if (resultInfo.getType() == FetchResult.FetchType.Update) {
                  //管理员添加和删除，走的群成员身份更新
                  List<TeamMemberWithUserInfo> updateData =
                      TeamUserManager.getInstance()
                          .getTeamMemberWithRoleListFromCache(
                              teamInfo.getTeamId(),
                              V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER);
                  managerList.clear();
                  managerList.addAll(updateData);
                  if (managerList.size() > 1) {
                    Collections.sort(managerList, TeamUtils.teamManagerComparator());
                  }
                  adapter.setDataList(managerList);
                }
                if (adapter.getItemCount() > 0) {
                  groupEmpty.setVisibility(View.GONE);
                } else {
                  groupEmpty.setVisibility(View.VISIBLE);
                }
              }
            });

    // 添加或删除群成员员观察者，收到添加和删除员信息后刷新列表
    viewModel
        .getRemoveMembersData()
        .observeForever(
            listResultInfo -> {
              dismissLoading();
              if (listResultInfo.getLoadStatus() == LoadStatus.Success) {
                if (adapter != null
                    && listResultInfo.getData() != null
                    && !listResultInfo.getData().isEmpty()) {
                  adapter.removeData(listResultInfo.getData());
                }
              }
            });
    // 添加管理员观察者，收到添加管理员信息后刷新列表
    viewModel
        .getAddRemoveManagerLiveData()
        .observeForever(
            listResultInfo -> {
              dismissLoading();
              if (listResultInfo.getLoadStatus() == LoadStatus.Success) {
                if (listResultInfo.getType() == FetchResult.FetchType.Remove) {
                  if (adapter != null
                      && listResultInfo.getData() != null
                      && !listResultInfo.getData().isEmpty()) {
                    adapter.removeData(listResultInfo.getData());
                  }
                }
              } else {
                Toast.makeText(
                        BaseTeamManagerListActivity.this,
                        R.string.team_request_fail,
                        Toast.LENGTH_SHORT)
                    .show();
              }
            });

    // Activity Launcher 添加管理员页面返回要添加的人员ID
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
                viewModel.addManager(teamInfo.getTeamId(), memberList);
              }
            });
    viewModel.requestTeamManagers(teamInfo.getTeamId());
  }

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(ivBack);
    Objects.requireNonNull(groupEmpty);
    Objects.requireNonNull(rvMemberList);
    Objects.requireNonNull(tvAddManager);
  }

  protected void startTeamMemberSelector(ActivityResultLauncher launcher) {}

  // 显示删除管理员确认对话框
  private void showDeleteConfirmDialog(List<String> accounts) {
    CommonChoiceDialog dialog = new CommonChoiceDialog();
    dialog
        .setTitleStr(getString(R.string.team_remove_member_title))
        .setContentStr(getString(R.string.team_remove_manager_content))
        .setPositiveStr(getString(R.string.team_confirm))
        .setNegativeStr(getString(R.string.team_cancel))
        .setConfirmListener(
            new ChoiceListener() {
              @Override
              public void onPositive() {
                if (!NetworkUtils.isConnected()) {
                  ToastX.showShortToast(R.string.team_network_error_tip);
                  return;
                }
                viewModel.removeManager(teamInfo.getTeamId(), accounts);
              }

              @Override
              public void onNegative() {}
            })
        .show(this.getSupportFragmentManager());
  }
}
