// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_MESSAGE_CONTENT;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_REMOTE_EXTENSION;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_SESSION_ID;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_SESSION_TYPE;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.PATH_CHAT_AIT_NOTIFY_ACTION;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.PATH_CHAT_SEND_TEAM_TIP_ACTION;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.PATH_CHAT_SEND_TEXT_ACTION;

import android.content.Context;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.coexist.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessageCreator;
import com.netease.nimlib.coexist.sdk.v2.message.result.V2NIMSendMessageResult;
import com.netease.nimlib.coexist.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ChatCustomMsgFactory;
import com.netease.yunxin.kit.chatkit.emoji.ChatEmojiManager;
import com.netease.yunxin.kit.chatkit.map.IMessageMapProvider;
import com.netease.yunxin.kit.chatkit.model.CustomAttachment;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitService;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.EmojiManager;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.CommonBaseMessageViewHolder;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.ProgressFetchCallback;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.Map;
import org.json.JSONObject;

/** Chat模块定制能力入口类。 */
public class ChatKitClient {

  private static final String TAG = "ChatKitClient";
  private static final Long timeGap = 2000L;
  private static ChatUIConfig chatConfig;
  private static IMessageMapProvider messageMapProvider;
  private static IPictureChooseEngine pictureChooseEngine;
  private static Boolean voicePlayEarphoneMode;

  public static void init(Context context) {
    EmojiManager.init(context);
    ChatEmojiManager.INSTANCE.init(context, R.xml.chat_emoji);
    registerSendTeamTips();
    registerAtMessageNotifyTrigger();
    registerSendText();
  }

  /**
   * 是否开启听筒模式播放语音
   *
   * @return true 听筒模式播放语音 false 扬声器模式播放语音
   */
  public static boolean isEarphoneMode() {
    if (voicePlayEarphoneMode == null) {
      voicePlayEarphoneMode = SettingRepo.getHandsetMode();
    }
    return voicePlayEarphoneMode;
  }

  /**
   * 设置听筒模式播放语音
   *
   * @param earphoneMode true 听筒模式播放语音 false 扬声器模式播放语音
   */
  public static void setEarphoneMode(boolean earphoneMode) {
    voicePlayEarphoneMode = earphoneMode;
    SettingRepo.setHandsetMode(earphoneMode);
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "setEarphoneMode:" + earphoneMode);
  }

  public static void setChatUIConfig(ChatUIConfig config) {
    chatConfig = config;
  }

  public static void setMessageMapProvider(IMessageMapProvider provider) {
    messageMapProvider = provider;
  }

  public static ChatUIConfig getChatUIConfig() {
    return chatConfig;
  }

  public static void setPictureChooseEngine(IPictureChooseEngine engine) {
    pictureChooseEngine = engine;
  }

  public static @Nullable IPictureChooseEngine getPictureChooseEngine() {
    return pictureChooseEngine;
  }

  public static @Nullable IMessageMapProvider getMessageMapProvider() {
    return messageMapProvider;
  }

  public static void addCustomAttach(int type, Class<? extends CustomAttachment> attachmentClass) {
    ChatCustomMsgFactory.addCustomAttach(type, attachmentClass);
  }

  public static void removeCustomAttach(int type) {
    ChatCustomMsgFactory.removeCustomAttach(type);
  }

  public static void addCustomViewHolder(
      int type, Class<? extends ChatBaseMessageViewHolder> attachmentClass) {
    ChatViewHolderDefaultFactory.getInstance().addCustomViewHolder(type, attachmentClass);
  }

  public static void removeCustomViewHolder(int type) {
    ChatViewHolderDefaultFactory.getInstance().removeCustomViewHolder(type);
  }

  public static void addCommonCustomViewHolder(
      int type,
      Class<? extends CommonBaseMessageViewHolder> holderClass,
      @LayoutRes int layoutRes) {
    ChatViewHolderDefaultFactory.getInstance()
        .addCommonCustomViewHolder(type, holderClass, layoutRes);
  }

  public static void removeCommonCustomViewHolder(int type) {
    ChatViewHolderDefaultFactory.getInstance().removeCommonCustomViewHolder(type);
  }

  // 将发送创建群成功Tips注册到路由器，可通过路由触发
  private static void registerSendTeamTips() {
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
              //构建消息，并本地保存
              V2NIMMessage tipMessage = V2NIMMessageCreator.createTipsMessage(null);
              if (extension != null) {
                try {
                  JSONObject json = new JSONObject(extension);
                  tipMessage.setServerExtension(json.toString());
                } catch (Exception e) {
                  ALog.e(
                      ChatKitUIConstant.LIB_TAG,
                      TAG,
                      "sendTeamTipWithoutUnreadExt onError:" + e.getMessage());
                }
              }
              String conversationId =
                  V2NIMConversationIdUtil.conversationId(
                      sessionId, V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM);
              ChatRepo.insertMessageToLocal(
                  tipMessage,
                  conversationId,
                  IMKitClient.account(),
                  messageTime,
                  new FetchCallback<V2NIMMessage>() {

                    @Override
                    public void onSuccess(@Nullable V2NIMMessage data) {
                      if (observer != null) {
                        observer.onResult(new ResultInfo<>(null, true));
                      }
                      ALog.d(
                          ChatKitUIConstant.LIB_TAG, TAG, "sendTeamTipWithoutUnreadExt onSuccess");
                    }

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
                  });
              return true;
            }));
  }

  private static void registerAtMessageNotifyTrigger() {
    XKitRouter.registerRouter(
        PATH_CHAT_AIT_NOTIFY_ACTION,
        new XKitRouter.RouterValue(
            PATH_CHAT_AIT_NOTIFY_ACTION,
            (value, params, observer) -> {
              AitService.getInstance().sendLocalAitEvent();
              return true;
            }));
  }

  private static void registerSendText() {
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
                  new ProgressFetchCallback<V2NIMSendMessageResult>() {
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

  private static void loadVoicePlayEarphoneMode() {
    SettingRepo.getHandsetMode(
        new FetchCallback<Boolean>() {
          @Override
          public void onError(int errorCode, @org.jetbrains.annotations.Nullable String errorMsg) {
            ALog.e(
                ChatKitUIConstant.LIB_TAG, TAG, "loadVoicePlayEarphoneMode onError:" + errorCode);
          }

          @Override
          public void onSuccess(@org.jetbrains.annotations.Nullable Boolean data) {
            ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "loadVoicePlayEarphoneMode onSuccess:" + data);
            if (data != null) {
              voicePlayEarphoneMode = data;
            }
          }
        });
  }
}
