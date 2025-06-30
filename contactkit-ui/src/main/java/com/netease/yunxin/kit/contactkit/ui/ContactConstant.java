// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui;

public class ContactConstant {

  public static final String LIB_TAG = "ContactKit-UI";
  public static final String USER_INFO_KEY = "UserInfo";

  public static class SearchViewType {
    public static final int USER = 1;
    public static final int TEAM = 2;
    public static final int GROUP = 3;
    public static final int TITLE = 4;
  }

  // Error Code
  public static final int ERROR_CODE_EMPTY = 108404;
  //
  public static final int ERROR_TEAM_MEMBER_ALREADY_EXIT = 109311;
  //
  public static final int ERROR_TEAM_ACTION_EXPIRED = 109313;
  //无操作权限
  public static final int ERROR_TEAM_NO_PERMISSION = 109432;
  //通知已处理错误码
  public static final int ERROR_TEAM_APPLICATION_HAS_DONE = 109404;
  //群组已解散
  public static final int ERROR_TEAM_HAS_DISSOLVED = ERROR_CODE_EMPTY;
  // 邀请群人数达到上限
  public static final int ERROR_TEAM_MEMBER_LIMIT = 108437;
  // 同意邀请群人数达到上限
  public static final int ERROR_TEAM_MEMBER_JOIN_LIMIT = 108305;
}
