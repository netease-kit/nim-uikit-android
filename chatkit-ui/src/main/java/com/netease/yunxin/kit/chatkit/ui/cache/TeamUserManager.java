// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.cache;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginDetailListener;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMConnectStatus;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncState;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncType;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.nimlib.sdk.v2.user.V2NIMUser;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.cache.FriendUserCache;
import com.netease.yunxin.kit.chatkit.impl.TeamListenerImpl;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.listener.ContactChangeType;
import com.netease.yunxin.kit.corekit.im2.listener.ContactListener;
import com.netease.yunxin.kit.corekit.im2.model.FriendAddApplicationInfo;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 群聊用户信息管理 */
public class TeamUserManager {

  private static final String TAG = "TeamUserManager";
  //群成员缓存
  private final Map<String, V2NIMTeamMember> teamMemberMap = new HashMap<>();
  //用户信息缓存，只缓存非好友
  private final Map<String, V2NIMUser> userInfoMap = new HashMap<>();
  //当前群信息
  private V2NIMTeam currentTeam;
  //当前群id
  private String teamId;
  //是否已经加载过所有群成员
  private boolean haveLoadAllTeamMembers = false;

  //用户信息变化监听
  private final Set<TeamUserChangedListener> userChangedListeners = new HashSet<>();

  //群信息变化监听
  private final Set<TeamChangeListener> teamChangedListeners = new HashSet<>();

  //内部类实现全局单例
  private TeamUserManager() {}

  private static class ChatUserCacheHolder {
    public static final TeamUserManager INSTANCE = new TeamUserManager();
  }

  public static TeamUserManager getInstance() {
    return ChatUserCacheHolder.INSTANCE;
  }

  //初始化
  public void init(String teamId) {
    if (TextUtils.equals(this.teamId, teamId)) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "init");
    clear();
    this.teamId = teamId;
    registerListener();
  }

  //清除缓存
  public void clear() {
    ALog.d(LIB_TAG, TAG, "clear");
    teamMemberMap.clear();
    userInfoMap.clear();
    currentTeam = null;
    teamId = null;
    haveLoadAllTeamMembers = false;
    teamChangedListeners.clear();
    userChangedListeners.clear();
    unregisterListener();
  }

  //初始化相关监听，包括群监听、好友监听、用户监听以及登录监听
  private void registerListener() {
    //监听群信息变化
    TeamRepo.addTeamListener(teamListener);
    //监听好友信息变化
    ContactRepo.addContactListener(contactListener);
    // 监听登录
    IMKitClient.addLoginDetailListener(loginDetailListener);
  }

  private void unregisterListener() {
    //监听群信息变化
    TeamRepo.removeTeamListener(teamListener);
    //监听好友信息变化
    ContactRepo.removeContactListener(contactListener);
    IMKitClient.removeLoginDetailListener(loginDetailListener);
  }

  //刷新群成员信息
  private void getTeamMemberAndNotify(String account, boolean isAdd) {
    if (teamId == null || teamId.isEmpty()) {
      return;
    }

    TeamRepo.getTeamMember(
            teamId,
            V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
            account,
            new FetchCallback<V2NIMTeamMember>() {
              @Override
              public void onError(int errorCode, String errorMsg) {
                ALog.e(
                        TAG,
                        "get team member error, errorCode = " + errorCode + ", errorMsg = " + errorMsg);
              }

              @Override
              public void onSuccess(V2NIMTeamMember data) {
                if (data == null || !TextUtils.equals(data.getTeamId(), teamId)) {
                  return;
                }
                teamMemberMap.put(data.getAccountId(), data);
                for (TeamUserChangedListener listener : userChangedListeners) {
                  if (isAdd) {
                    listener.onUsersAdd(Collections.singletonList(data.getAccountId()));
                  } else {

                    listener.onUsersChanged(Collections.singletonList(data.getAccountId()));
                  }
                }
              }
            });
  }

  //刷新群信息
  private void getTeamInfoAndNotify() {
    if (teamId == null || teamId.isEmpty()) {
      return;
    }
    TeamRepo.getTeamInfo(
            teamId,
            new FetchCallback<V2NIMTeam>() {
              @Override
              public void onError(int errorCode, String errorMsg) {
                ALog.e(
                        TAG, "get team info error, errorCode = " + errorCode + ", errorMsg = " + errorMsg);
              }

              @Override
              public void onSuccess(V2NIMTeam data) {
                currentTeam = data;
                for (TeamChangeListener listener : teamChangedListeners) {
                  listener.onTeamUpdate(data);
                }
              }
            });
  }

  /**
   * 添加用户信息变化监听
   *
   * @param listener 监听器
   */
  public void addTeamChangedListener(TeamChangeListener listener) {
    teamChangedListeners.add(listener);
  }

  /**
   * 移除群信息变化监听
   *
   * @param listener 监听器
   */
  public void removeTeamChangedListener(TeamChangeListener listener) {
    teamChangedListeners.remove(listener);
  }

  /**
   * 添加用户信息变化监听
   *
   * @param listener 监听器
   */
  public void addMemberChangedListener(TeamUserChangedListener listener) {
    userChangedListeners.add(listener);
  }

  /**
   * 移除用户信息变化监听
   *
   * @param listener 监听器
   */
  public void removeMemberChangedListener(TeamUserChangedListener listener) {
    userChangedListeners.remove(listener);
  }

  /**
   * 获取当前群成员信息
   *
   * @return 群成员信息
   */
  public V2NIMTeamMember getCurTeamMember() {
    if (!TextUtils.isEmpty(IMKitClient.account())) {
      return teamMemberMap.get(IMKitClient.account());
    }
    return null;
  }

  /**
   * 设置当前群成员信息
   *
   * @param curTeamMember 群成员信息
   */
  public void setCurTeamMember(V2NIMTeamMember curTeamMember) {
    if (curTeamMember != null && !TextUtils.isEmpty(curTeamMember.getAccountId())) {
      teamMemberMap.put(curTeamMember.getAccountId(), curTeamMember);
    }
  }

  /**
   * 更新群成员信息
   *
   * @param teamMember 群成员信息
   */
  private void updateTeamMember(V2NIMTeamMember teamMember) {
    if (teamMember == null || !TextUtils.equals(teamMember.getTeamId(), teamId)) {
      return;
    }
    teamMemberMap.put(teamMember.getAccountId(), teamMember);
  }

  /**
   * 移除群成员信息
   *
   * @param account 用户账号
   */
  private void removeTeamMember(String account) {
    teamMemberMap.remove(account);
  }

  /**
   * 获取当前群信息
   *
   * @return 群信息，可能为空
   */
  public V2NIMTeam getCurrentTeam() {
    if (currentTeam == null && teamId != null && !teamId.isEmpty()) {
      getTeamInfoAndNotify();
    }
    return currentTeam;
  }

  /**
   * 获取群成员的用户信息并回调
   *
   * @param teamMembers 群成员
   */
  private void fetchMemberUserInfoAndNotify(
          final List<V2NIMTeamMember> teamMembers, boolean isAdd) {
    List<String> accounts = new ArrayList<>();
    List<String> noCacheUsers = new ArrayList<>();
    for (V2NIMTeamMember teamMember : teamMembers) {
      teamMemberMap.put(teamMember.getAccountId(), teamMember);
      accounts.add(teamMember.getAccountId());
      if (FriendUserCache.getFriendByAccount(teamMember.getAccountId()) == null
              || FriendUserCache.getFriendByAccount(teamMember.getAccountId()).getUserInfo() != null) {
        noCacheUsers.add(teamMember.getAccountId());
      }
    }
    for (TeamUserChangedListener listener : userChangedListeners) {
      if (isAdd) {
        listener.onUsersAdd(accounts);
      } else {
        listener.onUsersChanged(accounts);
      }
    }
    if (!noCacheUsers.isEmpty()) {
      getUserInfoAndNotify(noCacheUsers, false);
    }
  }

  /**
   * 获取用户信息并回调
   *
   * @param accounts 用户账号
   * @param isAdd 是否添加
   */
  private void getUserInfoAndNotify(List<String> accounts, boolean isAdd) {
    ContactRepo.getUserInfo(
            accounts,
            new FetchCallback<List<V2NIMUser>>() {
              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                ALog.e(
                        TAG, "get user info error, errorCode = " + errorCode + ", errorMsg = " + errorMsg);
              }

              @Override
              public void onSuccess(@Nullable List<V2NIMUser> data) {
                if (data != null) {
                  for (V2NIMUser userInfo : data) {
                    userInfoMap.put(userInfo.getAccountId(), userInfo);
                  }
                }
                for (TeamUserChangedListener listener : userChangedListeners) {
                  if (isAdd) {
                    listener.onUsersAdd(accounts);
                  } else {
                    listener.onUsersChanged(accounts);
                  }
                }
              }
            });
  }

  /**
   * 获取群成员信息
   *
   * @param account 用户账号
   * @return 群成员信息，可能为空
   */
  public V2NIMTeamMember getTeamMember(String account) {
    if (TextUtils.isEmpty(account)) {
      return null;
    }
    V2NIMTeamMember teamMember = teamMemberMap.get(account);
    if (teamMember == null && teamId != null) {
      getTeamMemberAndNotify(account, false);
    }
    return teamMember;
  }

  /**
   * 获取用户信息
   *
   * @param account
   * @return
   */
  public V2NIMUser getUserInfo(String account) {
    if (TextUtils.isEmpty(account)) {
      return null;
    }
    UserWithFriend friend = FriendUserCache.getFriendByAccount(account);
    V2NIMUser userInfo = userInfoMap.get(account);
    if (friend != null && friend.getUserInfo() != null) {
      userInfo = friend.getUserInfo();
    }
    if (userInfo == null) {
      getUserInfoAndNotify(Collections.singletonList(account), false);
    }
    return userInfo;
  }

  /** 获取群成员信息 */
  public void getTeamMembers(
          List<String> accounts, final FetchCallback<List<TeamMemberWithUserInfo>> callback) {
    if (teamId == null || teamId.isEmpty()) {
      if (callback != null) {
        callback.onError(-1, "teamId is null");
      }
      return;
    }
    List<TeamMemberWithUserInfo> teamMembers = new ArrayList<>();
    List<String> noCacheAccIds = new ArrayList<>();
    for (String account : accounts) {
      V2NIMTeamMember teamMember = teamMemberMap.get(account);
      if (teamMember != null) {
        TeamMemberWithUserInfo teamMemberWithUserInfo =
                new TeamMemberWithUserInfo(teamMember, null);
        V2NIMUser userInfo = userInfoMap.get(account);
        UserWithFriend friend = FriendUserCache.getFriendByAccount(account);
        if (friend != null) {
          teamMemberWithUserInfo.setFriendInfo(friend.getFriend());
          teamMemberWithUserInfo.setUserInfo(friend.getUserInfo());
        }

        if (userInfo != null) {
          teamMemberWithUserInfo.setUserInfo(userInfo);
        }
        teamMembers.add(teamMemberWithUserInfo);
      } else {
        noCacheAccIds.add(account);
      }
    }
    if (haveLoadAllTeamMembers || noCacheAccIds.isEmpty()) {
      if (callback != null) {
        callback.onSuccess(teamMembers);
      }
      return;
    }

    TeamRepo.getTeamMemberListWithUserInfoByIds(
            teamId,
            V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
            noCacheAccIds,
            new FetchCallback<List<TeamMemberWithUserInfo>>() {
              @Override
              public void onError(int errorCode, String errorMsg) {
                if (callback != null) {
                  callback.onError(errorCode, errorMsg);
                }
              }

              @Override
              public void onSuccess(List<TeamMemberWithUserInfo> data) {
                if (data != null) {
                  for (TeamMemberWithUserInfo member : data) {
                    teamMemberMap.put(member.getAccountId(), member.getTeamMember());
                    if (member.getUserInfo() != null
                            && !FriendUserCache.isFriend(member.getAccountId())) {
                      userInfoMap.put(member.getAccountId(), member.getUserInfo());
                    }
                  }
                }
                if (data != null) {
                  teamMembers.addAll(data);
                }
                if (callback != null) {
                  callback.onSuccess(teamMembers);
                }
              }
            });
  }

  /** 获取群成员信息 */
  public List<TeamMemberWithUserInfo> getTeamMembersFromCache(List<String> accounts) {
    if (teamId == null || teamId.isEmpty()) {
      return new ArrayList<>();
    }
    List<TeamMemberWithUserInfo> teamMembers = new ArrayList<>();
    for (String account : accounts) {
      V2NIMTeamMember teamMember = teamMemberMap.get(account);
      if (teamMember != null) {
        TeamMemberWithUserInfo teamMemberWithUserInfo =
                new TeamMemberWithUserInfo(teamMember, null);
        V2NIMUser userInfo = userInfoMap.get(account);
        UserWithFriend friend = FriendUserCache.getFriendByAccount(account);
        if (friend != null) {
          teamMemberWithUserInfo.setFriendInfo(friend.getFriend());
          teamMemberWithUserInfo.setUserInfo(friend.getUserInfo());
        }

        if (userInfo != null) {
          teamMemberWithUserInfo.setUserInfo(userInfo);
        }
        teamMembers.add(teamMemberWithUserInfo);
      }
    }
    return teamMembers;
  }

  /**
   * 根据群身份获取群成员信息
   *
   * @param teamId 群ID
   * @param role 群身份
   * @return 群成员信息
   */
  public List<TeamMemberWithUserInfo> getTeamMemberWithRoleListFromCache(
          String teamId, V2NIMTeamMemberRole role) {
    if (teamId == null || !teamId.equals(this.teamId)) {
      return new ArrayList<>();
    }
    List<TeamMemberWithUserInfo> teamMemberList = new ArrayList<>();
    if (role != null) {
      for (V2NIMTeamMember teamMember : teamMemberMap.values()) {
        if (teamMember.getMemberRole() == role) {
          TeamMemberWithUserInfo teamMemberWithUserInfo =
                  new TeamMemberWithUserInfo(teamMember, null);
          V2NIMUser userInfo = userInfoMap.get(teamMember.getAccountId());
          UserWithFriend friend = FriendUserCache.getFriendByAccount(teamMember.getAccountId());
          if (friend != null) {
            teamMemberWithUserInfo.setFriendInfo(friend.getFriend());
            teamMemberWithUserInfo.setUserInfo(friend.getUserInfo());
          }
          if (userInfo != null) {
            teamMemberWithUserInfo.setUserInfo(userInfo);
          }
          teamMemberList.add(teamMemberWithUserInfo);
        }
      }
    }
    return teamMemberList;
  }

  /**
   * 获取所有群成员的账号
   *
   * @return 所有群成员的账号
   */
  public List<String> getAllMembersAccountIds() {
    return new ArrayList<>(teamMemberMap.keySet());
  }

  /**
   * 获取所有群成员
   *
   * @param needSelf 是否需要自己
   * @param callback 回调
   */
  public void getAllTeamMembers(
          boolean needSelf, FetchCallback<List<TeamMemberWithUserInfo>> callback) {
    if (teamId == null || teamId.isEmpty()) {
      callback.onError(-1, "teamId is null");
      return;
    }
    if (haveLoadAllTeamMembers) {
      List<TeamMemberWithUserInfo> teamMembers = new ArrayList<>();
      for (V2NIMTeamMember teamMember : teamMemberMap.values()) {
        //去除自己
        if (!needSelf && TextUtils.equals(teamMember.getAccountId(), IMKitClient.account())) {
          continue;
        }
        //不在群里的不显示
        if (!teamMember.isInTeam()) {
          continue;
        }
        TeamMemberWithUserInfo teamMemberWithUserInfo =
                new TeamMemberWithUserInfo(teamMember, null);
        V2NIMUser userInfo = userInfoMap.get(teamMember.getAccountId());
        UserWithFriend friend = FriendUserCache.getFriendByAccount(teamMember.getAccountId());
        if (friend != null) {
          teamMemberWithUserInfo.setFriendInfo(friend.getFriend());
          teamMemberWithUserInfo.setUserInfo(friend.getUserInfo());
        }
        if (userInfo != null) {
          teamMemberWithUserInfo.setUserInfo(userInfo);
        }
        teamMembers.add(teamMemberWithUserInfo);
      }
      if (!teamMembers.isEmpty() && teamMembers.size() > 1) {
        Collections.sort(teamMembers, ChatUtils.teamManagerComparator());
      }
      callback.onSuccess(teamMembers);
      return;
    }
    TeamRepo.queryAllTeamMemberListWithUserInfo(
            teamId,
            V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
            new FetchCallback<List<TeamMemberWithUserInfo>>() {
              @Override
              public void onError(int errorCode, String errorMsg) {
                callback.onError(errorCode, errorMsg);
              }

              @Override
              public void onSuccess(List<TeamMemberWithUserInfo> data) {
                List<TeamMemberWithUserInfo> result = new ArrayList<>();
                if (data != null) {
                  if (data.size() > 1) {
                    Collections.sort(data, ChatUtils.teamManagerComparator());
                  }
                  for (TeamMemberWithUserInfo member : data) {
                    if (!member.getTeamMember().isInTeam()) {
                      continue;
                    }
                    teamMemberMap.put(member.getAccountId(), member.getTeamMember());
                    if (member.getUserInfo() != null
                            && !FriendUserCache.isFriend(member.getAccountId())) {
                      userInfoMap.put(member.getAccountId(), member.getUserInfo());
                    }
                    if (needSelf || !TextUtils.equals(member.getAccountId(), IMKitClient.account())) {
                      result.add(member);
                    }
                  }
                }
                haveLoadAllTeamMembers = true;
                //去除自己
                if (!needSelf) {
                  callback.onSuccess(result);
                } else {
                  callback.onSuccess(data);
                }
              }
            });
  }

  /**
   * 获取用户显示昵称
   *
   * @param account 用户账号
   * @param needFriendAlias 是否需要好友备注
   * @return 昵称
   */
  public String getNickname(String account, boolean needFriendAlias) {
    if (TextUtils.equals(account, IMKitClient.account())) {
      if (getCurTeamMember() != null && !TextUtils.isEmpty(getCurTeamMember().getTeamNick())) {
        return getCurTeamMember().getTeamNick();
      }
      return (IMKitClient.currentUser() != null
              && !TextUtils.isEmpty(IMKitClient.currentUser().getName()))
              ? IMKitClient.currentUser().getName()
              : account;
    }
    if (!account.isEmpty() && !TextUtils.isEmpty(teamId)) {
      if (userInfoMap.get(account) == null && teamMemberMap.get(account) == null) {
        TeamRepo.getTeamMemberListWithUserInfoByIds(
                teamId,
                V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
                new ArrayList<String>() {
                  {
                    add(account);
                  }
                },
                new FetchCallback<List<TeamMemberWithUserInfo>>() {
                  @Override
                  public void onError(int errorCode, String errorMsg) {}

                  @Override
                  public void onSuccess(List<TeamMemberWithUserInfo> data) {
                    if (data != null && !data.isEmpty()) {
                      List<String> users = new ArrayList<>();
                      for (TeamMemberWithUserInfo member : data) {
                        if (member.getTeamMember().isInTeam()) {
                          teamMemberMap.put(member.getAccountId(), member.getTeamMember());
                        }
                        if (member.getUserInfo() != null
                                && !FriendUserCache.isFriend(member.getAccountId())) {
                          userInfoMap.put(member.getAccountId(), member.getUserInfo());
                        }
                        users.add(member.getAccountId());
                      }
                      for (TeamUserChangedListener listener : userChangedListeners) {
                        listener.onUsersChanged(users);
                      }
                    } else {
                      //非群成员，获取用户信息
                      getUserInfoAndNotify(Collections.singletonList(account), false);
                    }
                  }
                });
      }
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(account);
      if (needFriendAlias && friendInfo != null && !TextUtils.isEmpty(friendInfo.getAlias())) {
        return friendInfo.getAlias();
      }
      V2NIMTeamMember teamMember = teamMemberMap.get(account);
      if (teamMember != null && !TextUtils.isEmpty(teamMember.getTeamNick())) {
        return teamMember.getTeamNick();
      }
      V2NIMUser userInfo = userInfoMap.get(account);
      //使用好友信息填充用户信息
      if (userInfo == null && friendInfo != null) {
        userInfo = friendInfo.getUserInfo();
      }
      if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
        return userInfo.getName();
      }
    }
    return account;
  }

  /**
   * 获取用户头像中显示昵称，当用户头像URL为空，则用改名字展示头像
   *
   * @param account 用户账号
   * @return 昵称
   */
  public String getAvatarNickname(String account) {
    if (TextUtils.equals(account, IMKitClient.account())) {
      return (IMKitClient.currentUser() != null
              && !TextUtils.isEmpty(IMKitClient.currentUser().getName()))
              ? IMKitClient.currentUser().getName()
              : account;
    }
    if (!account.isEmpty()) {
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(account);
      if (friendInfo != null && !TextUtils.isEmpty(friendInfo.getAlias())) {
        return friendInfo.getAlias();
      }
      V2NIMUser userInfo = userInfoMap.get(account);
      //使用好友信息填充用户信息
      if (userInfo == null && friendInfo != null) {
        userInfo = friendInfo.getUserInfo();
      }
      if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
        return userInfo.getName();
      }
      if (userInfo == null) {
        getUserInfoAndNotify(Collections.singletonList(account), false);
      }
    }
    return account;
  }

  /**
   * 获取头像
   *
   * @param accId 用户账号
   * @return 头像地址，可空
   */
  public String getAvatar(String accId) {
    if (FriendUserCache.getFriendByAccount(accId) != null) {
      return FriendUserCache.getFriendByAccount(accId).getAvatar();
    }
    if (userInfoMap.get(accId) != null) {
      return userInfoMap.get(accId).getAvatar();
    }
    getUserInfoAndNotify(Collections.singletonList(accId), false);
    return null;
  }

  private TeamListenerImpl teamListener =
          new TeamListenerImpl() {
            @Override
            public void onTeamInfoUpdated(V2NIMTeam team) {
              ALog.d(TAG, "onTeamInfoUpdated:");
              if (team == null || !TextUtils.equals(team.getTeamId(), teamId)) {
                return;
              }
              currentTeam = team;
              for (TeamChangeListener listener : teamChangedListeners) {
                listener.onTeamUpdate(team);
              }
            }

            @Override
            public void onTeamMemberJoined(List<V2NIMTeamMember> teamMembers) {
              ALog.d(TAG, "onTeamMemberJoined");
              if (teamMembers == null || teamMembers.isEmpty()) {
                return;
              }
              V2NIMTeamMember teamMember = teamMembers.get(0);
              ALog.d(TAG, "onTeamMemberJoined, team member:" + teamMember.getAccountId());
              if (TextUtils.equals(teamMember.getTeamId(), teamId)) {
                fetchMemberUserInfoAndNotify(teamMembers, true);
              }
            }

            @Override
            public void onTeamMemberKicked(
                    String operatorAccountId, List<V2NIMTeamMember> teamMembers) {
              ALog.d(TAG, "onTeamMemberKicked");
              if (teamMembers == null || teamMembers.isEmpty()) {
                return;
              }
              V2NIMTeamMember member = teamMembers.get(0);
              if (!TextUtils.equals(member.getTeamId(), teamId)) {
                return;
              }
              List<String> accounts = new ArrayList<>();
              for (V2NIMTeamMember teamMember : teamMembers) {
                removeTeamMember(teamMember.getAccountId());
                accounts.add(teamMember.getAccountId());
              }
              for (TeamUserChangedListener listener : userChangedListeners) {
                listener.onUserDelete(accounts);
              }
            }

            @Override
            public void onTeamMemberLeft(List<V2NIMTeamMember> teamMembers) {
              ALog.d(TAG, "onTeamMemberLeft");
              if (teamMembers == null || teamMembers.isEmpty()) {
                return;
              }
              V2NIMTeamMember member = teamMembers.get(0);
              if (!TextUtils.equals(member.getTeamId(), teamId)) {
                return;
              }
              List<String> accounts = new ArrayList<>();
              for (V2NIMTeamMember teamMember : teamMembers) {
                removeTeamMember(teamMember.getAccountId());
                accounts.add(teamMember.getAccountId());
              }
              for (TeamUserChangedListener listener : userChangedListeners) {
                listener.onUserDelete(accounts);
              }
            }

            @Override
            public void onTeamMemberInfoUpdated(List<V2NIMTeamMember> teamMembers) {
              ALog.d(TAG, "onTeamMemberInfoUpdated");
              if (teamMembers == null || teamMembers.isEmpty()) {
                return;
              }
              V2NIMTeamMember member = teamMembers.get(0);
              if (!TextUtils.equals(member.getTeamId(), teamId)) {
                return;
              }
              List<String> accounts = new ArrayList<>();
              for (V2NIMTeamMember teamMember : teamMembers) {
                updateTeamMember(teamMember);
                accounts.add(teamMember.getAccountId());
              }
              for (TeamUserChangedListener listener : userChangedListeners) {
                listener.onUsersChanged(accounts);
              }
            }

            @Override
            public void onTeamLeft(@Nullable V2NIMTeam team, boolean isKicked) {
              if (!TextUtils.equals(team.getTeamId(), teamId)) {
                return;
              }
              List<String> accounts = new ArrayList<>();
              removeTeamMember(IMKitClient.account());
              accounts.add(IMKitClient.account());
              for (TeamUserChangedListener listener : userChangedListeners) {
                listener.onUserDelete(accounts);
              }
            }

            @Override
            public void onTeamJoined(@Nullable V2NIMTeam team) {
              if (!TextUtils.equals(team.getTeamId(), teamId)) {
                return;
              }
              getTeamMemberAndNotify(IMKitClient.account(), true);
            }
          };

  private ContactListener contactListener =
          new ContactListener() {

            @Override
            public void onFriendAddRejected(@NonNull FriendAddApplicationInfo rejectionInfo) {}

            @Override
            public void onFriendAddApplication(@NonNull FriendAddApplicationInfo friendApplication) {}

            @Override
            public void onContactChange(
                    @NonNull ContactChangeType changeType,
                    @NonNull List<? extends UserWithFriend> contactList) {
              List<String> accounts = new ArrayList<>();
              for (UserWithFriend contact : contactList) {
                accounts.add(contact.getAccount());
                if (contact.getFriend() == null) {
                  userInfoMap.put(contact.getAccount(), contact.getUserInfo());
                }
              }
              for (TeamUserChangedListener listener : userChangedListeners) {
                listener.onUsersChanged(accounts);
              }
            }
          };

  private V2NIMLoginDetailListener loginDetailListener =
          new V2NIMLoginDetailListener() {
            @Override
            public void onConnectStatus(V2NIMConnectStatus status) {}

            @Override
            public void onDisconnected(V2NIMError error) {}

            @Override
            public void onConnectFailed(V2NIMError error) {}

            @Override
            public void onDataSync(V2NIMDataSyncType type, V2NIMDataSyncState state, V2NIMError error) {
              if (type == V2NIMDataSyncType.V2NIM_DATA_SYNC_MAIN
                      && state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED) {
                getTeamInfoAndNotify();
              }
              if (type == V2NIMDataSyncType.V2NIM_DATA_SYNC_TEAM_MEMBER
                      && state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED) {
                if (!TextUtils.isEmpty(IMKitClient.account())) {
                  getTeamMemberAndNotify(IMKitClient.account(), true);
                }
              }
            }
          };
}
