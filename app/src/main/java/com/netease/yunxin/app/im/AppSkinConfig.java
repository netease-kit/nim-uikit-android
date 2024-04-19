// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im;

import com.netease.yunxin.kit.corekit.im2.utils.PreferenceUtils;

/**
 * App UI 风格设置IM UIKit 包含协同版和通用版 在加载页面的时候，可以根据需要加载不同风格的Fragment或者Activity。
 * 这里在Demo层面增加的开关，让用户客户在Demo层面切换看到两种不同的UI风格
 */
public class AppSkinConfig {

  private AppSkinConfig() {}

  private static class AppSkinConfigHolder {
    private static final AppSkinConfig instance = new AppSkinConfig();
  }

  public static AppSkinConfig getInstance() {
    return AppSkinConfigHolder.instance;
  }

  //本地储存的皮肤类型key值
  private static final String APP_SKIN_KEY = "app_skin_key";

  //基础类型
  public static final int APP_SKIN_BASE_TYPE = 0;

  //通用皮肤
  public static final int APP_SKIN_COMMON = 1;

  AppSkin currentSkin;

  //获取皮肤类型
  public AppSkin getAppSkinStyle() {
    if (currentSkin == null) {
      int style = PreferenceUtils.INSTANCE.getInt(APP_SKIN_KEY, APP_SKIN_BASE_TYPE);
      currentSkin = AppSkin.typeOfValue(style);
    }
    return currentSkin;
  }

  public void setAppSkinStyle(AppSkin style) {
    currentSkin = style;
    PreferenceUtils.INSTANCE.saveInt(APP_SKIN_KEY, style.getValue());
  }

  public enum AppSkin {
    //基础类型
    baseSkin(APP_SKIN_BASE_TYPE),
    //    通用皮肤
    commonSkin(APP_SKIN_COMMON);

    final int value;

    AppSkin(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public static AppSkin typeOfValue(int value) {
      for (AppSkin e : values()) {
        if (e.getValue() == value) {
          return e;
        }
      }
      return baseSkin;
    }
  }
}
