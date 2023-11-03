// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ChatService;
import com.netease.yunxin.kit.conversationkit.ui.fun.FunCreateTeamFactory;
import com.netease.yunxin.kit.conversationkit.ui.fun.page.FunConversationActivity;
import com.netease.yunxin.kit.conversationkit.ui.normal.NormalCreateTeamFactory;
import com.netease.yunxin.kit.conversationkit.ui.normal.page.ConversationActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConversationUIService extends ChatService {

  public final String TAG = "ConversationUIService";

  @NonNull
  @Override
  public String getServiceName() {
    return "ConversationUIKit";
  }

  @NonNull
  @Override
  public String getVersionName() {
    return BuildConfig.versionName;
  }

  @NonNull
  @Override
  public ChatService create(@NonNull Context context) {
    // normal
    XKitRouter.registerRouter(RouterConstant.PATH_CONVERSATION_PAGE, ConversationActivity.class);
    // create group from select friends page
    XKitRouter.registerRouter(
        RouterConstant.PATH_SELECT_CREATE_TEAM_PAGE,
        new XKitRouter.RouterValue(
            RouterConstant.PATH_SELECT_CREATE_TEAM_PAGE,
            (value, params, observer) -> {
              startSelectAndCreateTeam(
                  context, params, RouterConstant.PATH_CREATE_NORMAL_TEAM_ACTION);
              return true;
            }));
    // create advance team from select friends page
    XKitRouter.registerRouter(
        RouterConstant.PATH_SELECT_CREATE_ADVANCED_TEAM_PAGE,
        new XKitRouter.RouterValue(
            RouterConstant.PATH_SELECT_CREATE_ADVANCED_TEAM_PAGE,
            (value, params, observer) -> {
              startSelectAndCreateTeam(
                  context, params, RouterConstant.PATH_CREATE_ADVANCED_TEAM_ACTION);
              return true;
            }));

    // fun
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CONVERSATION_PAGE, FunConversationActivity.class);
    // create group from select friends page
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_SELECT_CREATE_TEAM_PAGE,
        new XKitRouter.RouterValue(
            RouterConstant.PATH_FUN_SELECT_CREATE_TEAM_PAGE,
            (value, params, observer) -> {
              startFunSelectAndCreateTeam(
                  context, params, RouterConstant.PATH_FUN_CREATE_NORMAL_TEAM_ACTION);
              return true;
            }));
    // create advance team from select friends page
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

  private void startSelectAndCreateTeam(
      Context context, Map<String, Object> params, String createAction) {
    int memberLimit = ConversationUIConstant.MAX_TEAM_MEMBER;
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

  private void startFunSelectAndCreateTeam(
      Context context, Map<String, Object> params, String createAction) {
    int memberLimit = ConversationUIConstant.MAX_TEAM_MEMBER;
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
