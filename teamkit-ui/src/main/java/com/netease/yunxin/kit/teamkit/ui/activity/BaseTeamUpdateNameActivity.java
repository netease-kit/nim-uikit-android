// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_NAME;
import static com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamInfoActivity.KEY_TEAM_IS_GROUP;
import static com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamInfoActivity.KEY_TEAM_TYPE;
import static com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamInfoActivity.KEY_TEAM_UPDATE_INFO_PRIVILEGE;
import static com.netease.yunxin.kit.teamkit.ui.utils.NetworkUtilsWrapper.handleNetworkBrokenResult;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;
import java.util.Objects;

/** set team name activity */
public abstract class BaseTeamUpdateNameActivity extends BaseActivity {
  protected static final String MAX_COUNT_STR = "/30";
  protected final TeamSettingViewModel model = new TeamSettingViewModel();
  protected boolean canUpdate = false;
  protected String lastTeamName;
  protected String teamId;

  private View rootView;
  protected View cancelView;
  protected View ivClear;
  protected TextView tvTitle;
  protected TextView tvFlag;
  protected TextView tvSave;
  protected EditText etName;

  protected String teamName;
  protected boolean isGroup;
  protected boolean hasPrivilege;

  @SuppressLint("SetTextI18n")
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);

    changeStatusBarColor(R.color.color_eff1f4);
    hasPrivilege = getIntent().getBooleanExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, false);
    teamId = getIntent().getStringExtra(KEY_TEAM_ID);
    lastTeamName = getIntent().getStringExtra(KEY_TEAM_NAME);
    isGroup = getIntent().getBooleanExtra(KEY_TEAM_IS_GROUP, false);
    TeamTypeEnum typeEnum = (TeamTypeEnum) getIntent().getSerializableExtra(KEY_TEAM_TYPE);

    if (typeEnum == TeamTypeEnum.Advanced && !isGroup) {
      tvTitle.setText(R.string.team_name_title);
    } else {
      tvTitle.setText(R.string.team_group_name_title);
    }
    teamName = lastTeamName;
    cancelView.setOnClickListener(v -> finish());
    if (!TextUtils.isEmpty(lastTeamName)) {
      etName.setText(lastTeamName);
      ivClear.setVisibility(View.VISIBLE);
      tvFlag.setText(lastTeamName.length() + MAX_COUNT_STR);
    }
    if (!hasPrivilege) {
      tvSave.setVisibility(View.GONE);
      etName.setEnabled(false);
      ivClear.setVisibility(View.GONE);
    }
    etName.requestFocus();
    ivClear.setOnClickListener(v -> etName.setText(""));
    etName.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(String.valueOf(s).trim())) {
              ivClear.setVisibility(View.GONE);
              tvSave.setAlpha(0.5f);
              tvSave.setEnabled(false);
            } else {
              ivClear.setVisibility(View.VISIBLE);
              tvSave.setAlpha(1f);
              tvSave.setEnabled(true);
            }
            tvFlag.setText(String.valueOf(s).length() + MAX_COUNT_STR);
          }
        });
    tvSave.setOnClickListener(v -> model.updateName(teamId, String.valueOf(etName.getText())));
    model
        .getNameData()
        .observe(
            this,
            voidResultInfo -> {
              if (!voidResultInfo.getSuccess()) {
                handleNetworkBrokenResult(this, voidResultInfo);
                return;
              }
              if (!TextUtils.equals(lastTeamName, voidResultInfo.getValue())) {
                canUpdate = true;
              }
              teamName = String.valueOf(etName.getText());
              finish();
            });
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(cancelView);
    Objects.requireNonNull(ivClear);
    Objects.requireNonNull(tvFlag);
    Objects.requireNonNull(tvSave);
    Objects.requireNonNull(etName);
    Objects.requireNonNull(tvTitle);
  }

  @Override
  public void finish() {
    if (hasPrivilege && canUpdate) {
      Intent intent = new Intent();
      intent.putExtra(KEY_TEAM_NAME, teamName);
      setResult(RESULT_OK, intent);
    }
    super.finish();
  }

  public static void launch(
      Context context,
      Class<? extends Activity> activity,
      boolean hasPrivilege,
      TeamTypeEnum typeEnum,
      String teamId,
      String name,
      boolean isGroup,
      ActivityResultLauncher<Intent> launcher) {
    Intent intent = new Intent(context, activity);
    intent.putExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, hasPrivilege);
    intent.putExtra(KEY_TEAM_TYPE, typeEnum);
    intent.putExtra(KEY_TEAM_NAME, name);
    intent.putExtra(KEY_TEAM_ID, teamId);
    intent.putExtra(KEY_TEAM_IS_GROUP, isGroup);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    launcher.launch(intent);
  }
}
