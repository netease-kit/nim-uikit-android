// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_ID;
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
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;
import java.util.Objects;

/** 群昵称修改界面基类 子类需要实现{@link #initViewAndGetRootView(Bundle)}方法，返回界面的根布局 */
public abstract class BaseTeamUpdateNicknameActivity extends BaseLocalActivity {
  public static final String KEY_TEAM_MY_NICKNAME = "my_team_nickname";
  protected static final String MAX_COUNT_STR = "/30";
  protected final TeamSettingViewModel model = new TeamSettingViewModel();

  private View rootView;
  protected View ivClear;
  protected View cancelView;
  protected TextView tvFlag;
  protected TextView tvSave;
  protected EditText etNickname;

  protected boolean canUpdate = false;
  protected String lastTeamNickname;
  protected String teamId;

  protected String teamNickname;

  @SuppressLint("SetTextI18n")
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);

    changeStatusBarColor(R.color.color_eff1f4);
    teamId = getIntent().getStringExtra(KEY_TEAM_ID);
    lastTeamNickname = getIntent().getStringExtra(KEY_TEAM_MY_NICKNAME);
    teamNickname = lastTeamNickname;
    cancelView.setOnClickListener(v -> finish());
    if (!TextUtils.isEmpty(lastTeamNickname)) {
      etNickname.setText(lastTeamNickname);
      ivClear.setVisibility(View.VISIBLE);
      tvFlag.setText(lastTeamNickname.length() + MAX_COUNT_STR);
    }
    etNickname.requestFocus();
    ivClear.setOnClickListener(v -> etNickname.setText(""));
    etNickname.addTextChangedListener(
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
        v -> model.updateNickname(teamId, String.valueOf(etNickname.getText()).trim()));
    model
        .getNicknameData()
        .observe(
            this,
            stringResultInfo -> {
              if (!stringResultInfo.isSuccess()) {
                handleNetworkBrokenResult(this, stringResultInfo);
                return;
              }
              if (!TextUtils.equals(lastTeamNickname, stringResultInfo.getData())) {
                canUpdate = true;
              }
              teamNickname = String.valueOf(etNickname.getText());
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
    Objects.requireNonNull(etNickname);
  }

  @Override
  public void finish() {
    if (canUpdate) {
      Intent intent = new Intent();
      intent.putExtra(KEY_TEAM_MY_NICKNAME, teamNickname);
      setResult(RESULT_OK, intent);
    }
    super.finish();
  }

  /**
   * 启动群昵称修改界面
   *
   * @param context 上下文
   * @param activity 目标Activity
   * @param teamId 群ID
   * @param teamNickname 群昵称
   * @param launcher 启动器
   */
  public static void launch(
      Context context,
      Class<? extends Activity> activity,
      String teamId,
      String teamNickname,
      ActivityResultLauncher<Intent> launcher) {
    Intent intent = new Intent(context, activity);
    intent.putExtra(KEY_TEAM_MY_NICKNAME, teamNickname);
    intent.putExtra(KEY_TEAM_ID, teamId);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    launcher.launch(intent);
  }
}
