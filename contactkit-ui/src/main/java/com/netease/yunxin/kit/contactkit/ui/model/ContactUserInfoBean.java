// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.user.V2NIMUser;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;

/** profile page user info */
public class ContactUserInfoBean {
  public V2NIMUser data;
  public UserWithFriend friendInfo;
  public boolean isFriend;
  public boolean isBlack;

  public ContactUserInfoBean(V2NIMUser user) {
    this.data = user;
  }

  /**
   * 获取用户名，不包括备注
   *
   * @return 用户名
   */
  public String getUserName() {
    String name = data.getName();
    if (TextUtils.isEmpty(name)) {
      name = data.getAccountId();
    }
    return name;
  }

  public String getName() {
    String name = data.getName();
    if (friendInfo != null) {
      name = friendInfo.getName();
    }
    if (TextUtils.isEmpty(name)) {
      name = data.getAccountId();
    }
    return name;
  }
}
