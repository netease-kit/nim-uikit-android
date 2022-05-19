/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui;


import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.netease.yunxin.kit.common.ui.CommonUIClient;
import com.netease.yunxin.kit.contactkit.ContactService;
import com.netease.yunxin.kit.contactkit.ui.addfriend.AddFriendActivity;
import com.netease.yunxin.kit.contactkit.ui.selector.ContactSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.team.TeamListActivity;
import com.netease.yunxin.kit.contactkit.ui.userinfo.UserInfoActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

@Keep
public class ContactUIService extends ContactService {

    @NonNull
    @Override
    public String getServiceName() {
        return "ContactUIService";
    }

    @NonNull
    @Override
    public String getVersionName() {
        return BuildConfig.versionName;
    }

    @NonNull
    @Override
    public ContactService create(@NonNull Context context) {
        XKitRouter.registerRouter(RouterConstant.PATH_SELECTOR_ACTIVITY, ContactSelectorActivity.class);
        XKitRouter.registerRouter(RouterConstant.PATH_ADD_FRIEND_ACTIVITY, AddFriendActivity.class);
        XKitRouter.registerRouter(RouterConstant.PATH_USER_INFO_ACTIVITY, UserInfoActivity.class);
        XKitRouter.registerRouter(RouterConstant.PATH_TEAM_LIST, TeamListActivity.class);
        CommonUIClient.init(context);
        return this;
    }
}
