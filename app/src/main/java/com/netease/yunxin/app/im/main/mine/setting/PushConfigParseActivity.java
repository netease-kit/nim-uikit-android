// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityPushConfigParseBinding;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.utils.ToastX;

public class PushConfigParseActivity extends BaseLocalActivity {

  private ActivityPushConfigParseBinding viewBinding;
  private PushConfigViewModel viewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityPushConfigParseBinding.inflate(getLayoutInflater());
    viewModel = new ViewModelProvider(this).get(PushConfigViewModel.class);
    setContentView(viewBinding.getRoot());
    initView();
    loadData();
  }

  private void initView() {
    viewBinding.settingTitleBar.setOnBackIconClickListener(v -> onBackPressed());

    viewBinding.pushConfigSwitchLayout.setOnClickListener(
        v -> viewBinding.pushConfigSC.setChecked(!viewBinding.pushConfigSC.isChecked()));
    viewBinding.tvSaveConfig.setOnClickListener(
        v -> {
          boolean configSwitch = viewBinding.pushConfigSC.isChecked();
          String serverConfig = viewBinding.configEt.getText().toString();
          boolean result = viewModel.savePushConfig(serverConfig, configSwitch);
          if (result) {
            ToastX.showShortToast(R.string.uikit_save_success);
            finish();
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
    String jsonData = viewModel.getPushConfigString();
    boolean configSwitch = viewModel.getPushConfigSwitch();
    viewBinding.pushConfigSC.setChecked(configSwitch);
    if (jsonData != null) {
      viewBinding.configEt.setText(jsonData);
    }
  }
}
