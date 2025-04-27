// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.teamkit.ui.utils.NetworkUtilsWrapper.doActionAndFilterNetworkBroken;
import static com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant.KEY_TEAM_INFO;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** 群成员选择界面基类 子类需要实现{@link #initViewAndGetRootView(Bundle)}方法，返回界面的根布局 */
public abstract class BaseTeamMemberSelectActivity extends BaseLocalActivity {

  protected TeamSettingViewModel viewModel;
  protected String teamId;
  private final Set<String> filterAccounts = new HashSet<>();
  protected BaseTeamMemberListAdapter<? extends ViewBinding> adapter;
  protected V2NIMTeamType teamTypeEnum;

  private View rootView;
  protected View ivBack;
  protected View ivClear;
  protected View tvSure;
  protected View groupEmpty;
  protected RecyclerView rvMemberList;
  protected EditText etSearch;
  protected int maxCount = IMKitConfigCenter.getTeamManagerMaxCount();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);
    changeStatusBarColor(R.color.color_white);
    V2NIMTeam teamInfo = (V2NIMTeam) getIntent().getSerializableExtra(KEY_TEAM_INFO);
    maxCount =
        getIntent()
            .getIntExtra(
                RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT,
                IMKitConfigCenter.getTeamManagerMaxCount());
    if (teamInfo == null) {
      finish();
      return;
    }
    List<String> accoutList =
        (List<String>) getIntent().getSerializableExtra(RouterConstant.SELECTOR_CONTACT_FILTER_KEY);
    if (accoutList != null) {
      filterAccounts.addAll(accoutList);
    }

    teamId = teamInfo.getTeamId();
    teamTypeEnum = teamInfo.getTeamType();
    initUI();
    configViewModel();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (NetworkUtils.isConnected()) {
      if (ivClear.getVisibility() == View.GONE) {
        //        viewModel.requestAllTeamMembers(teamId);
      }
    } else {
      dismissLoading();
      Toast.makeText(
              getApplicationContext(), getString(R.string.team_network_error), Toast.LENGTH_SHORT)
          .show();
    }
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  protected void teamMemberUpdate() {}

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(tvSure);
    Objects.requireNonNull(ivBack);
    Objects.requireNonNull(groupEmpty);
    Objects.requireNonNull(ivClear);
    Objects.requireNonNull(rvMemberList);
    Objects.requireNonNull(etSearch);
  }

  protected BaseTeamMemberListAdapter<? extends ViewBinding> getMemberListAdapter(
      V2NIMTeamType typeEnum) {
    return null;
  }

  private void initUI() {
    ivBack.setOnClickListener(v -> finish());
    rvMemberList.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    adapter = getMemberListAdapter(teamTypeEnum);
    adapter.setGroupIdentify(false);
    rvMemberList.setAdapter(adapter);
    ivClear.setOnClickListener(v -> etSearch.setText(null));
    tvSure.setOnClickListener(
        v -> {
          if (adapter != null) {
            ArrayList<TeamMemberWithUserInfo> selectedList = adapter.getSelectData();
            if (selectedList == null || selectedList.isEmpty()) {
              Toast.makeText(
                      getApplicationContext(),
                      getString(R.string.team_add_manager_empty_tip),
                      Toast.LENGTH_SHORT)
                  .show();
              return;
            }
            if (maxCount >= 0 && selectedList.size() > maxCount) {
              String tips = getString(R.string.team_add_manager_limit_tip);
              Toast.makeText(
                      getApplicationContext(),
                      String.format(
                          tips, String.valueOf(IMKitConfigCenter.getTeamManagerMaxCount())),
                      Toast.LENGTH_SHORT)
                  .show();
              return;
            }
            doActionAndFilterNetworkBroken(
                this,
                () -> {
                  Intent intent = new Intent();
                  intent.putExtra(
                      RouterConstant.REQUEST_CONTACT_SELECTOR_KEY,
                      TeamUtils.getAccIdListFromInfoList(selectedList));
                  setResult(Activity.RESULT_OK, intent);
                  finish();
                });
          }
        });
    etSearch.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (adapter != null) {
              adapter.filter(s);
              int count = adapter.getItemCount();
              if (count <= 0) {
                groupEmpty.setVisibility(View.VISIBLE);
              } else {
                groupEmpty.setVisibility(View.GONE);
              }
            }

            if (TextUtils.isEmpty(String.valueOf(s))) {
              ivClear.setVisibility(View.GONE);
            } else {
              ivClear.setVisibility(View.VISIBLE);
            }
          }
        });
  }

  private void configViewModel() {
    viewModel = new ViewModelProvider(this).get(TeamSettingViewModel.class);
    viewModel.configTeamId(teamId);
    // 获取群成员列表
    viewModel
        .getTeamMemberListWithUserData()
        .observe(
            this,
            resultInfo -> {
              dismissLoading();
              if (resultInfo.isSuccess()) {
                if (resultInfo.getType() == FetchResult.FetchType.Init) {
                  loadTeamMembers(resultInfo.getData());
                } else if (resultInfo.getType() == FetchResult.FetchType.Update) {
                  adapter.updateData(resultInfo.getData());
                } else if (resultInfo.getType() == FetchResult.FetchType.Add) {
                  filterAIUser(resultInfo.getData());
                  adapter.addData(resultInfo.getData(), null);
                }
              }
            });
    viewModel
        .getRemoveMembersData()
        .observe(
            this,
            resultInfo -> {
              if (resultInfo.isSuccess() && resultInfo.getData() != null) {
                adapter.removeData(resultInfo.getData());
                teamMemberUpdate();
              }
            });
    viewModel.loadTeamMember();
  }

  protected void loadTeamMembers(List<TeamMemberWithUserInfo> teamMemberList) {
    List<TeamMemberWithUserInfo> memberList =
        TeamUtils.filterMemberListFromInfoList(teamMemberList, filterAccounts);
    filterAIUser(memberList);
    if (!memberList.isEmpty() && memberList.size() > 1) {
      Collections.sort(memberList, TeamUtils.teamManagerComparator());
    }
    adapter.setDataAndSaveSelect(memberList);
    if (adapter.getItemCount() > 0) {
      groupEmpty.setVisibility(View.GONE);
    } else {
      groupEmpty.setVisibility(View.VISIBLE);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  /**
   * 启动群成员选择界面
   *
   * @param context 上下文
   * @param activity 群成员选择界面
   * @param team 群信息
   */
  public static void launch(Context context, Class<? extends Activity> activity, Team team) {
    Intent intent = new Intent(context, activity);
    intent.putExtra(KEY_TEAM_INFO, team);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }

  protected void filterAIUser(List<TeamMemberWithUserInfo> memberList) {
    if (memberList == null || memberList.size() < 1 || !IMKitConfigCenter.getEnableAIUser()) {
      return;
    }
    for (int index = memberList.size() - 1; index >= 0; index--) {
      TeamMemberWithUserInfo userInfo = memberList.get(index);
      if (AIUserManager.isAIUser(userInfo.getAccountId())) {
        memberList.remove(index);
      }
    }
  }
}
