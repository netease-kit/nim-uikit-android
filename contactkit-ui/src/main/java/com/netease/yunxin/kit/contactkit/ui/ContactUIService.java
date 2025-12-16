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
import com.netease.yunxin.kit.contactkit.ui.normal.addfriend.AddFriendActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.ai.AIUserListActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.blacklist.BlackListActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.ContactActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.search.GlobalSearchActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.selector.ContactSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.selector.ai.AIContactSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.selector.forward.ForwardSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.team.TeamListActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.team.TeamProfileActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.team.TeamSearchActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.userinfo.UserInfoActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.verify.ContactVerifyActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.verify.FriendVerifyActivity;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
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

    /** 协同版皮肤路由注册 注册协同办公场景下的各类联系人与群组相关页面路由 */
    // 基础人员选择器页面，用于从联系人列表中选择成员
    XKitRouter.registerRouter(
        RouterConstant.PATH_CONTACT_SELECTOR_PAGE, ContactSelectorActivity.class);
    // AI数字人专用的人员选择器，支持选择AI角色
    XKitRouter.registerRouter(
        RouterConstant.PATH_CONTACT_AI_SELECTOR_PAGE, AIContactSelectorActivity.class);
    // 添加新好友功能页面，支持搜索和发送好友请求
    XKitRouter.registerRouter(RouterConstant.PATH_ADD_FRIEND_PAGE, AddFriendActivity.class);
    // 群组搜索功能页面，支持按关键词搜索公开群组
    XKitRouter.registerRouter(RouterConstant.PATH_SEARCH_TEAM_PAGE, TeamSearchActivity.class);
    // 用户个人信息展示与编辑页面
    XKitRouter.registerRouter(RouterConstant.PATH_USER_INFO_PAGE, UserInfoActivity.class);
    // 群组详情页面，展示群信息、成员列表和群设置
    XKitRouter.registerRouter(RouterConstant.PATH_TEAM_PROFILE_PAGE, TeamProfileActivity.class);
    // 我的群组列表页面，展示用户加入的所有群组
    XKitRouter.registerRouter(RouterConstant.PATH_MY_TEAM_PAGE, TeamListActivity.class);
    // 黑名单管理页面，管理被屏蔽的用户
    XKitRouter.registerRouter(RouterConstant.PATH_MY_BLACK_PAGE, BlackListActivity.class);
    // AI用户列表页面，展示和管理已添加的AI数字人
    XKitRouter.registerRouter(RouterConstant.PATH_MY_AI_USER_PAGE, AIUserListActivity.class);
    // 联系人验证请求页面，处理好友申请和入群申请
    XKitRouter.registerRouter(
        RouterConstant.PATH_MY_NOTIFICATION_PAGE, ContactVerifyActivity.class);
    // 好友验证请求页面，处理好友申请
    XKitRouter.registerRouter(
        RouterConstant.PATH_FRIEND_NOTIFICATION_PAGE, FriendVerifyActivity.class);
    // 主联系人列表页面，展示所有联系人分组和在线状态
    XKitRouter.registerRouter(RouterConstant.PATH_CONTACT_PAGE, ContactActivity.class);
    // 全局搜索页面，支持跨模块搜索用户、群组等信息
    XKitRouter.registerRouter(RouterConstant.PATH_GLOBAL_SEARCH_PAGE, GlobalSearchActivity.class);
    // 消息转发选择器，用于选择消息转发对象
    XKitRouter.registerRouter(
        RouterConstant.PATH_FORWARD_SELECTOR_PAGE, ForwardSelectorActivity.class);

    /** 通用版路由注册 */
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
    return this;
  }
}
