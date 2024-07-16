// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ait;

import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitUserInfo;
import java.util.ArrayList;
import java.util.List;

/** @ 帮助类 */
public class AitHelper {

  /**
   * 转换群成员为AitUserInfo
   *
   * @param userInfoWithTeams 群成员
   * @return 不包含AI聊用户
   */
  public static List<AitUserInfo> convertTeamMemberToAitUserInfo(
      List<TeamMemberWithUserInfo> userInfoWithTeams) {
    List<AitUserInfo> aitUsers = new ArrayList<>(userInfoWithTeams.size());
    for (TeamMemberWithUserInfo userInfoWithTeam : userInfoWithTeams) {
      if (!AIUserManager.isAIChatUser(userInfoWithTeam.getAccountId())) {
        aitUsers.add(
            new AitUserInfo(
                userInfoWithTeam.getAccountId(),
                userInfoWithTeam.getName(),
                userInfoWithTeam.getName(false),
                userInfoWithTeam.getAvatar()));
      }
    }
    return aitUsers;
  }

  /**
   * 转换AI用户为AitUserInfo
   *
   * @param aiUsers AI用户
   * @return
   */
  public static List<AitUserInfo> convertAIUserToAitUserInfo(List<V2NIMAIUser> aiUsers) {
    List<AitUserInfo> aitUsers = new ArrayList<>(aiUsers.size());
    for (V2NIMAIUser aiUser : aiUsers) {
      String name = TextUtils.isEmpty(aiUser.getName()) ? aiUser.getAccountId() : aiUser.getName();
      aitUsers.add(new AitUserInfo(aiUser.getAccountId(), name, name, aiUser.getAvatar()));
    }
    return aitUsers;
  }
}
