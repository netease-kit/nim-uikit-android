// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.utils;

import android.os.Handler;
import android.os.Looper;

public class MainTask {

  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  private MainTask() {}

  public static MainTask getInstance() {
    return Instance.task;
  }

  public void runOnUIThread(Runnable runnable) {
    mainHandler.post(runnable);
  }

  public void runOnUIThreadDelay(Runnable runnable, long delayMillis) {
    mainHandler.postDelayed(runnable, delayMillis);
  }

  public static class Instance {
    private static final MainTask task = new MainTask();
  }
}
