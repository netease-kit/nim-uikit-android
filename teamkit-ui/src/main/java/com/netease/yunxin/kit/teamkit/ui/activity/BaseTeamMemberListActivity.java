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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.chatkit.model.TeamWithCurrentMember;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.event.BaseEvent;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.model.EventDef;
import com.netease.yunxin.kit.teamkit.ui.normal.adapter.TeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** team member list activity */
public abstract class BaseTeamMemberListActivity extends BaseActivity {

  protected final TeamSettingViewModel model = new TeamSettingViewModel();
  protected String teamId;
  protected boolean teamGroup = false;
  protected BaseTeamMemberListAdapter<? extends ViewBinding> adapter;
  protected TeamTypeEnum teamTypeEnum;
  protected TeamWithCurrentMember teamWithCurrentMember;

  private View rootView;
  protected View ivBack;
  protected View ivClear;
  protected View groupEmpty;
  protected RecyclerView rvMemberList;
  protected EditText etSearch;

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

    Team teamInfo = (Team) getIntent().getSerializableExtra(KEY_TEAM_INFO);
    teamId = teamInfo.getId();
    teamTypeEnum = teamInfo.getType();
    teamGroup = TeamUtils.isTeamGroup(teamInfo);
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
        model.requestTeamMembers(teamId);
      }
      model.requestTeamData(teamId);
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
      TeamTypeEnum typeEnum) {
    return null;
  }

  private void initUI() {
    ivBack.setOnClickListener(v -> finish());
    rvMemberList.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
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
                  accounts.add(data.getUserInfo().getAccount());
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
              model.requestTeamMembers(teamId);
            } else {
              ivClear.setVisibility(View.VISIBLE);
            }
          }
        });
  }

  private void configViewModel() {
    model
        .getTeamWithMemberData()
        .observe(
            this,
            listResultInfo -> {
              if (listResultInfo.getSuccess()) {
                if (listResultInfo.getValue() == null
                    || listResultInfo.getValue().getTeamMember() == null) {
                  return;
                }
                teamWithCurrentMember = listResultInfo.getValue();
                TeamMemberType removeTag = null;
                if (teamWithCurrentMember.getTeamMember().getType() == TeamMemberType.Owner) {
                  removeTag = TeamMemberType.Manager;
                } else if (teamWithCurrentMember.getTeamMember().getType()
                    == TeamMemberType.Manager) {
                  removeTag = TeamMemberType.Normal;
                }
                adapter.setShowRemoveTagWithMemberType(removeTag);
              }
            });
    model
        .getUserInfoData()
        .observe(
            this,
            listResultInfo -> {
              dismissLoading();
              if (listResultInfo.getSuccess()) {
                if (listResultInfo.getValue() != null && !listResultInfo.getValue().isEmpty()) {
                  Collections.sort(listResultInfo.getValue(), TeamUtils.teamManagerComparator());
                }
                adapter.addDataList(listResultInfo.getValue(), true);
                if (adapter.getItemCount() > 0) {
                  groupEmpty.setVisibility(View.GONE);
                } else {
                  groupEmpty.setVisibility(View.VISIBLE);
                }
              }
            });
    model
        .getAddRemoveMembersData()
        .observe(
            this,
            listResultInfo -> {
              model.requestTeamMembers(teamId);
            });
  }

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
                model.removeMember(teamId, accounts);
              }

              @Override
              public void onNegative() {}
            })
        .show(this.getSupportFragmentManager());
  }

  public static void launch(Context context, Class<? extends Activity> activity, Team team) {
    Intent intent = new Intent(context, activity);
    intent.putExtra(KEY_TEAM_INFO, team);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }
}
