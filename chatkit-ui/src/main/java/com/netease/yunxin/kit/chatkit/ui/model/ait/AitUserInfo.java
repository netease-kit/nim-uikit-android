// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model.ait;

/** @ 显示的信息 因为可能是群里，也可能是数字人，所以抽离单独类 */
public class AitUserInfo {
  // 账号
  private String account;
  // 名称
  private String name;

  //@之后显示的信息
  private String aitName;
  // 头像
  private String avatar;

  //是否是AI数字人
  private boolean isAI = false;

  public AitUserInfo(String account, String name, String aitName, String avatar) {
    this.account = account;
    this.name = name;
    this.aitName = aitName;
    this.avatar = avatar;
  }

  public void setAI(boolean AI) {
    isAI = AI;
  }

  public boolean isAI() {
    return isAI;
  }

  public String getAccount() {
    return account;
  }

  public String getName() {
    return name;
  }

  public String getAitName() {
    return aitName;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }
}
