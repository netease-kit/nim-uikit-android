// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.searchkit.ui.commone;

import com.netease.yunxin.kit.alog.ALog;

public class XLog {

  public static void d(String log) {

    ALog.d(log);
  }

  public static void d(String tag, String log) {
    ALog.e(tag, log);
  }

  public static void d(String tag, String moduleName, String log) {
    ALog.e(tag, moduleName, log);
  }
}
