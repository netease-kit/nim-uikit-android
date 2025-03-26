// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ChatService;
import com.netease.yunxin.kit.conversationkit.local.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.local.ui.fun.FunCreateTeamFactory;
import com.netease.yunxin.kit.conversationkit.local.ui.fun.page.FunLocalConversationActivity;
import com.netease.yunxin.kit.conversationkit.local.ui.normal.NormalCreateTeamFactory;
import com.netease.yunxin.kit.conversationkit.local.ui.normal.page.LocalConversationActivity;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 会话模块UI服务，跟随应用一起启动 用于注册会话模块UI相关的路由 */
public class LocalConversationUIService extends ChatService {

  public final String TAG = "ConversationUIService";

  @NonNull
  @Override
  public String getServiceName() {
    return "LocalConversationUIKit";
  }

  @NonNull
  @Override
  public String getVersionName() {
    return BuildConfig.versionName;
  }

  @NonNull
  @Override
  public ChatService create(@NonNull Context context) {
    // 注册包括普通版和娱乐版，如果集成中只需要其中一套UI风格，则另外一套可以不注册

    // 普通版注册，包括会话列表页面，创建讨论组，高级群
    // 注册会话列表Activity页面
    XKitRouter.registerRouter(
        RouterConstant.PATH_CONVERSATION_PAGE, LocalConversationActivity.class);
    // 注册创建讨论组方法，包括人员选择，创建讨论组，跳转到聊天页面
    XKitRouter.registerRouter(
        RouterConstant.PATH_SELECT_CREATE_TEAM_PAGE,
        new XKitRouter.RouterValue(
            RouterConstant.PATH_SELECT_CREATE_TEAM_PAGE,
            (value, params, observer) -> {
              startSelectAndCreateTeam(
                  context, params, RouterConstant.PATH_CREATE_NORMAL_TEAM_ACTION);
              return true;
            }));
    // 注册创建高级群方法，包括人员选择，创建讨论组，跳转到聊天页面
    XKitRouter.registerRouter(
        RouterConstant.PATH_SELECT_CREATE_ADVANCED_TEAM_PAGE,
        new XKitRouter.RouterValue(
            RouterConstant.PATH_SELECT_CREATE_ADVANCED_TEAM_PAGE,
            (value, params, observer) -> {
              startSelectAndCreateTeam(
                  context, params, RouterConstant.PATH_CREATE_ADVANCED_TEAM_ACTION);
              return true;
            }));

    // 娱乐版相关注册，包括会话列表页面，创建讨论组，高级群
    // 注册会话列表Activity页面
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CONVERSATION_PAGE, FunLocalConversationActivity.class);
    // 注册创建讨论组方法，包括人员选择，创建讨论组，跳转到聊天页面
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_SELECT_CREATE_TEAM_PAGE,
        new XKitRouter.RouterValue(
            RouterConstant.PATH_FUN_SELECT_CREATE_TEAM_PAGE,
            (value, params, observer) -> {
              startFunSelectAndCreateTeam(
                  context, params, RouterConstant.PATH_FUN_CREATE_NORMAL_TEAM_ACTION);
              return true;
            }));
    // 注册创建高级群方法，包括人员选择，创建讨论组，跳转到聊天页面
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_SELECT_CREATE_ADVANCED_TEAM_PAGE,
        new XKitRouter.RouterValue(
            RouterConstant.PATH_FUN_SELECT_CREATE_ADVANCED_TEAM_PAGE,
            (value, params, observer) -> {
              startFunSelectAndCreateTeam(
                  context, params, RouterConstant.PATH_FUN_CREATE_ADVANCED_TEAM_ACTION);
              return true;
            }));
    return this;
  }

  /**
   * 创建普通版讨论组或者高级群
   *
   * @param context 上下文
   * @param params 群相关设置参数
   * @param createAction 区分创建讨论组:RouterConstant.PATH_CREATE_NORMAL_TEAM_ACTION,
   *     还是高级群:RouterConstant.PATH_CREATE_ADVANCED_TEAM_ACTION
   */
  private void startSelectAndCreateTeam(
      Context context, Map<String, Object> params, String createAction) {
    int memberLimit = ConversationConstant.MAX_TEAM_MEMBER;
    List<String> accIdList = new ArrayList<>();
    List<String> memberList = null;
    try {
      memberLimit = (int) params.get(RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT);
      List<String> filterList =
          params.get(RouterConstant.SELECTOR_CONTACT_FILTER_KEY) != null
              ? (List<String>) params.get(RouterConstant.SELECTOR_CONTACT_FILTER_KEY)
              : new ArrayList<>();
      memberList =
          params.get(RouterConstant.REQUEST_CONTACT_SELECTOR_KEY) != null
              ? (List<String>) params.get(RouterConstant.REQUEST_CONTACT_SELECTOR_KEY)
              : new ArrayList<>();
      if (filterList != null && filterList.size() > 0) {
        accIdList.addAll(filterList);
      }
    } catch (Exception e) {
      ALog.e(TAG, "conversation create team error:" + e.getMessage());
    }
    NormalCreateTeamFactory.selectAndCreateTeam(
        context, 100, createAction, accIdList, memberList, memberLimit);
  }

  /**
   * 创建娱乐版讨论组或者高级群
   *
   * @param context 上下文
   * @param params 群相关设置参数
   * @param createAction 区分创建讨论组RouterConstant.PATH_FUN_CREATE_NORMAL_TEAM_ACTION,
   *     还是高级群:RouterConstant.PATH_FUN_CREATE_ADVANCED_TEAM_ACTION
   */
  private void startFunSelectAndCreateTeam(
      Context context, Map<String, Object> params, String createAction) {
    int memberLimit = ConversationConstant.MAX_TEAM_MEMBER;
    List<String> accIdList = new ArrayList<>();
    List<String> memberList = new ArrayList<>();

    try {
      memberLimit = (int) params.get(RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT);
      List<String> filterList =
          params.get(RouterConstant.SELECTOR_CONTACT_FILTER_KEY) != null
              ? (List<String>) params.get(RouterConstant.SELECTOR_CONTACT_FILTER_KEY)
              : new ArrayList<>();
      memberList =
          params.get(RouterConstant.REQUEST_CONTACT_SELECTOR_KEY) != null
              ? (List<String>) params.get(RouterConstant.REQUEST_CONTACT_SELECTOR_KEY)
              : new ArrayList<>();
      if (filterList != null && filterList.size() > 0) {
        accIdList.addAll(filterList);
      }
    } catch (Exception e) {
      ALog.e(TAG, "conversation create team error:" + e.getMessage());
    }
    FunCreateTeamFactory.selectAndCreateTeam(
        context, 100, createAction, accIdList, memberList, memberLimit);
  }
}
