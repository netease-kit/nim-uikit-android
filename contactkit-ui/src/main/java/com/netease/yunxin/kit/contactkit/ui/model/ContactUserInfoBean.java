/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.model;

import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;

/**
 * profile page user info
 */
public class ContactUserInfoBean {
    public UserInfo data;
    public FriendInfo friendInfo;
    public boolean isFriend;
    public boolean isBlack;
    public boolean messageNotify = true;

    public ContactUserInfoBean(UserInfo user){
        this.data = user;
    }
}
