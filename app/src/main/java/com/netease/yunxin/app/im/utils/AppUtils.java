// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

public class AppUtils {

  public static String getAppVersionName(Context context) {
    String versionName = "";
    try {
      PackageManager pm = context.getPackageManager();
      PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
      versionName = packageInfo.versionName;
    } catch (Exception exception) {

    }
    return versionName;
  }

  //邮箱
  public static final String EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
  //邮箱检查，为空或者符合邮箱格式
  public static boolean checkEmail(String email) {
    return TextUtils.isEmpty(email) || email.matches(EMAIL);
  }
}
