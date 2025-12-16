// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_TEAM_NAME;
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
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.coexist.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;
import java.util.Objects;

/** 群名称修改界面基类 子类需要实现{@link #initViewAndGetRootView(Bundle)}方法，返回界面的根布局 */
public abstract class BaseTeamUpdateNameActivity extends BaseLocalActivity {
  protected static final String MAX_COUNT_STR = "/30";
  protected TeamSettingViewModel viewModel;
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
    viewModel = new ViewModelProvider(this).get(TeamSettingViewModel.class);
    changeStatusBarColor(R.color.color_eff1f4);
    hasPrivilege = getIntent().getBooleanExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, false);
    teamId = getIntent().getStringExtra(KEY_TEAM_ID);
    lastTeamName = getIntent().getStringExtra(KEY_TEAM_NAME);
    isGroup = getIntent().getBooleanExtra(KEY_TEAM_IS_GROUP, false);
    V2NIMTeamType typeEnum = (V2NIMTeamType) getIntent().getSerializableExtra(KEY_TEAM_TYPE);

    if (typeEnum == V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL && !isGroup) {
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
    tvSave.setOnClickListener(v -> viewModel.updateName(teamId, String.valueOf(etName.getText())));
    viewModel
        .getNameData()
        .observe(
            this,
            voidResultInfo -> {
              if (!voidResultInfo.isSuccess()) {
                handleNetworkBrokenResult(this, voidResultInfo);
                return;
              }
              if (!TextUtils.equals(lastTeamName, voidResultInfo.getData())) {
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

  /**
   * 启动群名称修改界面
   *
   * @param context 上下文
   * @param activity 目标Activity
   * @param hasPrivilege 是否有权限
   * @param typeEnum 群类型
   * @param teamId 群ID
   * @param name 群名称
   * @param isGroup 是否是群
   * @param launcher 启动器
   */
  public static void launch(
      Context context,
      Class<? extends Activity> activity,
      boolean hasPrivilege,
      V2NIMTeamType typeEnum,
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
