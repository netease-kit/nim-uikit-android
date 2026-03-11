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
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.ResourceRepo;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
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
import java.util.List;

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
}
