// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityServerConfigParseBinding;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.dialog.CommonConfirmDialog;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;

public class ServerConfigParseActivity extends BaseLocalActivity {

  private ActivityServerConfigParseBinding viewBinding;
  private ServerConfigViewModel viewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityServerConfigParseBinding.inflate(getLayoutInflater());
    viewModel = new ViewModelProvider(this).get(ServerConfigViewModel.class);
    setContentView(viewBinding.getRoot());
    initView();
    loadData();
  }

  private void initView() {
    viewBinding.settingTitleBar.setOnBackIconClickListener(v -> onBackPressed());

    viewBinding.serverConfigSwitchLayout.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            viewBinding.serverConfigSC.setChecked(!viewBinding.serverConfigSC.isChecked());
          }
        });
    viewBinding.tvSaveConfig.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            boolean configSwitch = viewBinding.serverConfigSC.isChecked();
            String serverConfig = viewBinding.configEt.getText().toString();
            viewModel.saveServerConfig(serverConfig, configSwitch);
            CommonConfirmDialog.Companion.show(
                ServerConfigParseActivity.this,
                getString(R.string.server_config_dialog_title),
                getString(R.string.server_config_dialog_content),
                getString(R.string.server_config_dialog_cancel),
                getString(R.string.server_config_dialog_positive),
                true,
                true,
                positive -> {
                  if (positive) {
                    IMKitClient.logout(
                        new FetchCallback<Void>() {
                          @Override
                          public void onSuccess(@Nullable Void unused) {}

                          @Override
                          public void onError(int i, @NonNull String s) {}
                        });
                    Intent intent =
                        getBaseContext()
                            .getPackageManager()
                            .getLaunchIntentForPackage(getBaseContext().getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    android.os.Process.killProcess(android.os.Process.myPid());
                  }
                });
          }
        });

    viewBinding.configClearIv.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            viewBinding.configEt.setText("");
          }
        });
  }

  private void loadData() {
    String jsonData = viewModel.getServerConfigString();
    boolean configSwitch = viewModel.getServiceConfigSwitch();
    viewBinding.serverConfigSC.setChecked(configSwitch);
    if (jsonData != null) {
      viewBinding.configEt.setText(jsonData);
    }
  }
}
