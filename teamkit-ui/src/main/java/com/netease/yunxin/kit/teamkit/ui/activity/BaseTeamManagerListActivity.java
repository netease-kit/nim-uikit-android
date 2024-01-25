// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;
import static com.netease.yunxin.kit.teamkit.ui.utils.NetworkUtilsWrapper.doActionAndFilterNetworkBroken;

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
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.event.BaseEvent;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.model.EventDef;
import com.netease.yunxin.kit.teamkit.ui.normal.adapter.TeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class BaseTeamManagerListActivity extends BaseActivity {

  protected TeamSettingViewModel viewModel;

  protected Team teamInfo;

  protected List<UserInfoWithTeam> managerList = new ArrayList<>();

  protected BaseTeamMemberListAdapter<? extends ViewBinding> adapter;
  protected TeamTypeEnum teamTypeEnum;

  private View rootView;
  protected View ivBack;
  protected View tvAddManager;
  protected View groupEmpty;
  protected RecyclerView rvMemberList;
  protected ActivityResultLauncher<Intent> launcher;
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
    changeStatusBarColor(R.color.color_white);
    viewModel = new ViewModelProvider(this).get(TeamSettingViewModel.class);
    teamInfo = (Team) getIntent().getSerializableExtra(TeamUIKitConstant.KEY_TEAM_INFO);
    if (teamInfo == null || TextUtils.isEmpty(teamInfo.getId())) {
      finish();
      return;
    }
    initUI();
    initData();
  }

  @Override
  protected void onStart() {
    super.onStart();
    viewModel.requestTeamMembers(teamInfo.getId());
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  private void initUI() {
    ivBack.setOnClickListener(v -> finish());
    if (teamInfo != null && TextUtils.equals(teamInfo.getCreator(), IMKitClient.account())) {
      tvAddManager.setVisibility(View.VISIBLE);
      tvAddManager.setOnClickListener(
          v -> doActionAndFilterNetworkBroken(this, () -> startTeamMemberSelector(launcher)));
    } else {
      tvAddManager.setVisibility(View.GONE);
    }

    rvMemberList.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    adapter = getMemberListAdapter(teamTypeEnum);
    if (teamInfo != null && TextUtils.equals(teamInfo.getCreator(), IMKitClient.account())) {
      adapter.setShowRemoveTagWithMemberType(TeamMemberType.Manager);
      adapter.setItemClickListener(
          (action, view, data, position) -> {
            if (action.equals(TeamMemberListAdapter.ACTION_REMOVE)) {
              doActionAndFilterNetworkBroken(
                  this,
                  () -> {
                    List<String> accounts = new ArrayList<>();
                    accounts.add(data.getUserInfo().getAccount());
                    showDeleteConfirmDialog(accounts);
                  });
            }
          });
    }
    adapter.setGroupIdentify(false);
    rvMemberList.setAdapter(adapter);
  }

  protected BaseTeamMemberListAdapter<? extends ViewBinding> getMemberListAdapter(
      TeamTypeEnum typeEnum) {
    return null;
  }

  private void initData() {

    viewModel
        .getUserInfoData()
        .observe(
            this,
            listResultInfo -> {
              dismissLoading();
              if (listResultInfo.getSuccess()) {
                managerList.clear();
                for (int i = 0; i < listResultInfo.getValue().size(); i++) {
                  UserInfoWithTeam teamUser = listResultInfo.getValue().get(i);
                  if (teamUser.getTeamInfo().getType() == TeamMemberType.Manager) {
                    managerList.add(teamUser);
                  }
                }
                adapter.addDataList(managerList, true);
                if (adapter.getItemCount() > 0) {
                  groupEmpty.setVisibility(View.GONE);
                } else {
                  groupEmpty.setVisibility(View.VISIBLE);
                }
              }
            });

    viewModel
        .getAddRemoveMembersData()
        .observeForever(
            listResultInfo -> {
              dismissLoading();
              if (listResultInfo.getLoadStatus() == LoadStatus.Success) {
                viewModel.requestTeamMembers(teamInfo.getId());
              }
            });
    viewModel
        .getAddRemoveManagerLiveData()
        .observeForever(
            listResultInfo -> {
              dismissLoading();
              if (listResultInfo.getLoadStatus() == LoadStatus.Success) {
                viewModel.requestTeamMembers(teamInfo.getId());
              } else {
                Toast.makeText(
                        BaseTeamManagerListActivity.this,
                        R.string.team_request_fail,
                        Toast.LENGTH_SHORT)
                    .show();
              }
            });
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
                viewModel.addManager(teamInfo.getId(), memberList);
              }
            });
    viewModel.configTeamId(teamInfo.getId());
  }

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(ivBack);
    Objects.requireNonNull(groupEmpty);
    Objects.requireNonNull(rvMemberList);
    Objects.requireNonNull(tvAddManager);
  }

  protected void startTeamMemberSelector(ActivityResultLauncher launcher) {}

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
                viewModel.removeManager(teamInfo.getId(), accounts);
              }

              @Override
              public void onNegative() {}
            })
        .show(this.getSupportFragmentManager());
  }
}
