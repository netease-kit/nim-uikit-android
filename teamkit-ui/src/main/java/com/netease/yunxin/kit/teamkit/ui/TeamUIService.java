// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui;

import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_FIELDS;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_ICON;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_NAME;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_CREATE_ADVANCED_TEAM_ACTION;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_CREATE_NORMAL_TEAM_ACTION;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_FUN_CREATE_ADVANCED_TEAM_ACTION;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_FUN_CREATE_NORMAL_TEAM_ACTION;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_FUN_TEAM_SETTING_PAGE;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_TEAM_INVITE_ACTION;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_TEAM_SETTING_PAGE;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Context;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamAgreeMode;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamInviteMode;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamJoinMode;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamUpdateExtensionMode;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamUpdateInfoMode;
import com.netease.nimlib.sdk.v2.team.params.V2NIMCreateTeamParams;
import com.netease.nimlib.sdk.v2.team.result.V2NIMCreateTeamResult;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ChatService;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.corekit.im2.IMKitConstant;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.corekit.model.ResultObserver;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.corekit.startup.Initializer;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.FunTeamSettingActivity;
import com.netease.yunxin.kit.teamkit.ui.normal.activity.TeamSettingActivity;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamIconUtils;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/** 群组件服务 用于注册群组件相关路由和页面，其他组件可通过路由进行页面跳转和功能调用 */
@Keep
public class TeamUIService extends ChatService {

  private static final String TAG = "TeamUIService";

  @NonNull
  @Override
  public String getServiceName() {
    return "TeamUIKit";
  }

  @NonNull
  @Override
  public String getVersionName() {
    return BuildConfig.versionName;
  }

  @Nullable
  @Override
  public Object onMethodCall(@NonNull String method, @Nullable Map<String, ?> param) {
    return null;
  }

  @NonNull
  @Override
  public ChatService create(@NonNull Context context) {

    // 注册邀请用户到群聊路由
    XKitRouter.registerRouter(
        PATH_TEAM_INVITE_ACTION,
        new XKitRouter.RouterValue(
            PATH_TEAM_INVITE_ACTION, (value, params, observer) -> inviteUser(params)));

    // 标准版本注册 ========================================
    // 注册群聊设置页面路由
    XKitRouter.registerRouter(PATH_TEAM_SETTING_PAGE, TeamSettingActivity.class);
    // 创建讨论组注册到路由器，可通过路由触发
    registerCreateNormalTeamRouter(
        context, PATH_CREATE_NORMAL_TEAM_ACTION, () -> TeamIconUtils.getDefaultRandomIconUrl(true));
    // 创建高级群注册到路由器，可通过路由触发
    registerCreateAdvanceTeamRouter(
        context,
        PATH_CREATE_ADVANCED_TEAM_ACTION,
        () -> TeamIconUtils.getDefaultRandomIconUrl(true));

    // 娱乐版本注册 ========================================
    // 注册群聊设置页面路由
    XKitRouter.registerRouter(PATH_FUN_TEAM_SETTING_PAGE, FunTeamSettingActivity.class);
    // 创建讨论组注册到路由器，可通过路由触发
    registerCreateAdvanceTeamRouter(
        context,
        PATH_FUN_CREATE_ADVANCED_TEAM_ACTION,
        () -> TeamIconUtils.getDefaultRandomIconUrl(false));
    // 创建高级群注册到路由器，可通过路由触发
    registerCreateNormalTeamRouter(
        context,
        PATH_FUN_CREATE_NORMAL_TEAM_ACTION,
        () -> TeamIconUtils.getDefaultRandomIconUrl(false));
    return this;
  }

  @NonNull
  @Override
  public List<Class<? extends Initializer<?>>> dependencies() {
    return Collections.emptyList();
  }

  // 将创建讨论组注册到路由器，可通过路由触发
  public void registerCreateNormalTeamRouter(
      Context context, String path, RandomUrlProvider provider) {
    XKitRouter.registerRouter(
        path,
        new XKitRouter.RouterValue(
            path,
            (value, params, observer) -> {
              createTeam(params, context, provider, observer, true);
              return true;
            }));
  }

  // 将创建高级群注册到路由器，可通过路由触发
  public void registerCreateAdvanceTeamRouter(
      Context context, String path, RandomUrlProvider provider) {

    XKitRouter.registerRouter(
        path,
        new XKitRouter.RouterValue(
            path,
            (value, params, observer) -> {
              createTeam(params, context, provider, observer, false);
              return true;
            }));
  }
  // 邀请用户到群聊
  public boolean inviteUser(Map<String, Object> customParam) {
    List<String> accIdList =
        customParam.get(REQUEST_CONTACT_SELECTOR_KEY) != null
            ? (List<String>) customParam.get(REQUEST_CONTACT_SELECTOR_KEY)
            : new ArrayList<>();
    Object teamIdObject = customParam.get(RouterConstant.KEY_TEAM_ID);
    if (teamIdObject != null && accIdList != null && accIdList.size() > 0) {
      String teamId = teamIdObject.toString();
      TeamRepo.inviteUser(teamId, accIdList, null);
      return true;
    }

    return false;
  }

  // 创建群组
  private void createTeam(
      Map<String, Object> params,
      Context context,
      RandomUrlProvider provider,
      ResultObserver<Object> observer,
      boolean isGroup) {
    try {
      Map<String, Object> customParam = params;
      if (TeamKitClient.getTeamCustom() != null) {
        customParam = TeamKitClient.getTeamCustom().customCreateTeam(params);
      }
      List<String> nameList =
          customParam.get(KEY_REQUEST_SELECTOR_NAME) != null
              ? (List<String>) customParam.get(KEY_REQUEST_SELECTOR_NAME)
              : new ArrayList<>();
      List<String> accIdList =
          customParam.get(REQUEST_CONTACT_SELECTOR_KEY) != null
              ? (List<String>) customParam.get(REQUEST_CONTACT_SELECTOR_KEY)
              : new ArrayList<>();
      String teamName =
          customParam.get(KEY_TEAM_NAME) != null
              ? String.valueOf(customParam.get(KEY_TEAM_NAME))
              : null;
      if (teamName == null) {
        teamName =
            isGroup
                ? TeamUtils.generateNameFromAccIdList(
                    nameList, context.getString(R.string.group_team))
                : TeamUtils.generateNameFromAccIdList(
                    nameList, context.getString(R.string.advanced_team));
      }

      String iconUrl =
          customParam.get(KEY_TEAM_ICON) != null
              ? String.valueOf(customParam.get(KEY_TEAM_ICON))
              : provider.getRandomUrl();

      JSONObject extJson =
          customParam.get(KEY_TEAM_FIELDS) != null
              ? (JSONObject) customParam.get(KEY_TEAM_FIELDS)
              : new JSONObject();
      if (extJson == null) {
        extJson = new JSONObject();
      }
      if (isGroup) {
        extJson.put(IMKitConstant.TEAM_GROUP_TAG, true);
      }

      if (accIdList == null) {
        accIdList = new ArrayList<>();
      }

      V2NIMCreateTeamParams teamParams = new V2NIMCreateTeamParams();
      teamParams.setTeamType(V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL);
      if (isGroup) {
        teamParams.setInviteMode(V2NIMTeamInviteMode.V2NIM_TEAM_INVITE_MODE_ALL);
        teamParams.setUpdateInfoMode(V2NIMTeamUpdateInfoMode.V2NIM_TEAM_UPDATE_INFO_MODE_ALL);
      } else {
        teamParams.setInviteMode(V2NIMTeamInviteMode.V2NIM_TEAM_INVITE_MODE_MANAGER);
        teamParams.setUpdateInfoMode(V2NIMTeamUpdateInfoMode.V2NIM_TEAM_UPDATE_INFO_MODE_MANAGER);
      }
      teamParams.setAgreeMode(V2NIMTeamAgreeMode.V2NIM_TEAM_AGREE_MODE_NO_AUTH);
      teamParams.setUpdateExtensionMode(
          V2NIMTeamUpdateExtensionMode.V2NIM_TEAM_UPDATE_EXTENSION_MODE_ALL);
      teamParams.setJoinMode(V2NIMTeamJoinMode.V2NIM_TEAM_JOIN_MODE_FREE);
      teamParams.setServerExtension(extJson.toString());

      teamParams.setName(teamName);
      teamParams.setAvatar(iconUrl);

      TeamRepo.createTeam(
          teamParams,
          accIdList,
          null,
          null,
          new FetchCallback<>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              if (observer != null) {
                observer.onResult(new ResultInfo<>(null, false, new ErrorMsg(errorCode)));
              }
              ALog.e(TeamKitClient.LIB_TAG, TAG, "create team onFailed:" + errorCode);
            }

            @Override
            public void onSuccess(@Nullable V2NIMCreateTeamResult data) {
              if (observer != null) {
                observer.onResult(new ResultInfo<>(data, true));
              }
              ALog.e(TeamKitClient.LIB_TAG, TAG, "createNormalTeam onSuccess");
            }
          });
    } catch (Exception exception) {
      ALog.e(TeamKitClient.LIB_TAG, TAG, "createNormalTeam exception:" + exception.getMessage());
    }
  }

  interface RandomUrlProvider {
    String getRandomUrl();
  }
}
