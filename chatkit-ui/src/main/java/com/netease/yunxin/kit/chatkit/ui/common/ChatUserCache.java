// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 用户信息缓存，主要用于消息列表中显示用户信息、@弹窗展示等 */
public class ChatUserCache {

  private static final Map<String, TeamMember> teamMemberMap = new HashMap<>();
  private static final Map<String, FriendInfo> friendInfoMap = new HashMap<>();
  private static final Map<String, UserInfo> userInfoMap = new HashMap<>();

  public static void addTeamMember(List<TeamMember> teamMemberList) {
    for (TeamMember teamMember : teamMemberList) {
      teamMemberMap.put(teamMember.getAccount(), teamMember);
    }
  }

  public static void addUserCache(List<UserInfoWithTeam> userList) {
    if (userList != null) {
      for (UserInfoWithTeam user : userList) {
        String account = user.getTeamInfo().getAccount();
        teamMemberMap.put(account, user.getTeamInfo());
        if (user.getFriendInfo() != null) {
          friendInfoMap.put(account, user.getFriendInfo());
        }
        if (user.getUserInfo() != null) {
          userInfoMap.put(account, user.getUserInfo());
        }
      }
    }
  }

  public static TeamMember getTeamMember(String account) {
    return teamMemberMap.get(account);
  }

  public static void addUserInfo(List<UserInfo> userInfoList) {
    for (UserInfo userInfo : userInfoList) {
      userInfoMap.put(userInfo.getAccount(), userInfo);
      FriendInfo friendInfo = friendInfoMap.get(userInfo.getAccount());
      if (friendInfo != null) {
        friendInfo.setUserInfo(userInfo);
      }
    }
  }

  public static UserInfo getUserInfo(String account) {
    return userInfoMap.get(account);
  }

  public static void addFriendInfo(List<FriendInfo> friendInfoList) {
    for (FriendInfo friendInfo : friendInfoList) {
      if (friendInfo != null) {
        friendInfoMap.put(friendInfo.getAccount(), friendInfo);
        if (userInfoMap.get(friendInfo.getAccount()) == null || friendInfo.getUserInfo() != null) {
          userInfoMap.put(friendInfo.getAccount(), friendInfo.getUserInfo());
        }
      }
    }
  }

  public static FriendInfo getFriendInfo(String account) {
    return friendInfoMap.get(account);
  }

  public static void clear() {
    teamMemberMap.clear();
    friendInfoMap.clear();
    userInfoMap.clear();
  }

  public static String getName(UserInfoWithTeam withTeam) {
    if (withTeam == null || withTeam.getUserInfo() == null) {
      ALog.d(LIB_TAG, "ChatUserCache", "getName,null:");
      return null;
    }
    String account = withTeam.getUserInfo().getAccount();
    if (!TextUtils.isEmpty(account)) {
      FriendInfo friendInfo = friendInfoMap.get(account);
      if (friendInfo == null) {
        friendInfo = withTeam.getFriendInfo();
      }
      if (friendInfo == null) {
        friendInfo = ChatRepo.getFriendInfo(account);
        friendInfoMap.put(account, friendInfo);
      }
      if (friendInfo != null && !TextUtils.isEmpty(friendInfo.getAlias())) {
        return friendInfo.getAlias();
      }
      TeamMember teamMember = teamMemberMap.get(account);
      if (teamMember == null) {
        teamMember = withTeam.getTeamInfo();
      }
      String tid = withTeam.getTeamInfo().getTid();
      if (!TextUtils.isEmpty(teamMember.getTeamNick()) && TextUtils.isEmpty(tid)) {
        teamMember = ChatRepo.getTeamMember(tid, account);
        teamMemberMap.put(account, teamMember);
      }
      if (teamMember != null && !TextUtils.isEmpty(teamMember.getTeamNick())) {
        return teamMember.getTeamNick();
      }
      UserInfo userInfo = userInfoMap.get(account);
      if (userInfo == null) {
        userInfo = withTeam.getUserInfo();
      }
      if (userInfo == null) {
        userInfo = ChatRepo.getUserInfo(account);
        userInfoMap.put(account, userInfo);
      }
      if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
        return userInfo.getName();
      }
    }

    return account;
  }

  public static String getName(String account) {
    return getName(null, account);
  }

  public static String getName(String teamId, String account) {

    if (!TextUtils.isEmpty(account)) {
      FriendInfo friendInfo = friendInfoMap.get(account);
      if (friendInfo == null) {
        friendInfo = ChatRepo.getFriendInfo(account);
        friendInfoMap.put(account, friendInfo);
      }
      if (friendInfo != null && !TextUtils.isEmpty(friendInfo.getAlias())) {
        return friendInfo.getAlias();
      }

      if (!TextUtils.isEmpty(teamId)) {
        TeamMember teamMember = teamMemberMap.get(account);
        if (teamMember == null) {
          teamMember = ChatRepo.getTeamMember(teamId, account);
          teamMemberMap.put(account, teamMember);
        }
        if (teamMember != null
            && !TextUtils.isEmpty(teamMember.getTeamNick())
            && TextUtils.equals(teamId, teamMember.getTid())) {
          return teamMember.getTeamNick();
        }
      }

      UserInfo userInfo = userInfoMap.get(account);
      if (userInfo == null) {
        userInfo = ChatRepo.getUserInfo(account);
        userInfoMap.put(account, userInfo);
      }
      if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
        return userInfo.getName();
      }
    }

    return account;
  }

  public static String getAitName(UserInfoWithTeam withTeam) {
    if (withTeam == null || withTeam.getUserInfo() == null) {
      return null;
    }
    String account = withTeam.getUserInfo().getAccount();
    if (!TextUtils.isEmpty(account)) {
      TeamMember teamMember = teamMemberMap.get(account);
      if (teamMember == null) {
        teamMember = withTeam.getTeamInfo();
      }
      String tid = withTeam.getTeamInfo().getTid();
      if (!TextUtils.isEmpty(teamMember.getTeamNick()) && TextUtils.isEmpty(tid)) {
        teamMember = ChatRepo.getTeamMember(tid, account);
        teamMemberMap.put(account, teamMember);
      }
      if (teamMember != null && !TextUtils.isEmpty(teamMember.getTeamNick())) {
        return teamMember.getTeamNick();
      }
      UserInfo userInfo = userInfoMap.get(account);
      if (userInfo == null) {
        userInfo = withTeam.getUserInfo();
      }
      if (userInfo == null) {
        userInfo = ChatRepo.getUserInfo(account);
        userInfoMap.put(account, userInfo);
      }
      if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
        return userInfo.getName();
      }
    }

    return account;
  }

  public static String getAitName(String tid, String account) {
    if (!TextUtils.isEmpty(account)) {
      if (!TextUtils.isEmpty(tid)) {
        TeamMember teamMember = teamMemberMap.get(account);
        if (teamMember == null) {
          teamMember = ChatRepo.getTeamMember(tid, account);
          teamMemberMap.put(account, teamMember);
        }
        if (teamMember != null
            && !TextUtils.isEmpty(teamMember.getTeamNick())
            && TextUtils.equals(tid, teamMember.getTid())) {
          return teamMember.getTeamNick();
        }
      }
      UserInfo userInfo = userInfoMap.get(account);
      if (userInfo == null) {
        userInfo = ChatRepo.getUserInfo(account);
        userInfoMap.put(account, userInfo);
      }
      if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
        return userInfo.getName();
      }
    }
    return account;
  }

  public static UserInfo getUserInfo(UserInfoWithTeam withTeam) {
    if (withTeam == null || withTeam.getUserInfo() == null) {
      return null;
    }

    String account = withTeam.getUserInfo().getAccount();
    if (!TextUtils.isEmpty(account)) {
      UserInfo userInfo = userInfoMap.get(account);
      if (userInfo == null) {
        userInfo = ChatRepo.getUserInfo(account);
        userInfoMap.put(account, userInfo);
      }
      if (userInfo == null) {
        userInfo = withTeam.getUserInfo();
      }
      return userInfo;
    }

    return null;
  }
}
