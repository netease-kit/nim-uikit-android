// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.qchat.R;
import com.netease.yunxin.app.qchat.databinding.ServerConfigActivityBinding;
import com.netease.yunxin.app.qchat.utils.Constant;
import com.netease.yunxin.app.qchat.utils.DataUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.dialog.CommonConfirmDialog;

public class ServerActivity extends BaseActivity {

  private static final String TAG = "ServerActivity";
  private ServerConfigActivityBinding viewBinding;
  private int DELAY_RESTART = 500;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ALog.d(TAG, "onCreate");
    changeStatusBarColor(R.color.color_eff1f4);
    viewBinding = ServerConfigActivityBinding.inflate(LayoutInflater.from(this));
    setContentView(viewBinding.getRoot());
    loadData();
    initView();
  }

  private void initView() {
    viewBinding.serverRadioGroup.setOnCheckedChangeListener(
        (group, checkedId) -> {
          CommonConfirmDialog.Companion.show(
              ServerActivity.this,
              getString(R.string.server_config_dialog_title),
              getString(R.string.server_config_dialog_content),
              getString(R.string.server_config_dialog_cancel),
              getString(R.string.server_config_dialog_positive),
              true,
              true,
              positive -> {
                if (positive) {
                  int config =
                      (checkedId == viewBinding.serverChinaConfig.getId()
                          ? Constant.CHINA_CONFIG
                          : Constant.OVERSEA_CONFIG);
                  changeServerConfig(config);
                } else {
                  viewBinding.serverRadioGroup.setOnCheckedChangeListener(null);
                  loadData();
                  initView();
                }
              });
        });
    viewBinding.typeSelectTitleBar.setOnBackIconClickListener(view -> this.finish());
  }

  private void loadData() {
    int chinaConfig =
        DataUtils.getConfigShared(this).getInt(Constant.SERVER_CONFIG, Constant.CHINA_CONFIG);
    if (chinaConfig == Constant.CHINA_CONFIG) {
      viewBinding.serverChinaConfig.setChecked(true);
    } else {
      viewBinding.serverOverseaConfig.setChecked(true);
    }
  }

  private void changeServerConfig(int config) {
    DataUtils.getConfigShared(this).edit().putInt(Constant.SERVER_CONFIG, config).apply();
    new Handler()
        .postDelayed(
            () -> {
              Intent intent =
                  getBaseContext()
                      .getPackageManager()
                      .getLaunchIntentForPackage(getBaseContext().getPackageName());
              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
              startActivity(intent);
              android.os.Process.killProcess(android.os.Process.myPid());
            },
            DELAY_RESTART);
  }
}
