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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.TeamWithCurrentMember;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.event.BaseEvent;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.model.EventDef;
import com.netease.yunxin.kit.teamkit.ui.normal.adapter.TeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamBaseViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 群成员列表基类 子类需要实现{@link #initViewAndGetRootView(Bundle)}方法，返回布局的根View */
public abstract class BaseTeamMemberListActivity extends BaseActivity {

  protected TeamBaseViewModel viewModel;
  protected String teamId;
  protected boolean teamGroup = false;
  protected BaseTeamMemberListAdapter<? extends ViewBinding> adapter;
  protected V2NIMTeamType teamTypeEnum;
  protected TeamWithCurrentMember teamWithCurrentMember;

  private View rootView;
  protected View ivBack;
  protected View ivClear;
  protected View groupEmpty;
  protected RecyclerView rvMemberList;
  protected LinearLayoutManager layoutManager;
  protected EditText etSearch;

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
          return EventDef.EVENT_TYPE_CLOSE_CHAT_PAGE;
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

    V2NIMTeam teamInfo = (V2NIMTeam) getIntent().getSerializableExtra(KEY_TEAM_INFO);
    if (teamInfo != null) {
      teamId = teamInfo.getTeamId();
      teamTypeEnum = teamInfo.getTeamType();
      teamGroup = TeamUtils.isTeamGroup(teamInfo);
    }
    if (TextUtils.isEmpty(teamId)) {
      finish();
      return;
    }
    initUI();
    configViewModel();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (NetworkUtils.isConnected()) {
      if (ivClear.getVisibility() == View.GONE) {
        viewModel.requestAllTeamMembers(teamId);
      }
      viewModel.requestTeamData(teamId);
    } else {
      dismissLoading();
      Toast.makeText(
              getApplicationContext(), getString(R.string.team_network_error), Toast.LENGTH_SHORT)
          .show();
    }
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(ivBack);
    Objects.requireNonNull(groupEmpty);
    Objects.requireNonNull(ivClear);
    Objects.requireNonNull(rvMemberList);
    Objects.requireNonNull(etSearch);
  }

  protected BaseTeamMemberListAdapter<? extends ViewBinding> getMemberListAdapter(
      V2NIMTeamType teamType) {
    return null;
  }

  private void initUI() {
    ivBack.setOnClickListener(v -> finish());
    layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    rvMemberList.setLayoutManager(layoutManager);
    adapter = getMemberListAdapter(teamTypeEnum);
    // 讨论组不展示身份标签
    adapter.setGroupIdentify(!teamGroup);
    rvMemberList.setAdapter(adapter);
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
    ivClear.setOnClickListener(v -> etSearch.setText(null));
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

    // 监听滚动，当滚动到底部触发加载更多
    rvMemberList.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            ALog.i("BaseTeamMemberListActivity", "onScrollStateChanged newState = " + newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              int position = layoutManager.findLastVisibleItemPosition();
              if (viewModel != null
                  && viewModel.hasMore()
                  && adapter.getItemCount() < position + 5) {
                viewModel.requestMoreTeamMember(teamId);
              }
            }
          }

          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
          }
        });
  }

  private void configViewModel() {
    viewModel = new ViewModelProvider(this).get(TeamBaseViewModel.class);
    viewModel.configTeamId(teamId);
    // 获取群信息和当前用户信息
    viewModel
        .getTeamWitheMemberData()
        .observe(
            this,
            listResultInfo -> {
              if (listResultInfo.isSuccess()) {
                if (listResultInfo.getData() == null
                    || listResultInfo.getData().getTeamMember() == null) {
                  return;
                }
                teamWithCurrentMember = listResultInfo.getData();
                V2NIMTeamMemberRole removeTag = null;
                if (!teamGroup) {
                  if (teamWithCurrentMember.getTeamMember().getMemberRole()
                      == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_OWNER) {
                    removeTag = V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER;
                  } else if (teamWithCurrentMember.getTeamMember().getMemberRole()
                      == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER) {
                    removeTag = V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_NORMAL;
                  }
                }
                adapter.setShowRemoveTagWithMemberType(removeTag);
              }
            });
    // 获取群成员列表观察着
    viewModel
        .getTeamMemberListWithUserData()
        .observe(
            this,
            listResultInfo -> {
              dismissLoading();
              if (listResultInfo.isSuccess()) {
                if (listResultInfo.getData() != null && !listResultInfo.getData().isEmpty()) {
                  Collections.sort(listResultInfo.getData(), TeamUtils.teamManagerComparator());
                }
                boolean clearData = listResultInfo.getType() != FetchResult.FetchType.Add;
                adapter.addDataList(listResultInfo.getData(), clearData);
                if (adapter.getItemCount() > 0) {
                  groupEmpty.setVisibility(View.GONE);
                } else {
                  groupEmpty.setVisibility(View.VISIBLE);
                }
              }
            });
    // 添加或删除群成员观察者
    viewModel
        .getAddRemoveMembersData()
        .observe(
            this,
            listResultInfo -> {
              viewModel.requestAllTeamMembers(teamId);
            });
  }

  // 显示删除确认对话框
  private void showDeleteConfirmDialog(List<String> accounts) {
    CommonChoiceDialog dialog = new CommonChoiceDialog();
    dialog
        .setTitleStr(getString(R.string.team_remove_member_title))
        .setContentStr(getString(R.string.team_remove_member_content))
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
                viewModel.removeMember(teamId, accounts);
              }

              @Override
              public void onNegative() {}
            })
        .show(this.getSupportFragmentManager());
  }

  public static void launch(Context context, Class<? extends Activity> activity, V2NIMTeam team) {
    Intent intent = new Intent(context, activity);
    intent.putExtra(KEY_TEAM_INFO, team);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }
}
