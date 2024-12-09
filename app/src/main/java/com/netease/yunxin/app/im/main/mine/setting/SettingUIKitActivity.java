// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityKitConfigBinding;
import com.netease.yunxin.app.im.main.SettingKitConfig;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;

public class SettingUIKitActivity extends BaseLocalActivity {
  private ActivityKitConfigBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityKitConfigBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
  }

  private void initView() {
    SettingKitConfig kitConfig = DataUtils.getSettingKitConfig();
    viewBinding.kitOnlineSc.setChecked(kitConfig.hasOnlineStatus);
    viewBinding.kitTeamSc.setChecked(kitConfig.hasTeam);
    viewBinding.kitStickTopSc.setChecked(kitConfig.hasStickTopMsg);
    viewBinding.kitCollectSc.setChecked(kitConfig.hasCollection);
    viewBinding.kitPinSc.setChecked(kitConfig.hasPin);
    viewBinding.kitCallSc.setChecked(kitConfig.hasStrangeCallLimit);
    viewBinding.settingTitleBar.setOnBackIconClickListener(v -> onBackPressed());
    viewBinding.kitCallSc.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            DataUtils.getSettingKitConfig().hasStrangeCallLimit = viewBinding.kitCallSc.isChecked();
          }
        });

    viewBinding.kitCollectSc.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            DataUtils.getSettingKitConfig().hasCollection = viewBinding.kitCollectSc.isChecked();
          }
        });

    viewBinding.kitStickTopSc.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            DataUtils.getSettingKitConfig().hasStickTopMsg = viewBinding.kitStickTopSc.isChecked();
          }
        });

    viewBinding.kitPinSc.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            DataUtils.getSettingKitConfig().hasPin = viewBinding.kitPinSc.isChecked();
          }
        });

    viewBinding.kitTeamSc.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            DataUtils.getSettingKitConfig().hasTeam = viewBinding.kitTeamSc.isChecked();
          }
        });

    viewBinding.kitOnlineSc.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            DataUtils.getSettingKitConfig().hasOnlineStatus = viewBinding.kitOnlineSc.isChecked();
          }
        });
  }
}
