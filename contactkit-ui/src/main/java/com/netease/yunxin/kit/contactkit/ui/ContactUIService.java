// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui;

import android.content.Context;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ChatService;
import com.netease.yunxin.kit.contactkit.ui.fun.addfriend.FunAddFriendActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.ai.FunAIUserListActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.blacklist.FunBlackListActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.contact.FunContactActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.robot.FunRobotBindActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.robot.FunRobotCreateActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.robot.FunRobotInfoActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.robot.FunRobotListActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.search.FunSearchActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.selector.FunContactSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.selector.ai.FunAIContactSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.selector.forward.FunForwardSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.team.FunTeamListActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.team.FunTeamProfileActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.team.FunTeamSearchActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.userinfo.FunUserInfoActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.verify.FunContactVerifyActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.verify.FunFriendVerifyActivity;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
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

    // 通用版人员选择器，界面风格和功能侧重与协同版不同
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CONTACT_SELECTOR_PAGE, FunContactSelectorActivity.class);
    // 通用版AI数字人选择器
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CONTACT_AI_SELECTOR_PAGE, FunAIContactSelectorActivity.class);
    // 通用版添加好友页面
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_ADD_FRIEND_PAGE, FunAddFriendActivity.class);
    // 通用版用户信息页面，UI风格更活泼
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_USER_INFO_PAGE, FunUserInfoActivity.class);
    // 通用版群组搜索页面
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_SEARCH_TEAM_PAGE, FunTeamSearchActivity.class);
    // 通用版我的群组列表
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_MY_TEAM_PAGE, FunTeamListActivity.class);
    // 通用版黑名单管理
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_MY_BLACK_PAGE, FunBlackListActivity.class);
    // 通用版AI用户列表
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_MY_AI_USER_PAGE, FunAIUserListActivity.class);
    // 通用版通知消息中心，处理社交请求
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_MY_NOTIFICATION_PAGE, FunContactVerifyActivity.class);
    // 通用版好友验证消息中心
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_FRIEND_NOTIFICATION_PAGE, FunFriendVerifyActivity.class);
    // 通用版联系人列表
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_CONTACT_PAGE, FunContactActivity.class);
    // 通用版全局搜索
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_GLOBAL_SEARCH_PAGE, FunSearchActivity.class);
    // 通用版群组详情页
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_TEAM_PROFILE_PAGE, FunTeamProfileActivity.class);
    // 通用版消息转发选择器
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_FORWARD_SELECTOR_PAGE, FunForwardSelectorActivity.class);
    // 通用版机器人列表
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_MY_ROBOT_PAGE, FunRobotListActivity.class);
    // 通用版机器人信息页
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_MY_ROBOT_INFO_PAGE, FunRobotInfoActivity.class);
    // 通用版机器人创建页
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_MY_ROBOT_CREATE_PAGE, FunRobotCreateActivity.class);
    // 通用版机器人绑定页
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_MY_ROBOT_BIND_PAGE, FunRobotBindActivity.class);
    return this;
  }
}
