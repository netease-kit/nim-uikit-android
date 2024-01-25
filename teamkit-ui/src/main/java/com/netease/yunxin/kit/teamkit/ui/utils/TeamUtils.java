// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

import static com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant.KEY_EXTENSION_AT_ALL;
import static com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant.TYPE_EXTENSION_NOTIFY_ALL;

import android.text.TextUtils;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.IMKitConstant;
import com.netease.yunxin.kit.teamkit.ui.R;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

public class TeamUtils {

  private static final String TAG = "TeamUtils";
  // 是否为讨论组
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

  public static ArrayList<String> getAccIdListFromInfoList(List<UserInfoWithTeam> sourceList) {
    ArrayList<String> result = new ArrayList<>();
    if (sourceList == null || sourceList.isEmpty()) {
      return result;
    }
    for (UserInfoWithTeam item : sourceList) {
      if (item == null || item.getUserInfo() == null) {
        continue;
      }
      result.add(item.getUserInfo().getAccount());
    }

    return result;
  }

  public static List<UserInfoWithTeam> filterMemberListFromInfoList(
      List<UserInfoWithTeam> sourceList, Set<String> filterSet) {
    if (filterSet == null || filterSet.isEmpty()) {
      return sourceList;
    }
    ArrayList<UserInfoWithTeam> result = new ArrayList<>();
    for (UserInfoWithTeam item : sourceList) {
      if (item == null || filterSet.contains(item.getAccId())) {
        continue;
      }
      result.add(item);
    }

    return result;
  }

  public static int getManagerCount(List<UserInfoWithTeam> sourceList) {
    if (sourceList == null || sourceList.isEmpty()) {
      return 0;
    }
    int count = 0;
    for (UserInfoWithTeam item : sourceList) {
      if (item == null) {
        continue;
      }
      if (item.getTeamInfo().getType() == TeamMemberType.Manager) {
        count++;
      }
    }
    return count;
  }

  public static String getTeamNotifyAllMode(Team teamInfo) {
    String result = TYPE_EXTENSION_NOTIFY_ALL;
    if (teamInfo == null) {
      return result;
    }
    String extension = teamInfo.getExtension();
    if (extension != null && extension.contains(KEY_EXTENSION_AT_ALL)) {
      try {
        JSONObject obj = new JSONObject(teamInfo.getExtension());
        result = obj.optString(KEY_EXTENSION_AT_ALL, TYPE_EXTENSION_NOTIFY_ALL);
      } catch (JSONException e) {
        ALog.e(TAG, "getTeamNotifyAllMode", e);
      }
    }
    return result;
  }

  public static String buildAtNotificationText(String type) {
    String permissionText = "";
    if (type.equals(TYPE_EXTENSION_NOTIFY_ALL)) {
      permissionText +=
          IMKitClient.getApplicationContext().getString(R.string.team_at_permission_all_tips);
    } else {
      permissionText +=
          IMKitClient.getApplicationContext().getString(R.string.team_at_permission_manager_tips);
    }
    return permissionText;
  }

  /**
   * 群成员类型排序 群主排在第一个 管理员按照加入时间排序，并排在群主之后 普通成员按照加入时间排序，并排在管理员和群主之后
   *
   * @return
   */
  public static Comparator<UserInfoWithTeam> teamManagerComparator() {
    return (o1, o2) -> {
      if (o1 == null || o2 == null) {
        return 0;
      }
      if (o1.getTeamInfo().getType() == TeamMemberType.Owner) {
        return -1;
      } else if (o2.getTeamInfo().getType() == TeamMemberType.Owner) {
        return 1;
      } else if (o1.getTeamInfo().getType() == o2.getTeamInfo().getType()) {
        return o1.getTeamInfo().getJoinTime() < o2.getTeamInfo().getJoinTime() ? -1 : 1;
      } else {
        if (o1.getTeamInfo().getType() == TeamMemberType.Manager) {
          return -1;
        } else {
          return 1;
        }
      }
    };
  }
}
