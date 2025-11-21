// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.netease.yunxin.app.im.AppSkinConfig;
import com.netease.yunxin.app.im.PictureEngine;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityKitConfigBinding;
import com.netease.yunxin.app.im.main.SettingKitConfig;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;

public class SettingUIKitActivity extends BaseLocalActivity {
  private ActivityKitConfigBinding viewBinding;
  private SettingViewModel viewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityKitConfigBinding.inflate(getLayoutInflater());
    viewModel = new ViewModelProvider(this).get(SettingViewModel.class);

    setContentView(viewBinding.getRoot());
    initView();
    if (AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin) {
      changeStatusBarColor(R.color.color_ededed);
      viewBinding.clRoot.setBackgroundResource(R.color.color_ededed);
      viewBinding.kitSettingll.setBackgroundResource(R.color.color_white);
      updateCommonView(
          R.drawable.fun_setting_bg_switch_thumb_selector,
          R.drawable.fun_setting_bg_switch_track_selector);
    }
  }

  private void initView() {
    SettingKitConfig kitConfig = DataUtils.getSettingKitConfig();
    viewBinding.kitOnlineSc.setChecked(kitConfig.hasOnlineStatus);
    viewBinding.kitTeamSc.setChecked(kitConfig.hasTeam);
    viewBinding.kitTeamModeSc.setChecked(kitConfig.hasTeamApplyMode);
    viewBinding.kitStickTopSc.setChecked(kitConfig.hasStickTopMsg);
    viewBinding.kitCollectSc.setChecked(kitConfig.hasCollection);
    viewBinding.kitPinSc.setChecked(kitConfig.hasPin);
    viewBinding.kitCallSc.setChecked(kitConfig.hasStrangeCallLimit);
    viewBinding.kitImagePickSc.setChecked(ChatKitClient.getPictureChooseEngine() != null);
    viewBinding.kitAntiSpamSc.setChecked(IMKitConfigCenter.getEnableAntiSpamTipMessage());
    viewBinding.settingTitleBar.setOnBackIconClickListener(v -> onBackPressed());
    viewBinding.aiStreamModeSc.setChecked(viewModel.getAIStream(this));
    viewBinding.aiStreamModeSc.setOnClickListener(
        v -> {
          boolean checked = viewBinding.aiStreamModeSc.isChecked();
          viewModel.setAIStream(this, checked);
        });
    viewBinding.chatRichTextModeSc.setChecked(IMKitConfigCenter.getEnableRichTextMessage());
    viewBinding.chatRichTextModeSc.setOnClickListener(
        v -> {
          boolean checked = viewBinding.chatRichTextModeSc.isChecked();
          IMKitConfigCenter.setEnableRichTextMessage(checked);
        });
    viewBinding.kitCallSc.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            DataUtils.getSettingKitConfig().hasStrangeCallLimit = viewBinding.kitCallSc.isChecked();
          }
        });

    viewBinding.kitTeamModeSc.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            DataUtils.getSettingKitConfig().hasTeamApplyMode =
                viewBinding.kitTeamModeSc.isChecked();
            DataUtils.saveTeamModeConfigSwitch(
                SettingUIKitActivity.this, viewBinding.kitTeamModeSc.isChecked());
            IMKitConfigCenter.setEnableTeamJoinAgreeModelAuth(
                viewBinding.kitTeamModeSc.isChecked());
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
    viewBinding.kitTeamManageCountEt.setText(
        String.valueOf(IMKitConfigCenter.getTeamManagerMaxCount()));
    viewBinding.kitTeamManageCountEt.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            String editContent = s.toString();
            try {
              int teamMember = Integer.parseInt(editContent);
              IMKitConfigCenter.setTeamManagerMaxCount(teamMember);
            } catch (Exception e) {
            }
          }
        });

    viewBinding.kitAntiSpamSc.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            IMKitConfigCenter.setEnableAntiSpamTipMessage(viewBinding.kitAntiSpamSc.isChecked());
          }
        });

    viewBinding.kitImagePickSc.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (viewBinding.kitImagePickSc.isChecked()) {
              ChatKitClient.setPictureChooseEngine(new PictureEngine());
            } else {
              ChatKitClient.setPictureChooseEngine(null);
            }
          }
        });
  }

  private void updateCommonView(@DrawableRes int thumbRes, @DrawableRes int trackRes) {

    viewBinding.kitCallSc.setThumbResource(thumbRes);
    viewBinding.kitCallSc.setTrackResource(trackRes);

    viewBinding.kitCollectSc.setThumbResource(thumbRes);
    viewBinding.kitCollectSc.setTrackResource(trackRes);

    viewBinding.kitOnlineSc.setThumbResource(thumbRes);
    viewBinding.kitOnlineSc.setTrackResource(trackRes);

    viewBinding.kitPinSc.setThumbResource(thumbRes);
    viewBinding.kitPinSc.setTrackResource(trackRes);

    viewBinding.kitStickTopSc.setThumbResource(thumbRes);
    viewBinding.kitStickTopSc.setTrackResource(trackRes);

    viewBinding.kitTeamSc.setThumbResource(thumbRes);
    viewBinding.kitTeamSc.setTrackResource(trackRes);

    viewBinding.kitTeamModeSc.setThumbResource(thumbRes);
    viewBinding.kitTeamModeSc.setTrackResource(trackRes);

    viewBinding.aiStreamModeSc.setThumbResource(thumbRes);
    viewBinding.aiStreamModeSc.setTrackResource(trackRes);

    viewBinding.chatRichTextModeSc.setThumbResource(thumbRes);
    viewBinding.chatRichTextModeSc.setTrackResource(trackRes);

    viewBinding.kitAntiSpamSc.setThumbResource(thumbRes);
    viewBinding.kitAntiSpamSc.setTrackResource(trackRes);
  }
}
