// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.app.Activity;

/** 权限申请回调 该接口定义了权限申请的回调方法，用于处理权限申请的结果。 */
public interface IPermissionListener {

  /**
   * 权限申请被拒绝
   *
   * @param activity 触发权限申请的活动
   * @param permission 被拒绝的权限
   * @return 是否消费该事件
   */
  boolean requestPermissionDenied(Activity activity, String permission);

  /**
   * 权限申请被永久拒绝
   *
   * @param activity 触发权限申请的活动
   * @param permission 被永久拒绝的权限
   * @return 是否消费该事件
   */
  boolean permissionDeniedForever(Activity activity, String permission);

  /**
   * 权限申请回调
   *
   * @param activity 触发权限申请的活动
   * @param permission 申请的权限
   */
  void onPermissionRequest(Activity activity, String[] permission);
}
