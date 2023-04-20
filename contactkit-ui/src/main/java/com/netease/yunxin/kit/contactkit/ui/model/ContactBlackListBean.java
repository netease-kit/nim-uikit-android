// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import android.text.TextUtils;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;

/** data bean for friend */
public class ContactBlackListBean extends BaseContactBean {

  public UserInfo data;
  public FriendInfo friendInfo;

  public ContactBlackListBean(UserInfo data) {
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
    return data.getAccount();
  }

  public String getAvatarName() {
    if (!TextUtils.isEmpty(data.getName())) {
      return data.getName();
    }
    return data.getAccount();
  }

  @Override
  public boolean isShowDivision() {
    return false;
  }

  @Override
  public String getTarget() {
    return data.getName();
  }
}
