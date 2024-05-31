// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

import static com.netease.yunxin.kit.chatkit.ChatConstants.KEY_EXTENSION_AT_ALL;
import static com.netease.yunxin.kit.chatkit.ChatConstants.KEY_EXTENSION_STICKY_PERMISSION;
import static com.netease.yunxin.kit.chatkit.ChatConstants.TYPE_EXTENSION_ALLOW_ALL;
import static com.netease.yunxin.kit.chatkit.ChatConstants.TYPE_EXTENSION_ALLOW_MANAGER;

import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamUpdateInfoMode;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.nimlib.sdk.v2.user.V2NIMUser;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.IMKitConstant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 群组工具类
 *
 * <p>
 */
public class TeamUtils {

  private static final String TAG = "TeamUtils";

  // 是否为讨论组
  public static boolean isTeamGroup(V2NIMTeam teamInfo) {
    String teamExtension = teamInfo.getServerExtension();
    return (teamExtension != null && teamExtension.contains(IMKitConstant.TEAM_GROUP_TAG))
        || teamInfo.getTeamType() == V2NIMTeamType.V2NIM_TEAM_TYPE_INVALID;
  }

  // 创建群时，生成的群名称
  public static String generateNameFromAccIdList(List<String> nameList, String defaultName) {

    int maxTeamNameLength = 30;
    if (nameList == null || nameList.size() < 1) {
      return defaultName;
    }
    int nameLength = Math.min(nameList.size(), maxTeamNameLength);
    List<String> names = nameList.subList(0, nameLength);
    String myName = IMKitClient.account();
    V2NIMUser userInfo = IMKitClient.currentUser();
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

  // 根据ID，获取群成员账号列表
  public static ArrayList<String> getAccIdListFromInfoList(
      List<TeamMemberWithUserInfo> sourceList) {
    ArrayList<String> result = new ArrayList<>();
    if (sourceList == null || sourceList.isEmpty()) {
      return result;
    }
    for (TeamMemberWithUserInfo item : sourceList) {
      if (item == null || item.getUserInfo() == null) {
        continue;
      }
      result.add(item.getUserInfo().getAccountId());
    }

    return result;
  }

  // 根据ID，过滤群成员账号列表
  public static List<TeamMemberWithUserInfo> filterMemberListFromInfoList(
      List<TeamMemberWithUserInfo> sourceList, Set<String> filterSet) {
    if (filterSet == null || filterSet.isEmpty()) {
      return sourceList;
    }
    ArrayList<TeamMemberWithUserInfo> result = new ArrayList<>();
    for (TeamMemberWithUserInfo item : sourceList) {
      if (item == null || filterSet.contains(item.getAccountId())) {
        continue;
      }
      result.add(item);
    }
    Collections.sort(result, teamManagerComparator());

    return result;
  }

  // 根据群组身份过滤
  public static List<TeamMemberWithUserInfo> filterMemberListWithRole(
      List<TeamMemberWithUserInfo> sourceList, V2NIMTeamMemberRole filterRole) {
    ArrayList<TeamMemberWithUserInfo> result = new ArrayList<>();
    if (filterRole == null) {
      result.addAll(sourceList);
    } else {
      for (TeamMemberWithUserInfo item : sourceList) {
        if (item != null && item.getMemberRole() == filterRole) {
          result.add(item);
        }
      }
    }
    Collections.sort(result, teamManagerComparator());
    return result;
  }

  // 获取群管理员数量
  public static int getManagerCount(List<TeamMemberWithUserInfo> sourceList) {
    if (sourceList == null || sourceList.isEmpty()) {
      return 0;
    }
    int count = 0;
    for (TeamMemberWithUserInfo item : sourceList) {
      if (item == null) {
        continue;
      }
      if (item.getTeamMember().getMemberRole()
          == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER) {
        count++;
      }
    }
    return count;
  }

  // 获取群@权限功能权限信息
  public static String getTeamAtMode(V2NIMTeam teamInfo) {
    String result = TYPE_EXTENSION_ALLOW_ALL;
    if (teamInfo == null) {
      return result;
    }
    String extension = teamInfo.getServerExtension();
    if (extension != null && extension.contains(KEY_EXTENSION_AT_ALL)) {
      try {
        JSONObject obj = new JSONObject(teamInfo.getServerExtension());
        result = obj.optString(KEY_EXTENSION_AT_ALL, TYPE_EXTENSION_ALLOW_ALL);
      } catch (JSONException e) {
        ALog.e(TAG, "getTeamNotifyAllMode", e);
      }
    }
    return result;
  }

  // 获取群置顶功能权限信息
  public static String getTeamTopStickyMode(V2NIMTeam teamInfo) {
    String result = TYPE_EXTENSION_ALLOW_MANAGER;
    if (teamInfo == null) {
      return result;
    }
    String extension = teamInfo.getServerExtension();
    if (extension != null && extension.contains(KEY_EXTENSION_STICKY_PERMISSION)) {
      try {
        JSONObject obj = new JSONObject(teamInfo.getServerExtension());
        result = obj.optString(KEY_EXTENSION_STICKY_PERMISSION, TYPE_EXTENSION_ALLOW_MANAGER);
      } catch (JSONException e) {
        ALog.e(TAG, "getTeamNotifyAllMode", e);
      }
    }
    return result;
  }

  /**
   * 群成员类型排序 群主排在第一个 管理员按照加入时间排序，并排在群主之后 普通成员按照加入时间排序，并排在管理员和群主之后
   *
   * @return 排序比较器
   */
  public static Comparator<TeamMemberWithUserInfo> teamManagerComparator() {
    return (o1, o2) -> {
      if (o1 == null || o2 == null || o1 == o2) {
        return 0;
      }

      int roleComparison =
          compareRoles(o1.getTeamMember().getMemberRole(), o2.getTeamMember().getMemberRole());
      if (roleComparison != 0) {
        return roleComparison;
      }

      // If roles are the same, compare based on join time
      return Long.compare(o1.getTeamMember().getJoinTime(), o2.getTeamMember().getJoinTime());
    };
  }

  private static int compareRoles(V2NIMTeamMemberRole role1, V2NIMTeamMemberRole role2) {
    if (role1 == role2) {
      return 0;
    } else if (role1 == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_OWNER) {
      return -1;
    } else if (role2 == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_OWNER) {
      return 1;
    } else if (role1 == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER) {
      return -1;
    } else {
      return 1;
    }
  }

  /**
   * 群成员设置页面排序 ，与列表也顺序相反
   *
   * @return 排序比较器
   */
  public static Comparator<TeamMemberWithUserInfo> teamSettingMemberComparator() {
    return (o1, o2) -> {
      if (o1 == null || o2 == null || o1 == o2) {
        return 0;
      }

      int roleComparison =
          compareRoles(o1.getTeamMember().getMemberRole(), o2.getTeamMember().getMemberRole());
      if (roleComparison != 0) {
        return -roleComparison;
      }

      // If roles are the same, compare based on join time
      return -Long.compare(o1.getTeamMember().getJoinTime(), o2.getTeamMember().getJoinTime());
    };
  }

  /** 是否有更新群信息权限 */
  public static boolean hasUpdateTeamInfoPermission(V2NIMTeam team, V2NIMTeamMember teamMember) {
    if (team == null || teamMember == null) {
      return false;
    }
    return (team.getUpdateInfoMode() == V2NIMTeamUpdateInfoMode.V2NIM_TEAM_UPDATE_INFO_MODE_ALL)
        || teamMember.getMemberRole() != V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_NORMAL
        || team.getTeamType() == V2NIMTeamType.V2NIM_TEAM_TYPE_INVALID;
  }
}
