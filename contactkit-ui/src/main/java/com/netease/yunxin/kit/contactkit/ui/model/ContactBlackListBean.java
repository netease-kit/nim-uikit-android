// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.im2.model.V2UserInfo;

/** data bean for friend */
public class ContactBlackListBean extends BaseContactBean {

  public V2UserInfo data;
  public UserWithFriend friendInfo;

  public ContactBlackListBean(V2UserInfo data) {
    this.data = data;
    viewType = IViewTypeConstant.CONTACT_BLACK_LIST;
  }

  public String getName() {

    if (friendInfo != null && !TextUtils.isEmpty(friendInfo.getAlias())) {
      return friendInfo.getAlias();
    }

    if (!TextUtils.isEmpty(data.getName())) {
      return data.getName();
    }
    return data.getAccountId();
  }

  public String getAvatarName() {
    if (!TextUtils.isEmpty(data.getName())) {
      return data.getName();
    }
    return data.getAccountId();
  }

  @Override
  public boolean isShowDivision() {
    return false;
  }

  @Override
  public String getTarget() {
    return data.getName();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof ContactBlackListBean) {
      return data.getAccountId().equals(((ContactBlackListBean) obj).data.getAccountId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return data.getAccountId().hashCode();
  }
}
