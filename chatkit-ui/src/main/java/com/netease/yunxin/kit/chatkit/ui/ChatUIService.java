// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_MESSAGE_CONTENT;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REMOTE_EXTENSION;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_SESSION_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_SESSION_TYPE;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CHAT_AIT_NOTIFY_ACTION;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CHAT_SEND_TEAM_TIP_ACTION;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CHAT_SEND_TEXT_ACTION;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ChatService;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.page.ChatP2PActivity;
import com.netease.yunxin.kit.chatkit.ui.page.ChatPinActivity;
import com.netease.yunxin.kit.chatkit.ui.page.ChatSearchActivity;
import com.netease.yunxin.kit.chatkit.ui.page.ChatSettingActivity;
import com.netease.yunxin.kit.chatkit.ui.page.ChatTeamActivity;
import com.netease.yunxin.kit.chatkit.ui.page.LocationPageActivity;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitService;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.EmojiManager;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.Map;

/** launch service when app start the ChatUIService will be created it need to config in manifest */
public class ChatUIService extends ChatService {

  private Long timeGap = 2000L;
  private final String TAG = "ChatUIService";

  @NonNull
  @Override
  public String getServiceName() {
    return "ChatUIKit";
  }

  @NonNull
  @Override
  public String getVersionName() {
    return BuildConfig.versionName;
  }

  @NonNull
  @Override
  public ChatService create(@NonNull Context context) {
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_P2P_PAGE, ChatP2PActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_TEAM_PAGE, ChatTeamActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_SEARCH_PAGE, ChatSearchActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_LOCATION_PAGE, LocationPageActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_PIN_PAGE, ChatPinActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_SETTING_PAGE, ChatSettingActivity.class);
    chatKitInit(context);
    registerSendTeamTips();
    registerAitNotifyTrigger();
    registerSendText();
    IMKitClient.registerInitService(new ChatUIInitService());
    return this;
  }

  public void chatKitInit(Context context) {
    EmojiManager.init(context);
  }

  //将发送创建群成功Tips注册到路由器，可通过路由触发
  private void registerSendTeamTips() {
    XKitRouter.registerRouter(
        PATH_CHAT_SEND_TEAM_TIP_ACTION,
        new XKitRouter.RouterValue(
            PATH_CHAT_SEND_TEAM_TIP_ACTION,
            (value, params, observer) -> {
              String sessionId = params.get(KEY_SESSION_ID).toString();
              Map<String, Object> extension = null;
              if (params.containsKey(KEY_REMOTE_EXTENSION)) {
                extension = (Map<String, Object>) params.get(KEY_REMOTE_EXTENSION);
              }
              ChatRepo.sendTeamTipWithoutUnreadExt(
                  sessionId,
                  extension,
                  false,
                  timeGap,
                  new FetchCallback<Void>() {
                    @Override
                    public void onSuccess(@Nullable Void param) {
                      if (observer != null) {
                        observer.onResult(new ResultInfo<>(null, true));
                      }
                      ALog.d(
                          ChatKitUIConstant.LIB_TAG, TAG, "sendTeamTipWithoutUnreadExt onSuccess");
                    }

                    @Override
                    public void onFailed(int code) {
                      if (observer != null) {
                        observer.onResult(new ResultInfo<>(null, false, new ErrorMsg(code)));
                      }
                      ALog.e(
                          ChatKitUIConstant.LIB_TAG,
                          TAG,
                          "sendTeamTipWithoutUnreadExt onFailed:" + code);
                    }

                    @Override
                    public void onException(@Nullable Throwable exception) {
                      if (observer != null) {
                        observer.onResult(
                            new ResultInfo<>(null, false, new ErrorMsg(-1, null, exception)));
                      }
                      ALog.e(
                          ChatKitUIConstant.LIB_TAG,
                          TAG,
                          "sendTeamTipWithoutUnreadExt onException:" + exception.getMessage());
                    }
                  });
              return true;
            }));
  }

  private void registerAitNotifyTrigger() {
    XKitRouter.registerRouter(
        PATH_CHAT_AIT_NOTIFY_ACTION,
        new XKitRouter.RouterValue(
            PATH_CHAT_AIT_NOTIFY_ACTION,
            (value, params, observer) -> {
              AitService.getInstance().sendLocalAitEvent();
              return true;
            }));
  }

  private void registerSendText() {
    XKitRouter.registerRouter(
        PATH_CHAT_SEND_TEXT_ACTION,
        new XKitRouter.RouterValue(
            PATH_CHAT_SEND_TEXT_ACTION,
            (value, params, observer) -> {
              String sessionId = params.get(KEY_SESSION_ID).toString();
              String content = params.get(KEY_MESSAGE_CONTENT).toString();
              SessionTypeEnum sessionType =
                  SessionTypeEnum.typeOfValue((int) params.get(KEY_SESSION_TYPE));
              ChatRepo.sendTextMessage(
                  sessionId,
                  sessionType,
                  content,
                  true,
                  new FetchCallback<Void>() {
                    @Override
                    public void onSuccess(@Nullable Void param) {
                      if (observer != null) {
                        observer.onResult(new ResultInfo<>(null, true));
                      }
                      ALog.d(
                          ChatKitUIConstant.LIB_TAG, TAG, "sendTeamTipWithoutUnreadExt onSuccess");
                    }

                    @Override
                    public void onFailed(int code) {
                      if (observer != null) {
                        observer.onResult(new ResultInfo<>(null, false, new ErrorMsg(code)));
                      }
                      ALog.e(
                          ChatKitUIConstant.LIB_TAG,
                          TAG,
                          "sendTeamTipWithoutUnreadExt onFailed:" + code);
                    }

                    @Override
                    public void onException(@Nullable Throwable exception) {
                      if (observer != null) {
                        observer.onResult(
                            new ResultInfo<>(null, false, new ErrorMsg(-1, null, exception)));
                      }
                      ALog.e(
                          ChatKitUIConstant.LIB_TAG,
                          TAG,
                          "sendTeamTipWithoutUnreadExt onException:" + exception.getMessage());
                    }
                  });
              return true;
            }));
  }
}
