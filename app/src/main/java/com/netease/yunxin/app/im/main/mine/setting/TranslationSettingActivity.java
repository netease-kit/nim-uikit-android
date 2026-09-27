// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityTranslationSettingBinding;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.utils.SizeUtils;

/** 消息翻译设置页 - 目标语言：点击跳转语言选择页 - 自动翻译开关：开启时记录当前时间戳，关闭时设为 0 */
public class TranslationSettingActivity extends BaseLocalActivity {

  public static final int REQUEST_CODE_SELECT_LANGUAGE = 1001;

  private ActivityTranslationSettingBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityTranslationSettingBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
  }

  private void initView() {
    viewBinding.translationTitleBar.setOnBackIconClickListener(v -> onBackPressed());

    // 初始化目标语言显示
    refreshTargetLanguageText();

    // 初始化自动翻译开关状态
    long enableTime = DataUtils.getAutoTranslationEnableTime(this);
    viewBinding.scAutoTranslate.setChecked(enableTime > 0);

    // 点击目标语言区域，跳转语言选择页
    viewBinding.rlyTargetLanguage.setOnClickListener(
        v -> {
          Intent intent = new Intent(this, LanguageSelectActivity.class);
          startActivityForResult(intent, REQUEST_CODE_SELECT_LANGUAGE);
        });

    // 自动翻译开关
    viewBinding.scAutoTranslate.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          long enableTime1 = isChecked ? System.currentTimeMillis() : 0L;
          DataUtils.saveAutoTranslationEnableTime(this, enableTime1);
          IMKitConfigCenter.setAutoTranslationEnableTime(enableTime1);
        });

    android.view.ViewGroup.MarginLayoutParams layoutParams =
        (android.view.ViewGroup.MarginLayoutParams)
            viewBinding.llTranslationSettings.getLayoutParams();
    layoutParams.setMargins(SizeUtils.dp2px(20), SizeUtils.dp2px(16), SizeUtils.dp2px(20), 0);
    viewBinding.scAutoTranslate.setThumbResource(R.drawable.fun_setting_bg_switch_thumb_selector);
    viewBinding.scAutoTranslate.setTrackResource(R.drawable.fun_setting_bg_switch_track_selector);
    viewBinding.llTranslationSettings.setLayoutParams(layoutParams);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE_SELECT_LANGUAGE && resultCode == RESULT_OK) {
      refreshTargetLanguageText();
    }
  }

  /** 刷新目标语言文本显示 */
  private void refreshTargetLanguageText() {
    String langCode = DataUtils.getTranslationTargetLanguage(this);
    viewBinding.tvTargetLanguage.setText(getLanguageDisplayName(this, langCode));
  }

  /**
   * 根据语言代码获取本地化显示名称（走 string 资源，跟随系统语言）。
   *
   * @param context Context，用于读取 string 资源
   * @param langCode 语言代码（翻译接口参数 key，如 "zh"、"en"）
   * @return 当前语言环境下的语言名称
   */
  public static String getLanguageDisplayName(Context context, String langCode) {
    if (langCode == null) return "";
    int resId;
    switch (langCode) {
      case "zh-CHS":
        resId = R.string.translation_lang_zh_chs;
        break;
      case "zh-CHT":
        resId = R.string.translation_lang_zh_cht;
        break;
      case "ar":
        resId = R.string.translation_lang_ar;
        break;
      case "de":
        resId = R.string.translation_lang_de;
        break;
      case "en":
        resId = R.string.translation_lang_en;
        break;
      case "es":
        resId = R.string.translation_lang_es;
        break;
      case "fr":
        resId = R.string.translation_lang_fr;
        break;
      case "id":
        resId = R.string.translation_lang_id;
        break;
      case "it":
        resId = R.string.translation_lang_it;
        break;
      case "ja":
        resId = R.string.translation_lang_ja;
        break;
      case "ko":
        resId = R.string.translation_lang_ko;
        break;
      case "pt":
        resId = R.string.translation_lang_pt;
        break;
      case "ru":
        resId = R.string.translation_lang_ru;
        break;
      case "th":
        resId = R.string.translation_lang_th;
        break;
      case "vi":
        resId = R.string.translation_lang_vi;
        break;
      default:
        return langCode;
    }
    return context.getString(resId);
  }
}
