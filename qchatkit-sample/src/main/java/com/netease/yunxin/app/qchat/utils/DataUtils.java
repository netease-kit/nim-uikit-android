// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class DataUtils {

  private static String appKey = null;
  private static int serverConfig = -1;

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
          String keyStr =
              getServerConfigType(context) == Constant.CHINA_CONFIG
                  ? Constant.CONFIG_APPKEY_KEY
                  : Constant.CONFIG_APPKEY_KEY_OVERSEA;
          appKey = appInfo.metaData.getString(keyStr);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return appKey;
  }

  public static int getServerConfigType(Context context) {
    if (serverConfig < 0) {
      serverConfig = getConfigShared(context).getInt(Constant.SERVER_CONFIG, Constant.CHINA_CONFIG);
    }
    return serverConfig;
  }

  public static SharedPreferences getConfigShared(Context context) {
    SharedPreferences sharedPreferences =
        context.getSharedPreferences(Constant.SERVER_CONFIG_FILE, Context.MODE_MULTI_PROCESS);
    return sharedPreferences;
  }

  public static float getSizeToM(long size) {
    return size / (1024.0f * 1024.0f);
  }
}
