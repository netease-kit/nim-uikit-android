// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class DataUtils {

  private static String appKey = null;

  /** read appKey from manifest */
  public static String readAppKey(Context context) {
    if (appKey != null) {
      return appKey;
    }
    if (context != null) {

      try {
        ApplicationInfo appInfo =
            context
                .getPackageManager()
                .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        if (appInfo != null) {
          appKey = appInfo.metaData.getString(Constant.CONFIG_APPKEY_KEY);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return appKey;
  }

  public static float getSizeToM(long size) {
    return size / (1024.0f * 1024.0f);
  }
}
