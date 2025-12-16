// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;
import com.netease.nimlib.coexist.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.nimlib.coexist.sdk.v2.user.V2NIMUser;
import com.netease.yunxin.kit.chatkit.cache.FriendUserCache;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserManager;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.model.UserWithFriend;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 用户信息缓存，主要用于消息列表中显示用户信息、@弹窗展示等 */
public class ChatUserCache {

  private ChatUserCache() {}

  private static class InstanceHolder {
    private static final ChatUserCache INSTANCE = new ChatUserCache();
  }

  public static ChatUserCache getInstance() {
    return InstanceHolder.INSTANCE;
  }

  //非好友的用户信息
  private final Map<String, V2NIMUser> userInfoMap = new HashMap<>();

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
    return TeamUserManager.getInstance().getAllMembersAccountIds();
  }

  public void addUserInfo(V2NIMUser userInfo) {
    if (userInfo != null && !TextUtils.isEmpty(userInfo.getAccountId())) {
      userInfoMap.put(userInfo.getAccountId(), userInfo);
    }
  }

  /**
   * 获取群成员信息(不包括用户信息，好友信息)
   *
   * @param account 用户账号
   * @return 群成员信息
   */
  public V2NIMTeamMember getTeamMemberOnly(String account) {
    return TeamUserManager.getInstance().getTeamMember(account);
  }

  public void clear() {
    userInfoMap.clear();
  }

  /**
   * 仅使用用户nick，忽略群昵称等
   *
   * @param account 用户账号
   * @return 用户昵称
   */
  public String getUserNick(String account, V2NIMConversationType type) {
    if (TextUtils.equals(account, IMKitClient.account())) {
      if (IMKitClient.currentUser() != null
          && !TextUtils.isEmpty(IMKitClient.currentUser().getName())) {
        return IMKitClient.currentUser().getName();
      }
    }
    if (type == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(account);
      if (friendInfo != null
          && friendInfo.getUserInfo() != null
          && !TextUtils.isEmpty(friendInfo.getUserInfo().getName())) {
        return friendInfo.getUserInfo().getName();
      } else if (userInfoMap.get(account) != null) {
        V2NIMUser user = userInfoMap.get(account);
        if (!TextUtils.isEmpty(user.getName())) {
          return user.getName();
        }
      }
    } else {
      V2NIMUser user = TeamUserManager.getInstance().getUserInfo(account);
      if (user != null && !TextUtils.isEmpty(user.getName())) {
        return user.getName();
      }
    }
    return account;
  }

  public String getNickname(String account, V2NIMConversationType type) {
    if (type == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      //本人先处理
      if (Objects.equals(account, IMKitClient.account())) {
        if (!TextUtils.isEmpty(IMKitClient.currentUser().getName())) {
          return IMKitClient.currentUser().getName();
        }
      }
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(account);
      if (friendInfo != null) {
        return friendInfo.getName();
      } else if (userInfoMap.get(account) != null) {
        return userInfoMap.get(account).getName();
      }
    } else {
      return TeamUserManager.getInstance().getNickname(account, true);
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
    return TeamUserManager.getInstance().getNickname(account, false);
  }

  /**
   * 获取用户头像
   *
   * @param account 用户账号
   * @param type 会话类型
   * @return 用户头像
   */
  public String getAvatar(String account, V2NIMConversationType type) {
    //本人先处理
    if (Objects.equals(account, IMKitClient.account())) {
      return IMKitClient.currentUser().getAvatar();
    }
    if (type == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(account);
      if (friendInfo != null) {
        return friendInfo.getAvatar();
      } else if (userInfoMap.get(account) != null) {
        return userInfoMap.get(account).getAvatar();
      }
    } else {
      return TeamUserManager.getInstance().getAvatar(account);
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
    if (TextUtils.equals(account, IMKitClient.account())) {
      return IMKitClient.currentUser();
    }
    if (type == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(account);
      if (friendInfo != null && friendInfo.getUserInfo() != null) {
        return friendInfo.getUserInfo();
      } else {
        return userInfoMap.get(account);
      }
    } else {
      return TeamUserManager.getInstance().getUserInfo(account);
    }
  }
}
