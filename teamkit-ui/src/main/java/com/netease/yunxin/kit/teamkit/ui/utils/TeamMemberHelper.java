// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

public class TeamMemberHelper {

  public static String getTeamMemberName(String teamId, String accountId) {
    return TeamKitTeamMemberCache.getInstance().getNickname(teamId, accountId, true);
  }

  public static String getTeamMemberAvatar(String teamId, String accountId) {
    return TeamKitTeamMemberCache.getInstance().getAvatar(teamId, accountId);
  }

  public static String getTeamMemberAvatarName(String teamId, String accountId) {
    return TeamKitTeamMemberCache.getInstance().getAvatarNickname(teamId, accountId);
  }
}
