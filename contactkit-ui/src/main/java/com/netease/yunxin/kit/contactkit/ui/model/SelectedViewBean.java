// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import androidx.annotation.Nullable;

/** 用于展示已选中的会话 */
public class SelectedViewBean {

  // 头像，可空
  private final String avatar;

  // 名称，不可空
  private final String name;

  // id，不可空
  private final String targetId;

  // 账号，群ID或者账号ID
  private final String accountId;

  // 成员数量
  private int memberCount;

  public SelectedViewBean(String avatar, String name, String id, String account, int memberCount) {
    this.avatar = avatar;
    this.name = name;
    this.targetId = id;
    this.accountId = account;
    this.memberCount = memberCount;
  }

  public SelectedViewBean(String avatar, String name, String id, String account) {
    this.avatar = avatar;
    this.name = name;
    this.targetId = id;
    this.accountId = account;
    this.memberCount = 0;
  }

  public String getAvatar() {
    return avatar;
  }

  public String getName() {
    return name;
  }

  public String getTargetId() {
    return targetId;
  }

  public int getMemberCount() {
    return memberCount;
  }

  public String getAccountId() {
    return accountId;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj instanceof SelectedViewBean) {
      return targetId.equals(((SelectedViewBean) obj).targetId);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return targetId.hashCode();
  }
}
