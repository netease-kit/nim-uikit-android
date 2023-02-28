// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

import android.text.TextUtils;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.IMKitConstant;
import java.util.List;

public class TeamUtils {

  //是否为讨论组
  public static boolean isTeamGroup(Team teamInfo) {
    String teamExtension = teamInfo.getExtension();
    if ((teamExtension != null && teamExtension.contains(IMKitConstant.TEAM_GROUP_TAG))
        || teamInfo.getType() == TeamTypeEnum.Normal) {
      return true;
    }

    return false;
  }

  public static String generateNameFromAccIdList(List<String> nameList, String defaultName) {

    int maxTeamNameLength = 30;
    if (nameList == null || nameList.size() < 1) {
      return defaultName;
    }
    int nameLength = Math.min(nameList.size(), maxTeamNameLength);
    List<String> names = nameList.subList(0, nameLength);
    String myName = IMKitClient.account();
    UserInfo userInfo = IMKitClient.getUserInfo();
    if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
      myName = userInfo.getName();
    }
    StringBuilder nameBuilder = new StringBuilder(myName);
    for (String item : names) {
      nameBuilder.append("、");
      nameBuilder.append(item);
    }
    return nameBuilder.substring(0, Math.min(maxTeamNameLength, nameBuilder.length()));
  }
}
