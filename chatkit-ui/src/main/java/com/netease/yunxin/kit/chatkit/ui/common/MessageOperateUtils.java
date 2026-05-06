// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMAIModelCallMessage;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageCreator;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageAudioAttachment;
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessageAIConfig;
import com.netease.nimlib.sdk.v2.message.params.V2NIMSendMessageParams;
import com.netease.nimlib.sdk.v2.message.params.V2NIMVoiceToTextParams;
import com.netease.nimlib.sdk.v2.message.result.V2NIMSendMessageResult;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.listener.MessageUpdateType;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.media.ImageUtil;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.RecentForward;
import com.netease.yunxin.kit.chatkit.model.TranslationInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.ResourceRepo;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitBlock;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitBlock.AitSegment;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AtContactsModel;
import com.netease.yunxin.kit.chatkit.utils.SendMediaHelper;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.EncryptUtils;
import com.netease.yunxin.kit.common.utils.FileUtils;
import com.netease.yunxin.kit.common.utils.UriUtils;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.extend.ProgressFetchCallback;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageOperateUtils {

  private static final String TAG = "MessageOperateUtils";
  private static final String Orientation_Vertical = "90";

  public static void deleteMessages(
      List<ChatMessageBean> messageList,
      MutableLiveData<FetchResult<List<V2NIMMessageRefer>>> deleteMessageLiveData) {
    if (messageList == null || messageList.isEmpty()) {
      return;
    }
    if (messageList.size() < 2) {
      ChatRepo.deleteMessage(
          messageList.get(0).getMessageData().getMessage(),
          null,
          false,
          new FetchCallback<Void>() {
            @Override
            public void onError(int errorCode, String errorMsg) {
              FetchResult<List<V2NIMMessageRefer>> fetchResult =
                  new FetchResult<>(LoadStatus.Error);
              fetchResult.setError(-1, R.string.chat_message_delete_error);
              deleteMessageLiveData.setValue(fetchResult);
            }

            @Override
            public void onSuccess(Void data) {
              processDeleteSuccess(messageList, deleteMessageLiveData);
            }
          });
    } else {
      List<V2NIMMessage> deleteList = new ArrayList<>();
      boolean onlyDeleteLocal = true;
      for (ChatMessageBean messageBean : messageList) {
        deleteList.add(messageBean.getMessageData().getMessage());
        if (messageBean.getMessageData().getMessage().getMessageServerId() != null
            && !messageBean.getMessageData().getMessage().getMessageServerId().isEmpty()) {
          onlyDeleteLocal = false;
        }
      }
      ChatRepo.deleteMessages(
          deleteList,
          null,
          onlyDeleteLocal,
          new FetchCallback<Void>() {
            @Override
            public void onError(int errorCode, String errorMsg) {
              FetchResult<List<V2NIMMessageRefer>> fetchResult =
                  new FetchResult<>(LoadStatus.Error);
              fetchResult.setError(-1, R.string.chat_message_delete_error);
              deleteMessageLiveData.setValue(fetchResult);
            }

            @Override
            public void onSuccess(Void data) {
              processDeleteSuccess(messageList, deleteMessageLiveData);
            }
          });
    }
  }

  private static void processDeleteSuccess(
      List<ChatMessageBean> messageBean,
      MutableLiveData<FetchResult<List<V2NIMMessageRefer>>> deleteMessageLiveData) {
    List<V2NIMMessageRefer> deleteMessageList = new ArrayList<>();
    for (ChatMessageBean message : messageBean) {
      deleteMessageList.add(message.getMessageData().getMessage());
    }
    FetchResult<List<V2NIMMessageRefer>> result = new FetchResult<>(LoadStatus.Success);
    result.setData(deleteMessageList);
    result.setType(FetchResult.FetchType.Remove);
    result.setTypeIndex(-1);
    deleteMessageLiveData.setValue(result);
  }

  public static void voiceToText(
      ChatMessageBean messageBean,
      MutableLiveData<
              FetchResult<
                  Pair<
                      com.netease.yunxin.kit.chatkit.listener.MessageUpdateType,
                      List<ChatMessageBean>>>>
          updateMessageLiveData) {
    if (messageBean == null
        || messageBean.getMessageData() == null
        || messageBean.getMessageData().getMessage().getAttachment() == null) {
      return;
    }
    V2NIMMessageAudioAttachment audioAttachment =
        (V2NIMMessageAudioAttachment) messageBean.getMessageData().getMessage().getAttachment();
    V2NIMVoiceToTextParams.V2NIMVoiceToTextParamsBuilder paramsBuilder =
        V2NIMVoiceToTextParams.V2NIMVoiceToTextParamsBuilder.builder(audioAttachment.getDuration());
    String path = audioAttachment.getPath();
    if (audioAttachment.getUrl() != null && !audioAttachment.getUrl().isEmpty()) {
      paramsBuilder.withVoiceUrl(audioAttachment.getUrl());
    } else if (path != null && FileUtils.isFileExists(path)) {
      paramsBuilder.withVoicePath(path);
    } else {
      return;
    }
    paramsBuilder.withSceneName(audioAttachment.getSceneName());
    V2NIMVoiceToTextParams params = paramsBuilder.build();
    ChatRepo.voiceToText(
        params,
        new FetchCallback<String>() {
          @Override
          public void onSuccess(String data) {
            if (data != null && !data.isEmpty()) {
              FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> messageUpdateResult =
                  new FetchResult<>(LoadStatus.Success);
              List<ChatMessageBean> messageList = new ArrayList<>();
              messageBean.setVoiceToText(data);
              messageList.add(messageBean);
              messageUpdateResult.setData(new Pair<>(MessageUpdateType.VoiceToText, messageList));
              messageUpdateResult.setType(FetchResult.FetchType.Update);
              messageUpdateResult.setTypeIndex(-1);
              updateMessageLiveData.setValue(messageUpdateResult);
            } else {
              FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> messageUpdateResult =
                  new FetchResult<>(LoadStatus.Error);
              messageUpdateResult.setError(0, R.string.chat_voice_to_text_failed);
              messageUpdateResult.setData(null);
              updateMessageLiveData.setValue(messageUpdateResult);
            }
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> messageUpdateResult =
                new FetchResult<>(LoadStatus.Error);
            messageUpdateResult.setError(0, R.string.chat_voice_to_text_failed);
            messageUpdateResult.setData(null);
            updateMessageLiveData.setValue(messageUpdateResult);
          }
        });
  }

  /**
   * 翻译文本消息，并将翻译结果写入消息本地扩展字段，触发 UI 局部刷新。
   *
   * @param messageBean 需要翻译的消息
   * @param targetLanguage 目标语言代码（如 "zh"、"en"）
   * @param updateMessageLiveData 用于通知 UI 刷新的 LiveData
   */
  public static void translateMessage(
      ChatMessageBean messageBean,
      String targetLanguage,
      MutableLiveData<FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>>>
          updateMessageLiveData) {
    if (messageBean == null || messageBean.getMessageData() == null) {
      return;
    }
    V2NIMMessage message = messageBean.getMessageData().getMessage();
    if (message == null || TextUtils.isEmpty(message.getText())) {
      return;
    }

    // 缓存命中：已有翻译且目标语言相同，直接复用，不走网络
    TranslationInfo cached = messageBean.getTranslationInfo();
    if (cached != null && targetLanguage.equals(cached.getTargetLanguage())) {
      // 确保译文可见（用户可能之前隐藏过）
      messageBean.setTranslationVisible(true);
      // 直接通知 UI 刷新（translationInfo 已在 bean 上，只需触发局部刷新）
      List<ChatMessageBean> messageList = new ArrayList<>();
      messageList.add(messageBean);
      FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> result =
          new FetchResult<>(LoadStatus.Success);
      result.setData(new Pair<>(MessageUpdateType.Translation, messageList));
      result.setType(FetchResult.FetchType.Update);
      result.setTypeIndex(-1);
      updateMessageLiveData.setValue(result);
      return;
    }

    // @ 保留：切分文本，对每个非 @ 片段独立翻译，全部回调后拼装写入
    Pair<List<String>, List<Boolean>> splitResult =
        splitTextByAtMentions(message.getText(), message);
    List<String> parts = splitResult.first;
    List<Boolean> isAtFlags = splitResult.second;

    // 预填槽位：@ 段和空串直接保留原文，非 @ 非空串待翻译
    String[] translatedSlots = new String[parts.size()];
    int pendingCount = 0;
    for (int i = 0; i < parts.size(); i++) {
      if (isAtFlags.get(i) || TextUtils.isEmpty(parts.get(i))) {
        translatedSlots[i] = parts.get(i);
      } else {
        pendingCount++;
      }
    }

    // 如果全是 @ 段（无需翻译），直接拼接写入
    if (pendingCount == 0) {
      StringBuilder sb = new StringBuilder();
      for (String part : parts) {
        if (part != null) sb.append(part);
      }
      String restoredText = sb.toString();
      TranslationInfo translationInfo =
          new TranslationInfo(targetLanguage, restoredText, System.currentTimeMillis());
      ChatRepo.updateTranslationLocalExtension(
          message,
          translationInfo,
          new FetchCallback<V2NIMMessage>() {
            @Override
            public void onSuccess(V2NIMMessage updatedMsg) {
              messageBean.setTranslationVisible(true);
              messageBean.setTranslationInfo(translationInfo);
              List<ChatMessageBean> ml = new ArrayList<>();
              ml.add(messageBean);
              FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> r =
                  new FetchResult<>(LoadStatus.Success);
              r.setData(new Pair<>(MessageUpdateType.Translation, ml));
              r.setType(FetchResult.FetchType.Update);
              r.setTypeIndex(-1);
              updateMessageLiveData.setValue(r);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
              FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> er =
                  new FetchResult<>(LoadStatus.Error);
              er.setError(errorCode, R.string.chat_translate_failed);
              updateMessageLiveData.setValue(er);
            }
          });
      return;
    }

    // 并行翻译所有非 @ 片段，全部回调完成后拼装并写入
    AtomicInteger remaining = new AtomicInteger(pendingCount);
    AtomicBoolean hasError = new AtomicBoolean(false);

    for (int i = 0; i < parts.size(); i++) {
      if (isAtFlags.get(i) || TextUtils.isEmpty(parts.get(i))) continue;
      final int slotIndex = i;
      final String partText = parts.get(i);
      ChatRepo.INSTANCE.translateText(
          partText,
          targetLanguage,
          new FetchCallback<String>() {
            @Override
            public void onSuccess(String translatedText) {
              if (hasError.get()) return;
              // 填充槽位，为空时 fallback 原文
              translatedSlots[slotIndex] =
                  TextUtils.isEmpty(translatedText) ? partText : translatedText;
              // 全部片段完成后拼装写入
              if (remaining.decrementAndGet() == 0) {
                StringBuilder resultBuilder = new StringBuilder();
                for (String slot : translatedSlots) {
                  if (slot != null) resultBuilder.append(slot);
                }
                String restoredText = resultBuilder.toString();
                TranslationInfo translationInfo =
                    new TranslationInfo(targetLanguage, restoredText, System.currentTimeMillis());
                ChatRepo.updateTranslationLocalExtension(
                    message,
                    translationInfo,
                    new FetchCallback<V2NIMMessage>() {
                      @Override
                      public void onSuccess(V2NIMMessage updatedMsg) {
                        messageBean.setTranslationVisible(true);
                        messageBean.setTranslationInfo(translationInfo);
                        List<ChatMessageBean> messageList = new ArrayList<>();
                        messageList.add(messageBean);
                        FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> result =
                            new FetchResult<>(LoadStatus.Success);
                        result.setData(new Pair<>(MessageUpdateType.Translation, messageList));
                        result.setType(FetchResult.FetchType.Update);
                        result.setTypeIndex(-1);
                        updateMessageLiveData.setValue(result);
                      }

                      @Override
                      public void onError(int errorCode, String errorMsg) {
                        FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> errorResult =
                            new FetchResult<>(LoadStatus.Error);
                        errorResult.setError(errorCode, R.string.chat_translate_failed);
                        updateMessageLiveData.setValue(errorResult);
                      }
                    });
              }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
              if (hasError.compareAndSet(false, true)) {
                FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> errorResult =
                    new FetchResult<>(LoadStatus.Error);
                errorResult.setError(errorCode, R.string.chat_translate_failed);
                updateMessageLiveData.setValue(errorResult);
              }
            }
          });
    }
  }

  public static void sendMultiForwardMessage(
      String displayName,
      String inputMsg,
      String fromAccountId,
      List<String> conversationIds,
      List<ChatMessageBean> messages,
      boolean showRead) {
    if (conversationIds == null
        || conversationIds.isEmpty()
        || messages == null
        || messages.isEmpty()) {
      return;
    }
    List<IMMessageInfo> iMessageList = new ArrayList<>();
    for (ChatMessageBean message : messages) {
      iMessageList.add(message.getMessageData());
    }
    String msgInfo = MessageHelper.createMultiForwardMsg(iMessageList);
    try {
      File localFile = SendMediaHelper.createTextFile();
      ResourceRepo.writeLocalFileAndUploadNOS(
          localFile,
          msgInfo,
          new FetchCallback<String>() {
            @Override
            public void onError(int errorCode, String errorMsg) {}

            @Override
            public void onSuccess(String param) {
              if (param != null) {
                String fileMD5 = EncryptUtils.md5(localFile);
                MultiForwardAttachment attachment =
                    MessageHelper.createMultiTransmitAttachment(
                        displayName, fromAccountId, param, iMessageList);
                attachment.md5 = fileMD5;
                String msgText = MessageHelper.getMultiTransmitContent(iMessageList);
                List<RecentForward> recentForwards = new ArrayList<>();
                for (String conversationId : conversationIds) {
                  V2NIMMessage multiForwardMessage =
                      V2NIMMessageCreator.createCustomMessage(msgText, attachment.toJsonStr());
                  ChatRepo.sendMessage(
                      multiForwardMessage,
                      conversationId,
                      MessageParamBuildUtils.buildSendParams(
                          multiForwardMessage, conversationId, null, null, null, null, showRead),
                      new ProgressFetchCallback<V2NIMSendMessageResult>() {
                        @Override
                        public void onProgress(int progress) {}

                        @Override
                        public void onSuccess(V2NIMSendMessageResult data) {}

                        @Override
                        public void onError(int errorCode, String errorMsg) {}
                      });
                  String sessionId = V2NIMConversationIdUtil.conversationTargetId(conversationId);
                  V2NIMConversationType sessionType =
                      V2NIMConversationIdUtil.conversationType(conversationId);
                  recentForwards.add(new RecentForward(sessionId, sessionType));
                }
                SettingRepo.saveRecentForward(recentForwards);
                MessageHelper.sendNoteMessage(inputMsg, conversationIds, false);
              }
            }
          });
    } catch (IOException ignore) {
    }
  }

  public static void sendMessageWithParams(
      V2NIMMessage message,
      String conversationId,
      List<String> pushList,
      String remoteExtension,
      V2NIMAIUser aiAgent,
      List<V2NIMAIModelCallMessage> aiMessage,
      boolean showRead,
      MutableLiveData<FetchResult<IMMessageProgress>> progressLiveData) {
    V2NIMSendMessageParams params =
        MessageParamBuildUtils.buildSendParams(
            message, conversationId, pushList, remoteExtension, aiAgent, aiMessage, showRead);
    ChatRepo.sendMessage(
        message,
        conversationId,
        params,
        new ProgressFetchCallback<V2NIMSendMessageResult>() {
          @Override
          public void onProgress(int progress) {
            if (message.getMessageClientId() != null) {
              FetchResult<IMMessageProgress> result =
                  MessageParamBuildUtils.buildAttachmentProgress(
                      message.getMessageClientId(), progress);
              progressLiveData.setValue(result);
            }
          }

          @Override
          public void onSuccess(V2NIMSendMessageResult data) {
            if (data != null) {
              V2NIMMessageAIConfig aiConfig = data.getMessage().getAIConfig();
              if (aiConfig != null) {
                ToastX.showShortToast(R.string.chat_ai_message_progressing);
              }
              if (IMKitConfigCenter.getEnableAntiSpamTipMessage()
                  && data.getAntispamResult() != null) {
                String tips =
                    MessageHelper.getAntispamTips(
                        IMKitClient.getApplicationContext(), data.getAntispamResult());
                V2NIMMessage tipMessage = V2NIMMessageCreator.createTipsMessage(tips);
                ChatRepo.insertMessageToLocal(
                    tipMessage,
                    data.getMessage().getConversationId(),
                    data.getMessage().getSenderId(),
                    data.getMessage().getCreateTime() + 5,
                    null);
              }
            }
          }

          @Override
          public void onError(int errorCode, String errorMsg) {}
        });
  }

  public static void replyMessageWithParams(
      V2NIMMessage message,
      V2NIMMessage replyMessage,
      String conversationId,
      List<String> pushList,
      String remoteExtension,
      V2NIMAIUser aiUser,
      List<V2NIMAIModelCallMessage> aiMessageList,
      boolean needACK,
      boolean showRead,
      MutableLiveData<FetchResult<IMMessageProgress>> progressLiveData) {
    V2NIMSendMessageParams params =
        MessageParamBuildUtils.buildSendParams(
            message, conversationId, pushList, remoteExtension, aiUser, aiMessageList, showRead);
    ChatRepo.replyMessage(
        message,
        replyMessage,
        conversationId,
        params,
        new ProgressFetchCallback<V2NIMSendMessageResult>() {
          @Override
          public void onProgress(int progress) {
            if (message.getMessageClientId() != null) {
              FetchResult<IMMessageProgress> result =
                  MessageParamBuildUtils.buildAttachmentProgress(
                      message.getMessageClientId(), progress);
              progressLiveData.setValue(result);
            }
          }

          @Override
          public void onSuccess(V2NIMSendMessageResult data) {
            if (data != null) {
              V2NIMMessageAIConfig aiConfig = data.getMessage().getAIConfig();
              if (aiConfig != null) {
                ToastX.showShortToast(R.string.chat_ai_message_progressing);
              }
            }
          }

          @Override
          public void onError(int errorCode, String errorMsg) {}
        });
  }

  public static V2NIMMessage processUriAndSend(Uri uri, Context context) {
    if (uri == null) {
      return null;
    }
    long limitSize = ChatUtils.getFileLimitSize() * 1024 * 1024;
    String mimeType;
    try {
      String realPath = UriUtils.uri2FileRealPath(uri);
      mimeType = FileUtils.getFileExtension(realPath);
    } catch (IllegalStateException e) {
      ToastX.showShortToast(R.string.chat_message_type_resource_error);
      return null;
    }
    if (ImageUtil.isValidPictureFile(mimeType)) {
      File file = UriUtils.uri2File(uri);
      if (file != null && file.length() > limitSize) {
        String fileSizeLimit = String.valueOf(ChatUtils.getFileLimitSize());
        String limitText =
            String.format(
                context.getString(R.string.chat_message_file_size_limit_tips), fileSizeLimit);
        ToastX.showShortToast(limitText);
        return null;
      }
      V2NIMMessage imageMessage = MessageParamBuildUtils.createImageMessage(file);
      return imageMessage;
    } else if (ImageUtil.isValidVideoFile(mimeType)) {
      File file = UriUtils.uri2File(uri);
      if (file != null && file.length() > limitSize) {
        String fileSizeLimit = String.valueOf(ChatUtils.getFileLimitSize());
        String limitText =
            String.format(
                context.getString(R.string.chat_message_file_size_limit_tips), fileSizeLimit);
        ToastX.showShortToast(limitText);
        return null;
      }
      MediaMetadataRetriever mmr = new MediaMetadataRetriever();
      try {
        mmr.setDataSource(file.getPath());
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String orientation =
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if ("90".equals(orientation)) {
          String local = width;
          width = height;
          height = local;
        }
        ALog.d(
            LIB_TAG,
            "MessageParamBuildUtils",
            "width:" + width + "height" + height + "orientation:" + orientation);
        V2NIMMessage videoMessage =
            MessageParamBuildUtils.createVideoMessage(
                file.getPath(),
                file.getName(),
                Integer.parseInt(duration),
                Integer.parseInt(width),
                Integer.parseInt(height));
        return videoMessage;
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          mmr.release();
        } catch (Exception ignore) {
        }
      }
    } else {
      ToastX.showShortToast(R.string.chat_message_type_not_support_tips);
    }
    return null;
  }

  public static void saveWelcomeMessage(String mConversationId, String mChatAccountId) {
    String content = AIUserManager.getWelcomeText(mChatAccountId);
    if (TextUtils.isEmpty(content)) {
      return;
    }
    V2NIMMessage welcomeMessage = V2NIMMessageCreator.createTextMessage(content);
    ConversationRepo.createConversation(
        mConversationId,
        new FetchCallback<V2NIMConversation>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(TAG, "createConversation onError:" + errorCode + " errorMsg:" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable V2NIMConversation data) {
            //成功之后插入
            ChatRepo.insertMessageToLocal(
                welcomeMessage, mConversationId, mChatAccountId, System.currentTimeMillis(), null);
          }
        });
  }

  // 发送图片消息,参数为图片文件地址path
  public static File checkImageFile(String imagePath) {
    if (imagePath != null) {
      ALog.d(LIB_TAG, TAG, "sendImageMessage:" + imagePath);
      long limitSize = ChatUtils.getFileLimitSize() * 1024 * 1024;
      File file = new File(imagePath);
      if (file.exists() && file.length() > limitSize) {
        String fileSizeLimit = String.valueOf(ChatUtils.getFileLimitSize());
        String limitText =
            String.format(
                IMKitClient.getApplicationContext()
                    .getString(R.string.chat_message_file_size_limit_tips),
                fileSizeLimit);
        ToastX.showShortToast(limitText);
        return null;
      }
      return file;
    }
    return null;
  }

  // -------------------------------------------------------------------------
  // @ 保留翻译工具方法
  // -------------------------------------------------------------------------

  /**
   * 将消息文本按 @ 段切分为交替的"普通文本"和"@ 文本"片段列表。
   *
   * <p>返回两个等长列表：
   *
   * <ul>
   *   <li>first：文本片段列表（顺序保留原文结构）
   *   <li>second：对应片段是否为 @ 段（true = @ 段，不翻译；false = 普通文本，需翻译）
   * </ul>
   *
   * 不使用任何占位符，彻底规避翻译引擎对特殊字符的处理。
   *
   * @param text 消息原始文本（message.getText()）
   * @param message 消息对象（用于读取 serverExtension 中的 @ 信息）
   */
  public static Pair<List<String>, List<Boolean>> splitTextByAtMentions(
      String text, V2NIMMessage message) {
    List<String> parts = new ArrayList<>();
    List<Boolean> isAtFlags = new ArrayList<>();

    if (TextUtils.isEmpty(text)) {
      parts.add(text);
      isAtFlags.add(false);
      return new Pair<>(parts, isAtFlags);
    }

    AtContactsModel atModel = MessageHelper.getAitBlockFromMsg(message);
    if (atModel == null || atModel.getAtBlockList().isEmpty()) {
      parts.add(text);
      isAtFlags.add(false);
      return new Pair<>(parts, isAtFlags);
    }

    // 1. 展平所有 @ 段，AitSegment.end 是 inclusive
    List<int[]> segments = new ArrayList<>(); // [start, end(inclusive)]
    for (AitBlock block : atModel.getAtBlockList()) {
      if (block == null || TextUtils.isEmpty(block.text)) continue;
      for (AitSegment seg : block.segments) {
        if (seg.start < 0 || seg.end >= text.length() || seg.start > seg.end) continue;
        segments.add(new int[] {seg.start, seg.end});
      }
    }

    if (segments.isEmpty()) {
      parts.add(text);
      isAtFlags.add(false);
      return new Pair<>(parts, isAtFlags);
    }

    // 2. 按 start 升序排序，去重（同一 start 取第一个）
    Collections.sort(segments, (a, b) -> a[0] - b[0]);
    List<int[]> deduped = new ArrayList<>();
    int lastStart = -1;
    for (int[] seg : segments) {
      if (seg[0] != lastStart) {
        deduped.add(seg);
        lastStart = seg[0];
      }
    }

    // 3. 按 @ 段位置切分文本
    int cursor = 0;
    for (int[] seg : deduped) {
      int start = seg[0];
      int end = seg[1] + 1; // 转为 exclusive
      // @ 段前的普通文本
      if (cursor < start) {
        parts.add(text.substring(cursor, start));
        isAtFlags.add(false);
      }
      // @ 段本身
      parts.add(text.substring(start, end));
      isAtFlags.add(true);
      cursor = end;
    }
    // 末尾剩余普通文本
    if (cursor < text.length()) {
      parts.add(text.substring(cursor));
      isAtFlags.add(false);
    }

    return new Pair<>(parts, isAtFlags);
  }
}
