// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import java.util.Locale;

/** 语言设置 */
public class AppLanguageConfig {

  private AppLanguageConfig() {}

  private static class AppLanguageConfigHolder {
    private static final AppLanguageConfig instance = new AppLanguageConfig();
  }

  public static AppLanguageConfig getInstance() {
    return AppLanguageConfigHolder.instance;
  }

  //本地储存的语言类型key值
  private static final String APP_LANG_KEY = "app_lang_key";

  //中文类型
  public static final String APP_LANG_CHINESE = "zh";

  //英文
  public static final String APP_LANG_ENGLISH = "en";

  String currentLang;

  //获取语言类型
  public String getAppLanguage(Context context) {
    if (TextUtils.isEmpty(currentLang)) {
      currentLang = SPUtils.INSTANCE.getString(APP_LANG_KEY, "", context);
      if (TextUtils.isEmpty(currentLang)) {
        currentLang = getSystemLanguage().getLanguage();
      }
    }
    return currentLang;
  }

  //获取系统默认的语言
  private Locale getSystemLanguage() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return Resources.getSystem().getConfiguration().getLocales().get(0);
    } else {
      return Locale.getDefault();
    }
  }

  //设置语言类型
  public void setAppLanguage(Context context, String lang) {
    currentLang = lang;
    SPUtils.INSTANCE.saveString(APP_LANG_KEY, lang, context);
  }
}
