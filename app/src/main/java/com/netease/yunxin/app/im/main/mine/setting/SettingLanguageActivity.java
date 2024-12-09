// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.im.AppSkinConfig;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityLanguageSettingBinding;
import com.netease.yunxin.app.im.utils.MultiLanguageUtils;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.utils.AppLanguageConfig;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;

public class SettingLanguageActivity extends BaseLocalActivity {
  private ActivityLanguageSettingBinding viewBinding;

  //当前设置的语言
  private String currentLanguage = AppLanguageConfig.APP_LANG_CHINESE;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    changeStatusBarColor(R.color.color_ededed);
    super.onCreate(savedInstanceState);
    viewBinding = ActivityLanguageSettingBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
  }

  private void initView() {
    currentLanguage = AppLanguageConfig.getInstance().getAppLanguage(this);
    if (currentLanguage.equals(AppLanguageConfig.APP_LANG_ENGLISH)) {
      viewBinding.ivEnglish.setVisibility(View.VISIBLE);
      viewBinding.ivChinese.setVisibility(View.GONE);
    } else {

      viewBinding.ivChinese.setVisibility(View.VISIBLE);
      viewBinding.ivEnglish.setVisibility(View.GONE);
    }
    viewBinding.langSettingTitleBar.setOnBackIconClickListener(v -> onBackPressed());
    viewBinding
        .langSettingTitleBar
        .setActionText(R.string.setting_save)
        .setActionListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                AppLanguageConfig.getInstance()
                    .setAppLanguage(IMKitClient.getApplicationContext(), currentLanguage);
                MultiLanguageUtils.changeLanguage(
                    SettingLanguageActivity.this, currentLanguage, "");
                EventCenter.notifyEvent(new MultiLanguageUtils.LangEvent());
                finish();
              }
            });
    LinearLayout.LayoutParams layoutParams =
        (LinearLayout.LayoutParams) viewBinding.llyContainer.getLayoutParams();
    if (AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin) {
      viewBinding.langSettingTitleBar.setActionTextColor(
          getResources().getColor(R.color.color_58be6b));
      layoutParams.setMargins(0, SizeUtils.dp2px(20), 0, SizeUtils.dp2px(20));
      viewBinding.llyContainer.setBackgroundColor(getResources().getColor(R.color.color_white));
    } else {
      viewBinding.langSettingTitleBar.setActionTextColor(
          getResources().getColor(R.color.color_337EFF));
      layoutParams.setMargins(
          SizeUtils.dp2px(20), SizeUtils.dp2px(20), SizeUtils.dp2px(20), SizeUtils.dp2px(20));
    }
    viewBinding.llyContainer.setLayoutParams(layoutParams);

    viewBinding.rlyChinese.setOnClickListener(
        v -> {
          changeLanguage(AppLanguageConfig.APP_LANG_CHINESE);
        });
    viewBinding.rlyEnglish.setOnClickListener(
        v -> {
          changeLanguage(AppLanguageConfig.APP_LANG_ENGLISH);
        });
  }

  /**
   * 切换语言
   *
   * @param lang 语言
   */
  private void changeLanguage(String lang) {
    this.currentLanguage = lang;
    if (lang.equals(AppLanguageConfig.APP_LANG_ENGLISH)) {
      viewBinding.ivChinese.setVisibility(View.GONE);
      viewBinding.ivEnglish.setVisibility(View.VISIBLE);
    } else {
      viewBinding.ivEnglish.setVisibility(View.GONE);
      viewBinding.ivChinese.setVisibility(View.VISIBLE);
    }
  }
}
