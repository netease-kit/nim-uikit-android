// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_MESSAGE_CONTENT;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_REMOTE_EXTENSION;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_SESSION_ID;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_SESSION_TYPE;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_CHAT_AIT_NOTIFY_ACTION;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_CHAT_SEND_TEAM_TIP_ACTION;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_CHAT_SEND_TEXT_ACTION;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageCreator;
import com.netease.nimlib.sdk.v2.message.result.V2NIMSendMessageResult;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ChatService;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatForwardActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatP2PActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatPinActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatReaderActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatSearchActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatSettingActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatTeamActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunCollectionActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatForwardActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatP2PActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatPinActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatReaderActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatSearchActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatSettingActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatTeamActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.CollectionActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.CollectionDetailActivity;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitService;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.EmojiManager;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.extend.ProgressFetchCallback;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.Map;

/** Chat模块UI服务。在应用启动之后会调用{@link #create(Context)}方法。 当前用于对外能力接口的注册 */
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

    // normal
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_P2P_PAGE, ChatP2PActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_TEAM_PAGE, ChatTeamActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_SEARCH_PAGE, ChatSearchActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_PIN_PAGE, ChatPinActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_SETTING_PAGE, ChatSettingActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_ACK_PAGE, ChatReaderActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_FORWARD_PAGE, ChatForwardActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_COLLECTION_PAGE, CollectionActivity.class);
    XKitRouter.registerRouter(
        RouterConstant.PATH_COLLECTION_DETAIL_PAGE, CollectionDetailActivity.class);

    // fun
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_CHAT_P2P_PAGE, FunChatP2PActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_CHAT_TEAM_PAGE, FunChatTeamActivity.class);
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CHAT_SEARCH_PAGE, FunChatSearchActivity.class);

    XKitRouter.registerRouter(RouterConstant.PATH_FUN_CHAT_PIN_PAGE, FunChatPinActivity.class);
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CHAT_READER_PAGE, FunChatReaderActivity.class);
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CHAT_SETTING_PAGE, FunChatSettingActivity.class);
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CHAT_FORWARD_PAGE, FunChatForwardActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_COLLECTION_PAGE, FunCollectionActivity.class);

    // ===通用逻辑初始化===
    // 注册自定义消息类型
    ChatKitClient.addCustomAttach(
        ChatMessageType.MULTI_FORWARD_ATTACHMENT, MultiForwardAttachment.class);
    ChatKitClient.addCustomAttach(ChatMessageType.RICH_TEXT_ATTACHMENT, RichTextAttachment.class);

    chatKitInit(context);
    registerSendTeamTips();
    registerAitNotifyTrigger();
    registerSendText();
    IMKitClient.addInitService(new ChatUIInitService());
    return this;
  }

  public void chatKitInit(Context context) {
    EmojiManager.init(context);
  }

  // 将发送创建群成功Tips注册到路由器，可通过路由触发
  private void registerSendTeamTips() {
    XKitRouter.registerRouter(
        PATH_CHAT_SEND_TEAM_TIP_ACTION,
        new XKitRouter.RouterValue(
            PATH_CHAT_SEND_TEAM_TIP_ACTION,
            (value, params, observer) -> {
              String sessionId = params.get(KEY_SESSION_ID).toString();
              long messageTime = System.currentTimeMillis() - timeGap;
              if (params.containsKey(RouterConstant.KEY_MESSAGE_TIME)) {
                messageTime = (long) params.get(RouterConstant.KEY_MESSAGE_TIME);
              }
              Map<String, Object> extension = null;
              if (params.containsKey(KEY_REMOTE_EXTENSION)) {
                extension = (Map<String, Object>) params.get(KEY_REMOTE_EXTENSION);
              }
              ChatRepo.sendTeamTipWithoutUnreadExt(
                  sessionId,
                  extension,
                  false,
                  messageTime,
                  new FetchCallback<>() {
                    @Override
                    public void onError(int errorCode, @Nullable String errorMsg) {
                      if (observer != null) {
                        observer.onResult(
                            new ResultInfo<>(null, false, new ErrorMsg(errorCode, errorMsg)));
                      }
                      ALog.e(
                          ChatKitUIConstant.LIB_TAG,
                          TAG,
                          "sendTeamTipWithoutUnreadExt onError:" + errorCode);
                    }

                    @Override
                    public void onSuccess(@Nullable V2NIMMessage data) {
                      if (observer != null) {
                        observer.onResult(new ResultInfo<>(null, true));
                      }
                      ALog.d(
                          ChatKitUIConstant.LIB_TAG, TAG, "sendTeamTipWithoutUnreadExt onSuccess");
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
              V2NIMConversationType sessionType =
                  V2NIMConversationType.typeOfValue((int) params.get(KEY_SESSION_TYPE));
              V2NIMMessage textMessage = V2NIMMessageCreator.createTextMessage(content);
              String conversationId =
                  V2NIMConversationIdUtil.conversationId(sessionId, sessionType);
              ChatRepo.sendMessage(
                  textMessage,
                  conversationId,
                  null,
                  new ProgressFetchCallback<>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                      if (observer != null) {
                        observer.onResult(
                            new ResultInfo<>(null, false, new ErrorMsg(errorCode, errorMsg)));
                      }
                      ALog.e(
                          ChatKitUIConstant.LIB_TAG,
                          TAG,
                          "sendTeamTipWithoutUnreadExt onError:" + errorCode);
                    }

                    @Override
                    public void onSuccess(@Nullable V2NIMSendMessageResult data) {
                      if (observer != null) {
                        observer.onResult(new ResultInfo<>(null, true));
                      }
                      ALog.d(
                          ChatKitUIConstant.LIB_TAG, TAG, "sendTeamTipWithoutUnreadExt onSuccess");
                    }

                    @Override
                    public void onProgress(int progress) {
                      //do nothing
                    }
                  });
              return true;
            }));
  }
}
