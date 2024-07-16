// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserManager;

public class TeamMemberHelper {

  public static String getTeamMemberName(String teamId, String accountId) {
    return TeamUserManager.getInstance().getNickname(accountId, true);
  }

  public static String getTeamMemberAvatar(String teamId, String accountId) {
    return TeamUserManager.getInstance().getAvatar(accountId);
  }

  public static String getTeamMemberAvatarName(String teamId, String accountId) {
    return TeamUserManager.getInstance().getAvatarNickname(accountId);
  }
}
