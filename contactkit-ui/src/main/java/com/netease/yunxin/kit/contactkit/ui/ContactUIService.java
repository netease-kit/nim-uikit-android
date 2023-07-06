// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui;

import android.content.Context;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ChatService;
import com.netease.yunxin.kit.contactkit.ui.fun.addfriend.FunAddFriendActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.blacklist.FunBlackListActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.contact.FunContactActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.search.FunSearchActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.selector.FunContactSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.team.FunTeamListActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.userinfo.FunUserInfoActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.verify.FunVerifyListActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.addfriend.AddFriendActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.blacklist.BlackListActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.ContactActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.search.GlobalSearchActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.selector.ContactSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.team.TeamListActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.userinfo.UserInfoActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.verify.VerifyListActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

@Keep
public class ContactUIService extends ChatService {

  @NonNull
  @Override
  public String getServiceName() {
    return "ContactUIKit";
  }

  @NonNull
  @Override
  public String getVersionName() {
    return BuildConfig.versionName;
  }

  @NonNull
  @Override
  public ChatService create(@NonNull Context context) {
    //normal
    XKitRouter.registerRouter(
        RouterConstant.PATH_CONTACT_SELECTOR_PAGE, ContactSelectorActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_ADD_FRIEND_PAGE, AddFriendActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_USER_INFO_PAGE, UserInfoActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_MY_TEAM_PAGE, TeamListActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_MY_BLACK_PAGE, BlackListActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_MY_NOTIFICATION_PAGE, VerifyListActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CONTACT_PAGE, ContactActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_GLOBAL_SEARCH_PAGE, GlobalSearchActivity.class);

    //fun
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CONTACT_SELECTOR_PAGE, FunContactSelectorActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_ADD_FRIEND_PAGE, FunAddFriendActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_USER_INFO_PAGE, FunUserInfoActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_MY_TEAM_PAGE, FunTeamListActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_MY_BLACK_PAGE, FunBlackListActivity.class);
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_MY_NOTIFICATION_PAGE, FunVerifyListActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_CONTACT_PAGE, FunContactActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_GLOBAL_SEARCH_PAGE, FunSearchActivity.class);
    return this;
  }
}
