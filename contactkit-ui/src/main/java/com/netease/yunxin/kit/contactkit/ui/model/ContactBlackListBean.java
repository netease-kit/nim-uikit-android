/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.model;

import com.netease.yunxin.kit.corekit.im.model.UserInfo;

/**
 * data bean for friend
 */
public class ContactBlackListBean extends BaseContactBean {

    public UserInfo data;

    public ContactBlackListBean(UserInfo data) {
        this.data = data;
        viewType = IViewTypeConstant.CONTACT_BLACK_LIST;
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
