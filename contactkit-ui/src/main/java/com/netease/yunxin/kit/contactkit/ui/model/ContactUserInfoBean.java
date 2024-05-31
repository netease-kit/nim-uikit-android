// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.im2.model.V2UserInfo;

/** profile page user info */
public class ContactUserInfoBean {
  public V2UserInfo data;
  public UserWithFriend friendInfo;
  public boolean isFriend;
  public boolean isBlack;

  public ContactUserInfoBean(V2UserInfo user) {
    this.data = user;
  }

  public String getName() {
    String name = data.getUserInfoName();
    if (friendInfo != null) {
      name = friendInfo.getName();
    }

    return name;
  }
}
