// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.netease.yunxin.app.im.AppSkinConfig;
import com.netease.yunxin.app.im.IMApplication;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityMineSettingBinding;
import com.netease.yunxin.app.im.utils.AppUtils;
import com.netease.yunxin.app.im.utils.MultiLanguageUtils;
import com.netease.yunxin.app.im.welcome.WelcomeActivity;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatConfigManager;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;

public class SettingActivity extends BaseLocalActivity {

  private ActivityMineSettingBinding viewBinding;
  private SettingViewModel viewModel;

  //语言变更事件，切换语言后重新加载页面
  EventNotify<MultiLanguageUtils.LangEvent> langeNotify =
      new EventNotify<MultiLanguageUtils.LangEvent>() {
        @Override
        public void onNotify(@NonNull MultiLanguageUtils.LangEvent message) {
          ActivityCompat.recreate(SettingActivity.this);
        }

        @NonNull
        @Override
        public String getEventType() {
          return "langEvent";
        }
      };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityMineSettingBinding.inflate(getLayoutInflater());
    viewModel = new ViewModelProvider(this).get(SettingViewModel.class);
    setContentView(viewBinding.getRoot());
    initView();
    EventCenter.registerEventNotify(langeNotify);
  }

  private void initView() {
    // delete alias

    // show read and unread status
    viewBinding.messageReadSc.setChecked(viewModel.getShowReadStatus());
    viewBinding.messageReadSc.setOnClickListener(
        v -> {
          boolean checked = viewBinding.messageReadSc.isChecked();
          viewModel.setShowReadStatus(checked);
          ChatConfigManager.showReadStatus = checked;
        });

    // audio play mode AUDIO_PLAY_EARPIECE or AUDIO_PLAY_OUTSIDE
    viewBinding.playModeSc.setChecked(viewModel.getAudioPlayMode());
    viewBinding.playModeSc.setOnClickListener(
        v -> {
          boolean checked = viewBinding.playModeSc.isChecked();
          viewModel.setAudioPlayMode(checked);
        });

    // 本地会话
    viewBinding.conversationModeSc.setChecked(viewModel.getCloudConversation(this));
    viewBinding.conversationModeSc.setOnClickListener(
        v -> {
          boolean checked = viewBinding.conversationModeSc.isChecked();
          viewModel.setCloudConversation(this, checked);
          AppUtils.restartApp();
          finish();
        });

    viewBinding.notifyFl.setOnClickListener(
        v -> startActivity(new Intent(SettingActivity.this, SettingNotifyActivity.class)));

    viewBinding.skinFl.setOnClickListener(
        v -> startActivity(new Intent(SettingActivity.this, SkinActivity.class)));

    viewBinding.appLanguageLayout.setOnClickListener(
        view -> startActivity(new Intent(SettingActivity.this, SettingLanguageActivity.class)));

    viewBinding.clearFl.setOnClickListener(
        v -> startActivity(new Intent(SettingActivity.this, ClearCacheActivity.class)));

    viewBinding.serverConfigLayout.setOnClickListener(
        v -> startActivity(new Intent(SettingActivity.this, ServerConfigActivity.class)));

    viewBinding.pushConfigLayout.setOnClickListener(
        v -> {
          startActivity(new Intent(SettingActivity.this, PushConfigParseActivity.class));
        });

    viewBinding.kitConfigLayout.setVisibility(View.VISIBLE);
    viewBinding.kitConfigLayout.setOnClickListener(
        v -> {
          startActivity(new Intent(SettingActivity.this, SettingUIKitActivity.class));
        });

    viewBinding.tvLogout.setOnClickListener(
        v ->
            IMKitClient.logout(
                    new FetchCallback<Void>() {
                        @Override
                        public void onError(int errorCode, @NonNull String errorMsg) {
                            Toast.makeText(
                                            SettingActivity.this,
                                            "error code is " + errorCode + ", message is " + errorMsg,
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }

                        @Override
                        public void onSuccess(@Nullable Void data) {
                            if (getApplicationContext() instanceof IMApplication) {
                                ((IMApplication) getApplicationContext())
                                        .clearActivity(SettingActivity.this);
                            }
                            startActivity(new Intent(SettingActivity.this, WelcomeActivity.class));
                            finish();
                        }
                    }));
    viewBinding.settingTitleBar.setOnBackIconClickListener(v -> onBackPressed());
    if (AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin) {
      changeStatusBarColor(R.color.color_ededed);
      viewBinding.clRoot.setBackgroundResource(R.color.color_ededed);
      viewBinding.nextGroupLl.setBackgroundResource(R.color.color_white);
      ViewGroup.MarginLayoutParams layoutParams =
          (ViewGroup.MarginLayoutParams) viewBinding.nextGroupLl.getLayoutParams();
      layoutParams.setMargins(0, 0, 0, 0);
      viewBinding.nextGroupLl.setLayoutParams(layoutParams);

      viewBinding.notifyMessageLl.setBackgroundResource(R.color.color_white);
      ViewGroup.MarginLayoutParams layoutParamsN =
          (ViewGroup.MarginLayoutParams) viewBinding.notifyMessageLl.getLayoutParams();
      layoutParamsN.setMargins(0, SizeUtils.dp2px(6), 0, 0);
      viewBinding.notifyMessageLl.setLayoutParams(layoutParamsN);

      viewBinding.tvLogout.setBackgroundResource(R.color.color_white);
      ViewGroup.MarginLayoutParams layoutParamsL =
          (ViewGroup.MarginLayoutParams) viewBinding.tvLogout.getLayoutParams();
      layoutParamsL.setMargins(0, SizeUtils.dp2px(6), 0, 0);
      viewBinding.tvLogout.setLayoutParams(layoutParamsL);

      updateCommonView(
          R.drawable.fun_setting_bg_switch_thumb_selector,
          R.drawable.fun_setting_bg_switch_track_selector);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  private void updateCommonView(@DrawableRes int thumbRes, @DrawableRes int trackRes) {

    viewBinding.messageReadSc.setThumbResource(thumbRes);
    viewBinding.messageReadSc.setTrackResource(trackRes);

    viewBinding.playModeSc.setThumbResource(thumbRes);
    viewBinding.playModeSc.setTrackResource(trackRes);

    viewBinding.conversationModeSc.setThumbResource(thumbRes);
    viewBinding.conversationModeSc.setTrackResource(trackRes);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventCenter.unregisterEventNotify(langeNotify);
  }
}
