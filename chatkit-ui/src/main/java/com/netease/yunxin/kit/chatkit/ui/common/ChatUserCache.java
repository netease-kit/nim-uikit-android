// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.nimlib.sdk.v2.user.V2NIMUser;
import com.netease.yunxin.kit.chatkit.cache.FriendUserCache;
import com.netease.yunxin.kit.chatkit.cache.TeamMemberCache;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 用户信息缓存，主要用于消息列表中显示用户信息、@弹窗展示等 */
public class ChatUserCache {

  private ChatUserCache() {}

  private static final int MAX_NON_FRIEND_USER_CACHE_SIZE = 100;

  private static class InstanceHolder {
    private static final ChatUserCache INSTANCE = new ChatUserCache();
  }

  public static ChatUserCache getInstance() {
    return InstanceHolder.INSTANCE;
  }

  //非好友的用户信息
  private final Map<String, V2NIMUser> userInfoMap =
      new LinkedHashMap<String, V2NIMUser>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, V2NIMUser> eldest) {
          return size() > MAX_NON_FRIEND_USER_CACHE_SIZE;
        }
      };
  private final Map<String, String> conversationNameMap = new HashMap<>();

  //置顶消息
  private IMMessageInfo topMessage;

  public void setTopMessage(IMMessageInfo topMessage) {
    this.topMessage = topMessage;
  }

  public IMMessageInfo getTopMessage() {
    return topMessage;
  }

  public void removeTopMessage() {
    topMessage = null;
  }

  public List<String> getAllTeamMemberAccounts() {
    String teamId = currentTeamId();
    return TextUtils.isEmpty(teamId)
        ? Collections.emptyList()
        : TeamMemberCache.getAllTeamMemberAccounts(teamId);
  }

  public void addUserInfo(V2NIMUser userInfo) {
    if (userInfo != null && !TextUtils.isEmpty(userInfo.getAccountId())) {
      synchronized (userInfoMap) {
        userInfoMap.put(userInfo.getAccountId(), userInfo);
      }
    }
  }

  public void addConversationInfo(String conversationId, String name) {
    if (!TextUtils.isEmpty(conversationId)) {
      conversationNameMap.put(conversationId, name);
    }
  }

  public String getConversationInfo(String conversationId) {
    String name = conversationNameMap.get(conversationId);
    return name == null ? conversationId : name;
  }

  public void removeConversationInfo(String conversationId) {
    if (conversationNameMap.containsKey(conversationId)) {
      conversationNameMap.remove(conversationId);
    }
  }

  /**
   * 获取群成员信息(不包括用户信息，好友信息)
   *
   * @param account 用户账号
   * @return 群成员信息
   */
  public V2NIMTeamMember getTeamMemberOnly(String account) {
    String teamId = currentTeamId();
    return TextUtils.isEmpty(teamId) ? null : TeamMemberCache.getTeamMember(teamId, account);
  }

  public void clear() {
    synchronized (userInfoMap) {
      userInfoMap.clear();
    }
    removeTopMessage();
    conversationNameMap.clear();
  }

  public void clearSessionCache(String conversationId) {
    removeConversationInfo(conversationId);
    if (topMessage != null
        && TextUtils.equals(topMessage.getMessage().getConversationId(), conversationId)) {
      removeTopMessage();
    }
  }

  /**
   * 仅使用用户nick，忽略群昵称等
   *
   * @param account 用户账号
   * @return 用户昵称
   */
  public String getUserNick(String account, V2NIMConversationType type) {
    return getUserNick(account, type, currentTeamId());
  }

  public String getUserNick(String account, V2NIMConversationType type, String teamId) {
    if (TextUtils.equals(account, IMKitClient.account())) {
      V2NIMUser currentUser = IMKitClient.currentUser();
      if (currentUser != null && !TextUtils.isEmpty(currentUser.getName())) {
        return currentUser.getName();
      }
    }
    if (type == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(account);
      V2NIMUser friendUser = friendInfo == null ? null : friendInfo.getUserInfo();
      if (friendUser != null && !TextUtils.isEmpty(friendUser.getName())) {
        return friendUser.getName();
      } else {
        V2NIMUser user = getCachedUserInfo(account);
        if (user == null) {
          return account;
        }
        if (!TextUtils.isEmpty(user.getName())) {
          return user.getName();
        }
      }
    } else {
      V2NIMUser user = getTeamUserInfo(account, teamId);
      if (user != null && !TextUtils.isEmpty(user.getName())) {
        return user.getName();
      }
    }
    return account;
  }

  public String getNickname(String account, V2NIMConversationType type) {
    return getNickname(account, type, currentTeamId());
  }

  public String getNickname(String account, V2NIMConversationType type, String teamId) {
    if (type == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      //本人先处理
      if (Objects.equals(account, IMKitClient.account())) {
        V2NIMUser currentUser = IMKitClient.currentUser();
        if (currentUser != null && !TextUtils.isEmpty(currentUser.getName())) {
          return currentUser.getName();
        }
      }
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(account);
      if (friendInfo != null) {
        return friendInfo.getName();
      } else {
        V2NIMUser user = getCachedUserInfo(account);
        if (user != null) {
          return user.getName();
        }
      }
    } else {
      return getTeamNickname(account, true, teamId);
    }
    return account;
  }

  /**
   * 获取成员头像中展示名称
   *
   * @param account 用户账号
   * @return 群成员信息
   */
  public String getAvatarName(String account, V2NIMConversationType type) {
    return getAvatarName(account, type, currentTeamId());
  }

  public String getAvatarName(String account, V2NIMConversationType type, String teamId) {
    if (type == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      //本人先处理
      if (Objects.equals(account, IMKitClient.account())) {
        V2NIMUser currentUser = IMKitClient.currentUser();
        if (currentUser != null && !TextUtils.isEmpty(currentUser.getName())) {
          return currentUser.getName();
        }
      }
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(account);
      if (friendInfo != null) {
        return friendInfo.getAvatarName();
      } else {
        V2NIMUser user = getCachedUserInfo(account);
        if (user != null) {
          return user.getName();
        }
      }
    } else {
      return getTeamAvatarName(account, teamId);
    }
    return account;
  }

  /**
   * 获取群成员@展示名称
   *
   * @param account 用户账号
   * @return 群成员信息
   */
  public String getAitName(String account) {
    return getTeamNickname(account, false);
  }

  public String getAitName(String account, String teamId) {
    return getTeamNickname(account, false, teamId);
  }

  /**
   * 获取用户头像
   *
   * @param account 用户账号
   * @param type 会话类型
   * @return 用户头像
   */
  public String getAvatar(String account, V2NIMConversationType type) {
    return getAvatar(account, type, currentTeamId());
  }

  public String getAvatar(String account, V2NIMConversationType type, String teamId) {
    //本人先处理
    if (Objects.equals(account, IMKitClient.account())) {
      V2NIMUser currentUser = IMKitClient.currentUser();
      return currentUser == null ? null : currentUser.getAvatar();
    }
    if (type == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(account);
      if (friendInfo != null) {
        return friendInfo.getAvatar();
      } else {
        V2NIMUser user = getCachedUserInfo(account);
        if (user != null) {
          return user.getAvatar();
        }
      }
    } else {
      return getTeamAvatar(account, teamId);
    }
    return null;
  }

  /**
   * 获取用户信息,可能为空
   *
   * @param account 用户账号
   * @param type 会话类型
   * @return 用户信息
   */
  public V2NIMUser getUserInfo(String account, V2NIMConversationType type) {
    return getUserInfo(account, type, currentTeamId());
  }

  public V2NIMUser getUserInfo(String account, V2NIMConversationType type, String teamId) {
    if (TextUtils.equals(account, IMKitClient.account())) {
      return IMKitClient.currentUser();
    }
    if (type == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(account);
      if (friendInfo != null && friendInfo.getUserInfo() != null) {
        return friendInfo.getUserInfo();
      } else {
        return getCachedUserInfo(account);
      }
    } else {
      return getTeamUserInfo(account, teamId);
    }
  }

  private V2NIMUser getCachedUserInfo(String account) {
    synchronized (userInfoMap) {
      return userInfoMap.get(account);
    }
  }

  private String currentTeamId() {
    return ChatRepo.INSTANCE.getCurrentTeam() == null
        ? null
        : ChatRepo.INSTANCE.getCurrentTeam().getTeamId();
  }

  private V2NIMUser getTeamUserInfo(String account) {
    return getTeamUserInfo(account, currentTeamId());
  }

  private V2NIMUser getTeamUserInfo(String account, String teamId) {
    return TextUtils.isEmpty(teamId) ? null : TeamMemberCache.getUserInfo(teamId, account);
  }

  private String getTeamNickname(String account, boolean needAlias) {
    return getTeamNickname(account, needAlias, currentTeamId());
  }

  private String getTeamNickname(String account, boolean needAlias, String teamId) {
    if (TextUtils.isEmpty(teamId)) {
      return account;
    }
    String nickname = TeamMemberCache.getNickname(teamId, account, needAlias);
    return nickname == null ? account : nickname;
  }

  private String getTeamAvatarName(String account) {
    return getTeamAvatarName(account, currentTeamId());
  }

  private String getTeamAvatarName(String account, String teamId) {
    if (TextUtils.isEmpty(teamId)) {
      return account;
    }
    String name = TeamMemberCache.getAvatarName(teamId, account);
    return name == null ? account : name;
  }

  private String getTeamAvatar(String account) {
    return getTeamAvatar(account, currentTeamId());
  }

  private String getTeamAvatar(String account, String teamId) {
    return TextUtils.isEmpty(teamId) ? null : TeamMemberCache.getAvatar(teamId, account);
  }
}
