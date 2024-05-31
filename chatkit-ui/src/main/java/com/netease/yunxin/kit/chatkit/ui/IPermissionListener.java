// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.app.Activity;

public interface IPermissionListener {

  boolean requestPermissionDenied(Activity activity, String permission);

  boolean permissionDeniedForever(Activity activity, String permission);

  void onPermissionRequest(Activity activity, String[] permission);
}
