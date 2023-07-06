// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.os.Bundle;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.im.AppSkinConfig;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityMineSettingNotifyBinding;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.SizeUtils;

public class SettingNotifyActivity extends BaseActivity {

  private ActivityMineSettingNotifyBinding viewBinding;
  private SettingNotifyViewModel viewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    changeStatusBarColor(R.color.color_e9eff5);
    super.onCreate(savedInstanceState);
    viewBinding = ActivityMineSettingNotifyBinding.inflate(getLayoutInflater());
    viewModel = new ViewModelProvider(this).get(SettingNotifyViewModel.class);
    setContentView(viewBinding.getRoot());

    viewModel
        .getNotifyDetailLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Error && result.getData() != null) {
                viewBinding.notifyShowInfoSc.setChecked(!result.getData());
              }
            });

    viewModel
        .getToggleNotificationLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Error && result.getData() != null) {
                viewBinding.notifySc.setChecked(!result.getData());
              }
            });
  }

  @Override
  protected void onResume() {
    super.onResume();
    initView();
  }

  private void initView() {
    viewBinding.notifySc.setChecked(viewModel.getToggleNotification());
    viewBinding.notifySc.setOnClickListener(
        v -> viewModel.setToggleNotification(viewBinding.notifySc.isChecked()));
    viewBinding.notifyRingSc.setChecked(viewModel.getRingToggle());
    viewBinding.notifyRingSc.setOnClickListener(
        v -> viewModel.setRingToggle(viewBinding.notifyRingSc.isChecked()));
    viewBinding.notifyShakeSc.setChecked(viewModel.getVibrateToggle());
    viewBinding.notifyShakeSc.setOnClickListener(
        v -> viewModel.setVibrateToggle(viewBinding.notifyShakeSc.isChecked()));
    viewBinding.notifyShowInfoSc.setChecked(!viewModel.getPushShowNoDetail());
    viewBinding.notifyShowInfoSc.setOnClickListener(
        v -> viewModel.setPushShowNoDetail(!viewBinding.notifyShowInfoSc.isChecked()));
    viewBinding.settingTitleBar.setOnBackIconClickListener(v -> onBackPressed());
    if (AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin) {
      updateCommonView();
    }
  }

  private void updateCommonView() {
    changeStatusBarColor(R.color.color_ededed);
    viewBinding.clRoot.setBackgroundResource(R.color.color_ededed);

    viewBinding.notifyLl.setBackgroundResource(R.color.color_white);
    ViewGroup.MarginLayoutParams layoutParamsN =
        (ViewGroup.MarginLayoutParams) viewBinding.notifyLl.getLayoutParams();
    layoutParamsN.setMargins(0, SizeUtils.dp2px(4), 0, 0);
    viewBinding.notifyLl.setLayoutParams(layoutParamsN);

    viewBinding.notifyModeLl.setBackgroundResource(R.color.color_white);
    ViewGroup.MarginLayoutParams layoutParamsM =
        (ViewGroup.MarginLayoutParams) viewBinding.notifyModeLl.getLayoutParams();
    layoutParamsM.setMargins(0, SizeUtils.dp2px(4), 0, 0);
    viewBinding.notifyModeLl.setLayoutParams(layoutParamsM);

    viewBinding.pushModeLl.setBackgroundResource(R.color.color_white);
    ViewGroup.MarginLayoutParams layoutParamsP =
        (ViewGroup.MarginLayoutParams) viewBinding.pushModeLl.getLayoutParams();
    layoutParamsP.setMargins(0, SizeUtils.dp2px(4), 0, 0);
    viewBinding.pushModeLl.setLayoutParams(layoutParamsP);

    viewBinding.notifySc.setThumbResource(R.drawable.fun_setting_bg_switch_thumb_selector);
    viewBinding.notifySc.setTrackResource(R.drawable.fun_setting_bg_switch_track_selector);

    viewBinding.notifyRingSc.setThumbResource(R.drawable.fun_setting_bg_switch_thumb_selector);
    viewBinding.notifyRingSc.setTrackResource(R.drawable.fun_setting_bg_switch_track_selector);

    viewBinding.notifyShakeSc.setThumbResource(R.drawable.fun_setting_bg_switch_thumb_selector);
    viewBinding.notifyShakeSc.setTrackResource(R.drawable.fun_setting_bg_switch_track_selector);

    viewBinding.notifyShowInfoSc.setThumbResource(R.drawable.fun_setting_bg_switch_thumb_selector);
    viewBinding.notifyShowInfoSc.setTrackResource(R.drawable.fun_setting_bg_switch_track_selector);
  }
}
