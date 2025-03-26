// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.crash;

import androidx.annotation.NonNull;
import com.netease.yunxin.app.im.IMApplication;
import com.netease.yunxin.app.im.utils.AppUtils;

public class AppCrashHandler implements Thread.UncaughtExceptionHandler {

  private static AppCrashHandler appCrashHandler;

  private Thread.UncaughtExceptionHandler defaultHandler;

  private IMApplication imApplication;

  public void initCrashHandler(IMApplication application) {
    this.imApplication = application;
    defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(this);
  }

  public static AppCrashHandler getInstance() {
    if (appCrashHandler == null) {
      appCrashHandler = new AppCrashHandler();
    }
    return appCrashHandler;
  }

  @Override
  public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
    imApplication.clearActivity(null);
    AppUtils.restartApp();
  }
}
