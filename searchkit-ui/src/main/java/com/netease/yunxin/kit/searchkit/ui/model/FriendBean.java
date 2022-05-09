/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.searchkit.ui.model;

import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.searchkit.model.FriendSearchInfo;
import com.netease.yunxin.kit.searchkit.ui.commone.SearchConstant;

import java.util.Objects;

public class FriendBean extends BaseBean {
    private static final String TAG = "SearchFriendBean";
    public FriendSearchInfo friendSearchInfo;

    public FriendBean(FriendSearchInfo searchInfo){
        this.friendSearchInfo = searchInfo;
        this.viewType = SearchConstant.ViewType.USER;
        this.router = RouterConstant.PATH_CHAT_P2P;
        this.paramKey = RouterConstant.CHAT_KRY;
        this.param = searchInfo.getFriendInfo().getUserInfo();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendBean)) return false;
        FriendBean that = (FriendBean) o;
        return Objects.equals(friendSearchInfo.getFriendInfo().getAccount(), that.friendSearchInfo.getFriendInfo().getAccount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(friendSearchInfo.getFriendInfo().getAccount());
    }
}
