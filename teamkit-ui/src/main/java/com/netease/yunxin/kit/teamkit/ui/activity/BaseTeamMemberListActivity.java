// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

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
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.event.BaseEvent;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.model.EventDef;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import com.netease.yunxin.kit.teamkit.ui.utils.viewmodel.TeamSettingViewModel;
import java.util.Objects;

/** team member list activity */
public abstract class BaseTeamMemberListActivity extends BaseActivity {
  public static final String KEY_TEAM_INFO = "team_info";
  protected final TeamSettingViewModel model = new TeamSettingViewModel();
  protected String teamId;
  protected boolean teamGroup = false;
  protected BaseTeamMemberListAdapter<? extends ViewBinding> adapter;
  protected TeamTypeEnum teamTypeEnum;

  private View rootView;
  protected View ivBack;
  protected View ivClear;
  protected View groupEmtpy;
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
    initUI();
    configViewModel();
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(ivBack);
    Objects.requireNonNull(groupEmtpy);
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
    adapter.setGroupIdentify(teamGroup);
    rvMemberList.setAdapter(adapter);
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
                groupEmtpy.setVisibility(View.VISIBLE);
              } else {
                groupEmtpy.setVisibility(View.GONE);
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
    showLoading();
    if (NetworkUtils.isConnected()) {
      model.requestTeamMembers(teamId);
    } else {
      dismissLoading();
      Toast.makeText(
              getApplicationContext(), getString(R.string.team_network_error), Toast.LENGTH_SHORT)
          .show();
    }
    model
        .getUserInfoData()
        .observe(
            this,
            listResultInfo -> {
              dismissLoading();
              if (listResultInfo.getSuccess()) {
                adapter.addDataList(listResultInfo.getValue(), true);
                if (adapter.getItemCount() > 0) {
                  groupEmtpy.setVisibility(View.GONE);
                } else {
                  groupEmtpy.setVisibility(View.VISIBLE);
                }
              }
            });
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
