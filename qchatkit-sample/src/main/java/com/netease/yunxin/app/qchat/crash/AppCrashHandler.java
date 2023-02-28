// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.crash;

import android.os.Process;
import androidx.annotation.NonNull;
import com.netease.yunxin.app.qchat.QChatApplication;

public class AppCrashHandler implements Thread.UncaughtExceptionHandler {

  private static AppCrashHandler appCrashHandler;

  private Thread.UncaughtExceptionHandler defaultHandler;

  private QChatApplication imApplication;

  public void initCrashHandler(QChatApplication application) {
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
    Process.killProcess(Process.myPid());
    System.exit(0);
    System.gc();
  }
}
