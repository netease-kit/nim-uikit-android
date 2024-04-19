// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_ID;
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
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;
import java.util.Objects;

/** 群介绍修改界面基类 子类需要实现{@link #initViewAndGetRootView(Bundle)}方法，返回界面的根布局 */
public abstract class BaseTeamUpdateIntroduceActivity extends BaseActivity {
  public static final String KEY_TEAM_INTRODUCE = "team/teamIntroduce";
  protected static final String MAX_COUNT_STR = "/100";
  protected final TeamSettingViewModel model = new TeamSettingViewModel();
  protected boolean canUpdate = false;
  protected String lastTeamIntroduce;
  protected String teamId;

  protected String teamIntroduce;
  protected boolean hasPrivilege;

  private View rootView;
  protected View cancelView;
  protected View ivClear;
  protected TextView tvFlag;
  protected TextView tvSave;
  protected EditText etIntroduce;

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
    lastTeamIntroduce = getIntent().getStringExtra(KEY_TEAM_INTRODUCE);
    teamIntroduce = lastTeamIntroduce;
    cancelView.setOnClickListener(v -> finish());
    if (!TextUtils.isEmpty(lastTeamIntroduce)) {
      etIntroduce.setText(lastTeamIntroduce);
      ivClear.setVisibility(View.VISIBLE);
      tvFlag.setText(lastTeamIntroduce.length() + MAX_COUNT_STR);
    }
    if (!hasPrivilege) {
      tvSave.setVisibility(View.GONE);
      etIntroduce.setEnabled(false);
      ivClear.setVisibility(View.GONE);
    }
    etIntroduce.requestFocus();
    ivClear.setOnClickListener(v -> etIntroduce.setText(""));
    etIntroduce.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(String.valueOf(s))) {
              ivClear.setVisibility(View.GONE);
            } else {
              ivClear.setVisibility(View.VISIBLE);
            }
            tvFlag.setText(String.valueOf(s).length() + MAX_COUNT_STR);
          }
        });
    tvSave.setOnClickListener(
        v -> model.updateIntroduce(teamId, String.valueOf(etIntroduce.getText())));
    model
        .getIntroduceData()
        .observe(
            this,
            stringResultInfo -> {
              if (!stringResultInfo.isSuccess()) {
                handleNetworkBrokenResult(this, stringResultInfo);
                return;
              }
              if (!TextUtils.equals(lastTeamIntroduce, String.valueOf(etIntroduce.getText()))) {
                canUpdate = true;
              }
              teamIntroduce = String.valueOf(etIntroduce.getText());
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
    Objects.requireNonNull(etIntroduce);
  }

  @Override
  public void finish() {
    if (hasPrivilege && canUpdate) {
      Intent intent = new Intent();
      intent.putExtra(KEY_TEAM_INTRODUCE, teamIntroduce);
      setResult(RESULT_OK, intent);
    }
    super.finish();
  }

  /**
   * 启动群介绍修改界面
   *
   * @param context 上下文
   * @param activity 目标Activity
   * @param hasPrivilege 是否有权限
   * @param teamId 群ID
   * @param introduce 群介绍
   * @param launcher 启动器
   */
  public static void launch(
      Context context,
      Class<? extends Activity> activity,
      boolean hasPrivilege,
      String teamId,
      String introduce,
      ActivityResultLauncher<Intent> launcher) {
    Intent intent = new Intent(context, activity);
    intent.putExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, hasPrivilege);
    intent.putExtra(KEY_TEAM_INTRODUCE, introduce);
    intent.putExtra(KEY_TEAM_ID, teamId);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    launcher.launch(intent);
  }
}
