// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model.ait;

import com.netease.nimlib.sdk.search.model.RecordHitInfo;
import com.netease.yunxin.kit.chatkit.model.HitType;

/** @ 显示的信息 因为可能是群里，也可能是数字人，所以抽离单独类 */
public class AitUserInfo {
  // 账号
  private String account;
  // 名称
  private String showName;
  private String avatarName;

  //@之后显示的信息
  private String aitName;
  // 头像
  private String avatar;

  private String friendAlias;
  private String teamNick;
  private String userName;
  private HitType searchHitType = HitType.None;
  private RecordHitInfo searchHitInfo;

  //是否是AI数字人
  private boolean isAI = false;
  // AI数字人是否属于当前群，只有群成员才允许搜索
  private boolean aiSearchable = false;

  public AitUserInfo(
      String account, String showName, String avatarName, String aitName, String avatar) {
    this.account = account;
    this.showName = showName;
    this.avatarName = avatarName;
    this.aitName = aitName;
    this.avatar = avatar;
  }

  public void setAI(boolean AI) {
    isAI = AI;
  }

  public boolean isAI() {
    return isAI;
  }

  public void setAISearchable(boolean aiSearchable) {
    this.aiSearchable = aiSearchable;
  }

  public boolean isAISearchable() {
    return aiSearchable;
  }

  public String getAccount() {
    return account;
  }

  public String getAvatarName() {
    return avatarName;
  }

  public String getShowName() {
    return showName;
  }

  public String getAitName() {
    return aitName;
  }

  public String getAvatar() {
    return avatar;
  }

  public String getFriendAlias() {
    return friendAlias;
  }

  public void setFriendAlias(String friendAlias) {
    this.friendAlias = friendAlias;
  }

  public String getTeamNick() {
    return teamNick;
  }

  public void setTeamNick(String teamNick) {
    this.teamNick = teamNick;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public HitType getSearchHitType() {
    return searchHitType;
  }

  public void setSearchHitType(HitType searchHitType) {
    this.searchHitType = searchHitType;
  }

  public RecordHitInfo getSearchHitInfo() {
    return searchHitInfo;
  }

  public void setSearchHitInfo(RecordHitInfo searchHitInfo) {
    this.searchHitInfo = searchHitInfo;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public void setAvatarName(String avatarName) {
    this.avatarName = avatarName;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }
}
