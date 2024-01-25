// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

public class TeamUIKitConstant {

  public static final int KEY_MANAGER_MAX_COUNT = 10;

  //群不存在，或者没有权限都是该错误码
  public static final int QUIT_TEAM_ERROR_CODE_NO_MEMBER = 802;
  public static final int REMOVE_MEMBER_ERROR_CODE_NO_PERMISSION = 403;

  public static final String KEY_TEAM_INFO = "team_info";

  public static final String KEY_EXTENSION_AT_ALL = "yxAllowAt";
  public static final String TYPE_EXTENSION_NOTIFY_ALL = "all";
  public static final String TYPE_EXTENSION_NOTIFY_MANAGER = "manager";
}
