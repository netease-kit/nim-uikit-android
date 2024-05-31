// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncState;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncType;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.nimlib.sdk.v2.user.V2NIMUser;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.cache.FriendUserCache;
import com.netease.yunxin.kit.chatkit.impl.LoginDetailListenerImpl;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.im2.model.V2UserInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 用户信息缓存，主要用于消息列表中显示用户信息、@弹窗展示等 */
public class ChatUserCache {

  private ChatUserCache() {

    IMKitClient.addLoginDetailListener(
        new LoginDetailListenerImpl() {
          @Override
          public void onDataSync(
              @Nullable V2NIMDataSyncType type,
              @Nullable V2NIMDataSyncState state,
              @Nullable V2NIMError error) {
            super.onDataSync(type, state, error);
            if (type == V2NIMDataSyncType.V2NIM_DATA_SYNC_TEAM_MEMBER
                && state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED) {
              updateCurrentTeamMember();
              haveLoadAllTeamMembers = false;
            }
          }
        });
  }

  private static class InstanceHolder {
    private static final ChatUserCache INSTANCE = new ChatUserCache();
  }

  public static ChatUserCache getInstance() {
    return InstanceHolder.INSTANCE;
  }

  //群成员信息
  private final Map<String, V2NIMTeamMember> teamMemberMap = new HashMap<>();

  //非好友的用户信息
  private final Map<String, V2UserInfo> userInfoMap = new HashMap<>();

  private V2NIMTeam teamInfo;

  private V2NIMTeamMember curTeamMember;

  //置顶消息
  private IMMessageInfo topMessage;

  public boolean haveLoadAllTeamMembers = false;

  public void setTopMessage(IMMessageInfo topMessage) {
    this.topMessage = topMessage;
  }

  public IMMessageInfo getTopMessage() {
    return topMessage;
  }

  public void removeTopMessage() {
    topMessage = null;
  }

  public V2NIMTeamMember getCurTeamMember() {
    return curTeamMember;
  }

  public void setCurTeamMember(V2NIMTeamMember curTeamMember) {
    this.curTeamMember = curTeamMember;
  }

  public void addTeamMember(List<V2NIMTeamMember> teamMemberList) {
    for (V2NIMTeamMember teamMember : teamMemberList) {
      teamMemberMap.put(teamMember.getAccountId(), teamMember);
    }
  }

  public void updateTeamMember(V2NIMTeamMember teamMember) {
    teamMemberMap.put(teamMember.getAccountId(), teamMember);
  }

  public void removeTeamMember(String account) {
    teamMemberMap.remove(account);
  }

  public List<String> getAllTeamMemberAccounts() {
    return new ArrayList<>(teamMemberMap.keySet());
  }

  public void addTeamMembersCache(List<TeamMemberWithUserInfo> userList) {
    if (userList != null) {
      for (TeamMemberWithUserInfo user : userList) {
        addTeamMemberToCache(user);
      }
    }
  }

  /** 更新当前用户在群里的信息 */
  private void updateCurrentTeamMember() {
    if (teamInfo != null && IMKitClient.account() != null) {
      String account = IMKitClient.account();
      TeamRepo.getTeamMember(
          teamInfo.getTeamId(),
          V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
          account,
          new FetchCallback<V2NIMTeamMember>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {}

            @Override
            public void onSuccess(@Nullable V2NIMTeamMember data) {
              if (data != null) {
                curTeamMember = data;
                teamMemberMap.put(account, data);
              }
            }
          });
    }
  }

  /**
   * 添加单个群成员信息到缓存
   *
   * @param user 群成员信息
   */
  public void addTeamMemberToCache(TeamMemberWithUserInfo user) {
    String account = user.getAccountId();
    teamMemberMap.put(account, user.getTeamMember());
    //非好友的用户信息才存储
    if (!FriendUserCache.isFriend(account)) {
      userInfoMap.put(account, new V2UserInfo(account, user.getUserInfo()));
    }
  }

  /**
   * 获取群成员信息(不包括用户信息，好友信息)
   *
   * @param account 用户账号
   * @return 群成员信息
   */
  public V2NIMTeamMember getTeamMemberOnly(String account) {
    return teamMemberMap.get(account);
  }

  /**
   * 获取群成员信息,填充用户信息，好友信息
   *
   * @param teamMembers 群成员信息
   * @param callback 回调
   */
  public void fillUserInfoToTeamMember(
      List<V2NIMTeamMember> teamMembers, FetchCallback<List<TeamMemberWithUserInfo>> callback) {
    List<TeamMemberWithUserInfo> teamMemberWithUserInfos = new ArrayList<>();

    List<String> onCacheUsers = new ArrayList<>();
    for (V2NIMTeamMember teamMember : teamMembers) {
      teamMemberMap.put(teamMember.getAccountId(), teamMember);
      if (FriendUserCache.getFriendByAccount(teamMember.getAccountId()) != null) {
        TeamMemberWithUserInfo teamMemberInfo =
            new TeamMemberWithUserInfo(
                teamMember,
                FriendUserCache.getFriendByAccount(teamMember.getAccountId()).getUserInfo());
        teamMemberInfo.setFriendInfo(
            FriendUserCache.getFriendByAccount(teamMember.getAccountId()).getFriend());
        teamMemberWithUserInfos.add(teamMemberInfo);
      } else if (userInfoMap.get(teamMember.getAccountId()) != null) {
        TeamMemberWithUserInfo teamMemberInfo =
            new TeamMemberWithUserInfo(
                teamMember, userInfoMap.get(teamMember.getAccountId()).getNIMUserInfo());
        teamMemberWithUserInfos.add(teamMemberInfo);
      } else {
        onCacheUsers.add(teamMember.getAccountId());
      }
    }
    if (onCacheUsers.size() > 0) {
      ContactRepo.getUserInfo(
          onCacheUsers,
          new FetchCallback<>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              callback.onSuccess(teamMemberWithUserInfos);
            }

            @Override
            public void onSuccess(List<V2NIMUser> users) {
              if (users != null && users.size() > 0) {
                for (V2NIMUser user : users) {
                  userInfoMap.put(user.getAccountId(), new V2UserInfo(user.getAccountId(), user));
                  TeamMemberWithUserInfo teamMemberInfo =
                      new TeamMemberWithUserInfo(teamMemberMap.get(user.getAccountId()), user);
                  teamMemberWithUserInfos.add(teamMemberInfo);
                }
                callback.onSuccess(teamMemberWithUserInfos);
              }
            }
          });
    } else {
      callback.onSuccess(teamMemberWithUserInfos);
    }
  }

  /** 清空群成员缓存 */
  public void clearTeamMemberCache() {
    teamMemberMap.clear();
  }

  /**
   * 获取群成员信息,填充用户信息，好友信息
   *
   * @param account 用户账号
   * @return 群成员信息
   */
  public TeamMemberWithUserInfo getTeamMember(String account) {
    if (teamMemberMap.get(account) != null) {
      if (FriendUserCache.getFriendByAccount(account) != null) {
        TeamMemberWithUserInfo teamMemberInfo =
            new TeamMemberWithUserInfo(
                teamMemberMap.get(account),
                FriendUserCache.getFriendByAccount(account).getUserInfo());
        teamMemberInfo.setFriendInfo(FriendUserCache.getFriendByAccount(account).getFriend());
        return teamMemberInfo;
      } else if (userInfoMap.get(account) != null) {
        TeamMemberWithUserInfo teamMemberInfo =
            new TeamMemberWithUserInfo(
                teamMemberMap.get(account), userInfoMap.get(account).getNIMUserInfo());
        return teamMemberInfo;
      }
      ContactRepo.getUserInfoAndNotify(account);
    } else if (teamInfo != null) {
      //没有缓存的用户，直接获取用户信息
      List<String> accounts = new ArrayList<>();
      accounts.add(account);
      TeamRepo.getTeamMemberAndNotify(teamInfo.getTeamId(), accounts);
      ContactRepo.getUserInfoAndNotify(account);
    }
    return null;
  }

  public void addUserInfo(List<V2UserInfo> userInfoList) {
    for (V2UserInfo userInfo : userInfoList) {
      userInfoMap.put(userInfo.getAccountId(), userInfo);
    }
  }

  public List<TeamMemberWithUserInfo> fillUserWithTeamMember(List<V2NIMUser> users) {
    if (users != null && users.size() > 0) {
      List<TeamMemberWithUserInfo> teamMemberWithUserInfos = new ArrayList<>();
      for (V2NIMUser user : users) {
        userInfoMap.put(user.getAccountId(), new V2UserInfo(user.getAccountId(), user));
        if (teamMemberMap.get(user.getAccountId()) != null) {
          TeamMemberWithUserInfo teamMemberInfo =
              new TeamMemberWithUserInfo(teamMemberMap.get(user.getAccountId()), user);
          teamMemberInfo.setFriendInfo(
              FriendUserCache.getFriendByAccount(user.getAccountId()) == null
                  ? null
                  : FriendUserCache.getFriendByAccount(user.getAccountId()).getFriend());
          teamMemberWithUserInfos.add(teamMemberInfo);
        }
      }
      return teamMemberWithUserInfos;
    }
    return null;
  }

  public void addUserInfo(V2UserInfo userInfo) {
    userInfoMap.put(userInfo.getAccountId(), userInfo);
  }

  public V2UserInfo getUserInfo(String account) {
    if (userInfoMap.get(account) != null) {
      return userInfoMap.get(account);
    } else if (FriendUserCache.getFriendByAccount(account) != null) {
      V2UserInfo userInfo =
          new V2UserInfo(account, FriendUserCache.getFriendByAccount(account).getUserInfo());
      return userInfo;
    }
    return null;
  }

  public boolean containsUser(String account) {
    if (TextUtils.isEmpty(account)) {
      return false;
    }
    return FriendUserCache.isFriend(account) || userInfoMap.containsKey(account);
  }

  public void addFriendInfo(List<UserWithFriend> friendInfoList) {
    for (UserWithFriend friendInfo : friendInfoList) {
      if (friendInfo != null) {
        if (userInfoMap.get(friendInfo.getAccount()) == null || friendInfo.getUserInfo() != null) {
          userInfoMap.put(
              friendInfo.getAccount(),
              new V2UserInfo(friendInfo.getAccount(), friendInfo.getUserInfo()));
        }
      }
    }
  }

  public UserWithFriend getFriendInfo(String account) {
    if (account == null) {
      return null;
    }
    if (FriendUserCache.getFriendByAccount(account) != null) {
      return FriendUserCache.getFriendByAccount(account);
    } else if (userInfoMap.get(account) != null) {
      UserWithFriend friendInfo = new UserWithFriend(account, null);
      friendInfo.setUserInfo(userInfoMap.get(account).getNIMUserInfo());
      return friendInfo;
    } else {
      return new UserWithFriend(account, null);
    }
  }

  public void clear() {
    teamMemberMap.clear();
    userInfoMap.clear();
    teamInfo = null;
    haveLoadAllTeamMembers = false;
  }

  public String getName(TeamMemberWithUserInfo withTeam) {
    if (withTeam == null || withTeam.getUserInfo() == null) {
      ALog.d(LIB_TAG, "ChatUserCache", "getName,null:");
      return null;
    }
    String account = withTeam.getUserInfo().getAccountId();
    if (!TextUtils.isEmpty(account)) {
      String name = getName(account);
      if (!TextUtils.isEmpty(name) && !name.equals(account)) {
        return name;
      }
      if (!TextUtils.isEmpty(withTeam.getName())) {
        return withTeam.getName();
      }
    }

    return account;
  }

  /**
   * 仅使用用户nick，忽略群昵称等
   *
   * @param account 用户账号
   * @return 用户昵称
   */
  public String getUserNick(String account) {
    if (!TextUtils.isEmpty(account)) {
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(account);
      if (friendInfo != null
          && friendInfo.getUserInfo() != null
          && !TextUtils.isEmpty(friendInfo.getUserInfo().getName())) {
        return friendInfo.getUserInfo().getName();
      }
      V2UserInfo user = userInfoMap.get(account);
      if (user != null && !TextUtils.isEmpty(user.getName())) {
        return user.getName();
      }
    }
    return account;
  }

  public String getName(String account) {

    //当前用户
    if (Objects.equals(account, IMKitClient.account())) {
      if (IMKitClient.currentUser() != null
          && !TextUtils.isEmpty(IMKitClient.currentUser().getName())) {
        return IMKitClient.currentUser().getName();
      }
      return account;
    }

    if (!TextUtils.isEmpty(account)) {
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(account);
      if (friendInfo != null && !TextUtils.isEmpty(friendInfo.getAlias())) {
        return friendInfo.getAlias();
      }

      V2NIMTeamMember teamMember = teamMemberMap.get(account);
      if (teamMember != null && !TextUtils.isEmpty(teamMember.getTeamNick())) {
        return teamMember.getTeamNick();
      }
      //没有群成员信息则拉取并更新
      if (teamMember == null && !account.equals(IMKitClient.account()) && teamInfo != null) {

        List<String> accounts = new ArrayList<>();
        accounts.add(account);
        TeamRepo.getTeamMemberAndNotify(teamInfo.getTeamId(), accounts);
      }

      if (friendInfo != null && !TextUtils.isEmpty(friendInfo.getName())) {
        return friendInfo.getName();
      }

      if (userInfoMap.get(account) != null) {
        return userInfoMap.get(account).getName();
      }
    }

    ContactRepo.getUserInfoAndNotify(account);
    return account;
  }

  public String getAitName(TeamMemberWithUserInfo withTeam) {
    if (withTeam == null) {
      return null;
    }
    if (teamMemberMap.get(withTeam.getAccountId()) == null) {
      teamMemberMap.put(withTeam.getAccountId(), withTeam.getTeamMember());
    }

    TeamMemberWithUserInfo teamMemberWithUserInfo;
    if (FriendUserCache.getFriendByAccount(withTeam.getAccountId()) != null) {
      teamMemberWithUserInfo =
          new TeamMemberWithUserInfo(
              withTeam.getTeamMember(),
              FriendUserCache.getFriendByAccount(withTeam.getAccountId()).getUserInfo());
      teamMemberWithUserInfo.setFriendInfo(
          FriendUserCache.getFriendByAccount(withTeam.getAccountId()).getFriend());

    } else if (userInfoMap.get(withTeam.getAccountId()) != null) {
      teamMemberWithUserInfo =
          new TeamMemberWithUserInfo(
              withTeam.getTeamMember(), userInfoMap.get(withTeam.getAccountId()).getNIMUserInfo());
    } else {
      teamMemberWithUserInfo = withTeam;
    }
    return teamMemberWithUserInfo.getNameWithoutFriendAlias();
  }

  /**
   * 获取所有群成员信息
   *
   * @return 群成员信息
   */
  public List<TeamMemberWithUserInfo> getAllMemberWithoutCurrentUser() {
    List<TeamMemberWithUserInfo> teamMemberWithUserInfo = new ArrayList<>();
    for (V2NIMTeamMember teamMember : teamMemberMap.values()) {
      if (teamMember != null
          && !TextUtils.equals(IMKitClient.account(), teamMember.getAccountId())) {
        if (FriendUserCache.getFriendByAccount(teamMember.getAccountId()) != null) {
          TeamMemberWithUserInfo teamMemberInfo =
              new TeamMemberWithUserInfo(
                  teamMember,
                  FriendUserCache.getFriendByAccount(teamMember.getAccountId()).getUserInfo());
          teamMemberInfo.setFriendInfo(
              FriendUserCache.getFriendByAccount(teamMember.getAccountId()).getFriend());
          teamMemberWithUserInfo.add(teamMemberInfo);
        } else if (userInfoMap.get(teamMember.getAccountId()) != null) {
          TeamMemberWithUserInfo teamMemberInfo =
              new TeamMemberWithUserInfo(
                  teamMember, userInfoMap.get(teamMember.getAccountId()).getNIMUserInfo());
          teamMemberWithUserInfo.add(teamMemberInfo);
        } else {
          teamMemberWithUserInfo.add(new TeamMemberWithUserInfo(teamMember, null));
        }
      }
    }
    Collections.sort(teamMemberWithUserInfo, ChatUtils.teamManagerComparator());
    return teamMemberWithUserInfo;
  }

  /**
   * 获取群成员@展示名称
   *
   * @param account 用户账号
   * @return 群成员信息
   */
  public String getAitName(String account) {
    if (!TextUtils.isEmpty(account)) {
      V2NIMTeamMember teamMember = teamMemberMap.get(account);
      UserWithFriend friend = FriendUserCache.getFriendByAccount(account);
      //群昵称优先
      if (teamMember != null && !TextUtils.isEmpty(teamMember.getTeamNick())) {
        return teamMember.getTeamNick();
      }
      //获取用户信息，首先从好友信息中获取
      V2UserInfo userinfo;
      if (friend != null && friend.getUserInfo() != null) {
        userinfo = new V2UserInfo(account, friend.getUserInfo());
      } else {
        userinfo = userInfoMap.get(account) == null ? null : userInfoMap.get(account);
      }
      if (userinfo != null && !TextUtils.isEmpty(userinfo.getName())) {
        return userinfo.getName();
      }
    }
    return account;
  }

  public V2UserInfo getUserInfo(TeamMemberWithUserInfo withTeam) {
    if (withTeam == null || withTeam.getUserInfo() == null) {
      return null;
    }

    String account = withTeam.getUserInfo().getAccountId();
    if (!TextUtils.isEmpty(account)) {
      return userInfoMap.get(account);
    }

    return null;
  }

  public void setCurrentTeam(V2NIMTeam team) {
    teamInfo = team;
  }

  public V2NIMTeam getCurrentTeam() {
    return teamInfo;
  }
}
