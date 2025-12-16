// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import com.netease.nimlib.coexist.sdk.search.model.RecordHitInfo;
import com.netease.yunxin.kit.chatkit.model.HitType;
import com.netease.yunxin.kit.corekit.coexist.im2.model.UserWithFriend;
import java.util.Objects;

/** data bean for friend */
public class ContactFriendBean extends BaseContactBean {

  public UserWithFriend data;

  public boolean isSelected;

  //搜索相关
  public HitType hitType = HitType.None;
  public RecordHitInfo recordHitInfo = null;

  public ContactFriendBean(UserWithFriend data) {
    this.data = data;
    weight = ContactBeanWeight.BASE_WEIGHT;
    viewType = IViewTypeConstant.CONTACT_FRIEND;
  }

  public boolean isSelected() {
    return isSelected;
  }

  public void setSelected(boolean selected) {
    isSelected = selected;
  }

  @Override
  public boolean isNeedToPinyin() {
    return true;
  }

  @Override
  public boolean isShowDivision() {
    return true;
  }

  @Override
  public String getTarget() {
    return data.getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ContactFriendBean that = (ContactFriendBean) o;
    return this.data.equals(that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data);
  }

  @Override
  public String getAccountId() {
    return data.getAccount();
  }
}
