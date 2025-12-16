// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.corekit.event.BaseEvent;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import java.util.Locale;

public class MultiLanguageUtils {

  /**
   * 修改应用内语言设置
   *
   * @param language 语言
   * @param area 地区
   */
  public static void changeLanguage(Context context, String language, String area) {
    if (context == null || TextUtils.isEmpty(language)) {
      return;
    }
    //修改app语言，持久化语言选项信息
    Locale newLocale = new Locale(language, area);
    setAppLanguage(context, newLocale);
  }

  /**
   * 更新应用语言
   *
   * @param context 上下
   * @param locale 语言
   */
  private static void setAppLanguage(Context context, Locale locale) {
    Resources resources = context.getResources();
    DisplayMetrics metrics = resources.getDisplayMetrics();
    Configuration configuration = resources.getConfiguration();
    //Android 7.0以上的方法
    if (Build.VERSION.SDK_INT >= 24) {
      configuration.setLocale(locale);
      configuration.setLocales(new LocaleList(locale));
      context.createConfigurationContext(configuration);
      //实测，updateConfiguration这个方法虽然很多博主说是版本不适用
      //但是我的生产环境androidX+Android Q环境下，必须加上这一句，才可以通过重启App来切换语言
      resources.updateConfiguration(configuration, metrics);

    } else {
      //Android 4.1 以上方法
      configuration.setLocale(locale);
      resources.updateConfiguration(configuration, metrics);
    }

    //设置应用语言
    setApplicationLocal(locale);
  }

  /**
   * 设置应用的语言 解决应用作为Context时 getString 的问题
   *
   * @param locale 语言
   */
  public static void setApplicationLocal(Locale locale) {
    Resources resources = IMKitClient.getApplicationContext().getResources();
    DisplayMetrics metrics = resources.getDisplayMetrics();
    Configuration configuration = resources.getConfiguration();
    //Android 7.0以上的方法
    if (Build.VERSION.SDK_INT >= 24) {
      configuration.setLocale(locale);
      configuration.setLocales(new LocaleList(locale));
      IMKitClient.getApplicationContext().createConfigurationContext(configuration);
      //实测，updateConfiguration这个方法虽然很多博主说是版本不适用
      //但是我的生产环境androidX+Android Q环境下，必须加上这一句，才可以通过重启App来切换语言
      resources.updateConfiguration(configuration, metrics);

    } else {
      //Android 4.1 以上方法
      configuration.setLocale(locale);
      resources.updateConfiguration(configuration, metrics);
    }
  }

  //语言变更事件
  public static class LangEvent extends BaseEvent {
    @NonNull
    @Override
    public String getType() {
      return "langEvent";
    }
  }
}
