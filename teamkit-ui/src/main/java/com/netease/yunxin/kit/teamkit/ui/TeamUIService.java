// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_FIELDS;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_NAME;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CREATE_ADVANCED_TEAM_ACTION;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CREATE_NORMAL_TEAM_ACTION;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_TEAM_SETTING_PAGE;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Context;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.team.constant.TeamBeInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamExtensionUpdateModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamUpdateModeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.CreateTeamResult;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.XKitService;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.IMKitConstant;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.corekit.startup.Initializer;
import com.netease.yunxin.kit.teamkit.TeamService;
import com.netease.yunxin.kit.teamkit.repo.TeamRepo;
import com.netease.yunxin.kit.teamkit.ui.activity.TeamSettingActivity;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamIconUtils;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** launch service when app start the TeamUIService will be created it need to config in manifest */
@Keep
public class TeamUIService extends TeamService {

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

  @Override
  public XKitService create(@NonNull Context context) {
    XKitRouter.registerRouter(PATH_TEAM_SETTING_PAGE, TeamSettingActivity.class);
    registerCreateNormalTeamRouter(context);
    registerCreateAdvanceTeamRouter(context);
    return this;
  }

  @NonNull
  @Override
  public List<Class<? extends Initializer<?>>> dependencies() {
    return Collections.emptyList();
  }

  //将创建讨论组注册到路由器，可通过路由触发
  public void registerCreateNormalTeamRouter(Context context) {
    XKitRouter.registerRouter(
        PATH_CREATE_NORMAL_TEAM_ACTION,
        new XKitRouter.RouterValue(
            PATH_CREATE_NORMAL_TEAM_ACTION,
            (value, params, observer) -> {
              Map<String, Object> customParam = params;
              if (TeamKitClient.getTeamCustom() != null) {
                customParam = TeamKitClient.getTeamCustom().customCreateTeam(params);
              }
              try {

                List<String> nameList =
                    customParam.get(KEY_REQUEST_SELECTOR_NAME) != null
                        ? (List<String>) customParam.get(KEY_REQUEST_SELECTOR_NAME)
                        : new ArrayList<>();
                List<String> accIdList =
                    customParam.get(REQUEST_CONTACT_SELECTOR_KEY) != null
                        ? (List<String>) customParam.get(REQUEST_CONTACT_SELECTOR_KEY)
                        : new ArrayList<>();

                Map<TeamFieldEnum, Serializable> fieldMap =
                    customParam.get(KEY_TEAM_FIELDS) != null
                        ? (Map<TeamFieldEnum, Serializable>) customParam.get(KEY_TEAM_FIELDS)
                        : new HashMap<>();
                String teamName =
                    customParam.get(KEY_TEAM_NAME) != null
                        ? customParam.get(KEY_TEAM_NAME).toString()
                        : TeamUtils.generateNameFromAccIdList(
                            nameList, context.getString(R.string.group_team));

                fieldMap.put(TeamFieldEnum.BeInviteMode, TeamBeInviteModeEnum.NoAuth);
                fieldMap.put(TeamFieldEnum.InviteMode, TeamInviteModeEnum.All);
                fieldMap.put(TeamFieldEnum.VerifyType, VerifyTypeEnum.Free);
                fieldMap.put(TeamFieldEnum.TeamUpdateMode, TeamUpdateModeEnum.All);
                fieldMap.put(
                    TeamFieldEnum.TeamExtensionUpdateMode, TeamExtensionUpdateModeEnum.All);
                if (fieldMap.containsKey(TeamFieldEnum.Extension)) {
                  String extension =
                      fieldMap.get(TeamFieldEnum.Extension)
                          + IMKitConstant.TEAM_EXTENSION_SPLIT_TAG
                          + IMKitConstant.TEAM_GROUP_TAG;
                  fieldMap.put(TeamFieldEnum.Extension, extension);
                } else {
                  fieldMap.put(TeamFieldEnum.Extension, IMKitConstant.TEAM_GROUP_TAG);
                }

                TeamRepo.createAdvanceTeam(
                    teamName,
                    TeamIconUtils.getDefaultRandomIconUrl(),
                    accIdList,
                    fieldMap,
                    new FetchCallback<CreateTeamResult>() {
                      @Override
                      public void onSuccess(@Nullable CreateTeamResult param) {
                        if (observer != null) {
                          observer.onResult(new ResultInfo<>(param, true));
                        }
                        ALog.e(TeamKitClient.LIB_TAG, TAG, "createNormalTeam onSuccess");
                      }

                      @Override
                      public void onFailed(int code) {
                        if (observer != null) {
                          observer.onResult(new ResultInfo<>(null, false, new ErrorMsg(code)));
                        }
                        ALog.e(TeamKitClient.LIB_TAG, TAG, "createNormalTeam onFailed:" + code);
                      }

                      @Override
                      public void onException(@Nullable Throwable exception) {
                        if (observer != null) {
                          observer.onResult(
                              new ResultInfo<>(null, false, new ErrorMsg(-1, null, exception)));
                        }
                        ALog.e(
                            TeamKitClient.LIB_TAG,
                            TAG,
                            "createNormalTeam onException:" + exception.getMessage());
                      }
                    });
              } catch (Exception exception) {
                ALog.e(
                    TeamKitClient.LIB_TAG,
                    TAG,
                    "createNormalTeam exception:" + exception.getMessage());
              }

              return true;
            }));
  }

  //将创建高级群注册到路由器，可通过路由触发
  private void registerCreateAdvanceTeamRouter(Context context) {

    XKitRouter.registerRouter(
        PATH_CREATE_ADVANCED_TEAM_ACTION,
        new XKitRouter.RouterValue(
            PATH_CREATE_ADVANCED_TEAM_ACTION,
            (value, params, observer) -> {
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

              Map<TeamFieldEnum, Serializable> fieldMap =
                  customParam.get(KEY_TEAM_FIELDS) != null
                      ? (Map<TeamFieldEnum, Serializable>) customParam.get(KEY_TEAM_FIELDS)
                      : new HashMap<>();
              String teamName =
                  customParam.get(KEY_TEAM_NAME) != null
                      ? customParam.get(KEY_TEAM_NAME).toString()
                      : TeamUtils.generateNameFromAccIdList(
                          nameList, context.getString(R.string.advanced_team));

              TeamRepo.createAdvanceTeam(
                  teamName,
                  TeamIconUtils.getDefaultRandomIconUrl(),
                  accIdList,
                  fieldMap,
                  new FetchCallback<CreateTeamResult>() {
                    @Override
                    public void onSuccess(@Nullable CreateTeamResult param) {
                      if (observer != null) {
                        observer.onResult(new ResultInfo<>(param, true));
                      }
                      ALog.e(TeamKitClient.LIB_TAG, TAG, "createNormalTeam onSuccess");
                    }

                    @Override
                    public void onFailed(int code) {
                      if (observer != null) {
                        observer.onResult(new ResultInfo<>(null, false, new ErrorMsg(code)));
                      }
                      ALog.e(TeamKitClient.LIB_TAG, TAG, "createNormalTeam onFailed:" + code);
                    }

                    @Override
                    public void onException(@Nullable Throwable exception) {
                      if (observer != null) {
                        observer.onResult(
                            new ResultInfo<>(null, false, new ErrorMsg(-1, null, exception)));
                      }
                      ALog.e(
                          TeamKitClient.LIB_TAG,
                          TAG,
                          "createNormalTeam onException:" + exception.getMessage());
                    }
                  });

              return true;
            }));
  }
}
