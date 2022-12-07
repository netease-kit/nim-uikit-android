// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.CollectInfo;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MemberPushOption;
import com.netease.nimlib.sdk.msg.model.MsgPinOption;
import com.netease.nimlib.sdk.msg.model.MsgPinSyncResponseOption;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.media.ImageUtil;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatCallback;
import com.netease.yunxin.kit.chatkit.ui.model.ChatConstants;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitContactsModel;
import com.netease.yunxin.kit.chatkit.utils.SendMediaHelper;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.FileUtils;
import com.netease.yunxin.kit.common.utils.UriUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.UserInfoObserver;
import com.netease.yunxin.kit.corekit.im.repo.SettingRepo;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** chat info view model fetch and send messages for chat page */
public abstract class ChatBaseViewModel extends BaseViewModel {
  public static final String TAG = "ChatViewModel";
  private final MutableLiveData<FetchResult<List<ChatMessageBean>>> messageLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ChatMessageBean>> messageFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final MutableLiveData<FetchResult<List<UserInfo>>> userInfoLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<UserInfo>> userInfoFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final MutableLiveData<FetchResult<ChatMessageBean>> sendMessageLiveData =
      new MutableLiveData<>();
  private final FetchResult<ChatMessageBean> sendMessageFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final MutableLiveData<FetchResult<AttachmentProgress>> attachmentProgressMutableLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<ChatMessageBean>> revokeMessageLiveData =
      new MutableLiveData<>();

  protected String mSessionId;
  private SessionTypeEnum mSessionType;

  private long credibleTimestamp = -1;
  private final int messagePageSize = 100;
  private final String Orientation_Vertical = "90";

  private final EventObserver<List<IMMessageInfo>> receiveMessageObserver =
      new EventObserver<List<IMMessageInfo>>() {
        @Override
        public void onEvent(@Nullable List<IMMessageInfo> event) {
          ALog.d(LIB_TAG, TAG, "receive msg -->> " + (event == null ? "null" : event.size()));
          messageFetchResult.setLoadStatus(LoadStatus.Finish);
          messageFetchResult.setData(convert(event));
          messageFetchResult.setType(FetchResult.FetchType.Add);
          messageFetchResult.setTypeIndex(-1);
          messageLiveData.postValue(messageFetchResult);
        }
      };

  private final EventObserver<IMMessageInfo> msgStatusObserver =
      new EventObserver<IMMessageInfo>() {
        @Override
        public void onEvent(@Nullable IMMessageInfo event) {
          ALog.d(
              LIB_TAG,
              TAG,
              "msg status change -->> "
                  + (event == null ? "null" : event.getMessage().getStatus()));
          sendMessageFetchResult.setLoadStatus(LoadStatus.Finish);
          sendMessageFetchResult.setData(new ChatMessageBean(event));
          sendMessageFetchResult.setType(FetchResult.FetchType.Update);
          sendMessageFetchResult.setTypeIndex(-1);
          sendMessageLiveData.postValue(sendMessageFetchResult);
        }
      };

  private final EventObserver<List<IMMessageInfo>> msgSendingObserver =
      new EventObserver<List<IMMessageInfo>>() {
        @Override
        public void onEvent(@Nullable List<IMMessageInfo> event) {
          ALog.d(
              LIB_TAG, TAG, "msg sending change -->> " + (event == null ? "null" : event.size()));
          if (event != null && event.size() > 0) {
            postMessageSend(event.get(0), false);
          }
        }
      };

  private final Observer<AttachmentProgress> attachmentProgressObserver =
      attachmentProgress -> {
        ALog.d(
            LIB_TAG,
            TAG,
            "attachment progress update -->> "
                + attachmentProgress.getTransferred()
                + "/"
                + attachmentProgress.getTotal());
        FetchResult<AttachmentProgress> result = new FetchResult<>(LoadStatus.Finish);
        result.setData(attachmentProgress);
        result.setType(FetchResult.FetchType.Update);
        result.setTypeIndex(-1);
        attachmentProgressMutableLiveData.postValue(result);
      };

  private Observer<RevokeMsgNotification> revokeMsgObserver =
      revokeMsgNotification -> {
        ALog.d(LIB_TAG, TAG, "revokeMsgObserver");
        FetchResult<ChatMessageBean> fetchResult = new FetchResult<>(LoadStatus.Success);
        fetchResult.setData(
            new ChatMessageBean(new IMMessageInfo(revokeMsgNotification.getMessage())));
        revokeMessageLiveData.postValue(fetchResult);
      };

  private final UserInfoObserver userInfoObserver =
      userList -> {
        userInfoFetchResult.setLoadStatus(LoadStatus.Finish);
        userInfoFetchResult.setData(userList);
        userInfoFetchResult.setType(FetchResult.FetchType.Update);
        userInfoFetchResult.setTypeIndex(-1);
        userInfoLiveData.postValue(userInfoFetchResult);
      };

  /** chat message revoke live data */
  public MutableLiveData<FetchResult<ChatMessageBean>> getRevokeMessageLiveData() {
    return revokeMessageLiveData;
  }

  /** query chat message list */
  public MutableLiveData<FetchResult<List<ChatMessageBean>>> getQueryMessageLiveData() {
    return messageLiveData;
  }

  public MutableLiveData<FetchResult<List<UserInfo>>> getUserInfoLiveData() {
    return userInfoLiveData;
  }

  public void deleteMessage(IMMessageInfo messageInfo) {
    ChatRepo.deleteMessage(messageInfo);
  }

  public void revokeMessage(ChatMessageBean messageBean) {
    if (messageBean != null && messageBean.getMessageData() != null) {
      ALog.d(LIB_TAG, TAG, "revokeMessage " + messageBean.getMessageData().getMessage().getUuid());
      ChatRepo.revokeMessage(
          messageBean.getMessageData(),
          new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
              FetchResult<ChatMessageBean> fetchResult = new FetchResult<>(LoadStatus.Success);
              fetchResult.setData(messageBean);
              revokeMessageLiveData.postValue(fetchResult);
              ALog.d(LIB_TAG, TAG, "revokeMessage, onSuccess");
            }

            @Override
            public void onFailed(int code) {
              FetchResult<ChatMessageBean> fetchResult = new FetchResult<>(LoadStatus.Error);
              fetchResult.setError(
                  code,
                  code == ResponseCode.RES_OVERDUE
                      ? R.string.chat_message_revoke_over_time
                      : R.string.chat_message_revoke_error);
              revokeMessageLiveData.postValue(fetchResult);
              ALog.d(LIB_TAG, TAG, "revokeMessage,onFailed:" + code);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              FetchResult<ChatMessageBean> fetchResult = new FetchResult<>(LoadStatus.Error);
              fetchResult.setError(-1, R.string.chat_message_revoke_error);
              revokeMessageLiveData.postValue(fetchResult);
              ALog.d(LIB_TAG, TAG, "revokeMessage,onException");
            }
          });
    }
  }

  /** send new message or chat message status change live data */
  public MutableLiveData<FetchResult<ChatMessageBean>> getSendMessageLiveData() {
    return sendMessageLiveData;
  }

  /** message attachment load progress live data */
  public MutableLiveData<FetchResult<AttachmentProgress>> getAttachmentProgressMutableLiveData() {
    return attachmentProgressMutableLiveData;
  }

  public void init(String sessionId, SessionTypeEnum sessionType) {
    ALog.d(LIB_TAG, TAG, "init sessionId:" + sessionId + " sessionType:" + sessionType);
    this.mSessionId = sessionId;
    this.mSessionType = sessionType;
  }

  public void setChattingAccount() {
    ALog.d(LIB_TAG, TAG, "setChattingAccount sessionId:" + mSessionId);
    ChatRepo.setChattingAccount(mSessionId, mSessionType);
  }

  public String getSessionId() {
    return mSessionId;
  }

  public boolean isShowReadStatus() {
    return SettingRepo.getShowReadStatus();
  }

  public void clearChattingAccount() {
    ChatRepo.clearChattingAccount();
  }

  public void registerObservers() {
    ALog.d(LIB_TAG, TAG, "registerObservers ");
    ChatObserverRepo.registerReceiveMessageObserve(mSessionId, receiveMessageObserver);
    ChatObserverRepo.registerMsgStatusObserve(msgStatusObserver);
    ChatObserverRepo.registerAttachmentProgressObserve(attachmentProgressObserver);
    ChatObserverRepo.registerMessageSendingObserve(mSessionId, msgSendingObserver);
    ChatObserverRepo.registerRevokeMessageObserve(revokeMsgObserver);
    ChatRepo.registerUserInfoObserver(userInfoObserver);
    ChatObserverRepo.registerAddMessagePinObserve(msgPinAddObserver);
    ChatObserverRepo.registerRemoveMessagePinObserve(msgPinRemoveObserver);
  }

  public void unregisterObservers() {
    ALog.d(LIB_TAG, TAG, "unregisterObservers ");
    ChatObserverRepo.unregisterReceiveMessageObserve(mSessionId, receiveMessageObserver);
    ChatObserverRepo.unregisterMsgStatusObserve(msgStatusObserver);
    ChatObserverRepo.unregisterAttachmentProgressObserve(attachmentProgressObserver);
    ChatObserverRepo.unregisterMessageSendingObserve(mSessionId, msgSendingObserver);
    ChatObserverRepo.unregisterRevokeMessageObserve(revokeMsgObserver);
    ChatRepo.unregisterUserInfoObserver(userInfoObserver);
    ChatObserverRepo.unregisterAddMessagePinObserve(msgPinAddObserver);
    ChatObserverRepo.unregisterRemoveMessagePinObserve(msgPinRemoveObserver);
  }

  public void sendTextMessage(String content, List<String> pushList) {
    ALog.d(LIB_TAG, TAG, "sendTextMessage:" + (content != null ? content.length() : "null"));
    IMMessage textMsg = MessageBuilder.createTextMessage(mSessionId, mSessionType, content);
    appendTeamMemberPush(textMsg, pushList);
    sendMessage(textMsg, false, true);
  }

  public void addMsgCollection(IMMessageInfo messageInfo) {
    ALog.d(
        LIB_TAG,
        TAG,
        "addMsgCollection:" + (messageInfo != null ? messageInfo.getMessage().getUuid() : "null"));
    ChatRepo.collectMessage(
        messageInfo.getMessage(), new ChatCallback<CollectInfo>().setShowSuccess(true));
  }

  public void sendAudioMessage(File audio, long audioLength) {
    if (audio != null) {
      ALog.d(LIB_TAG, TAG, "sendAudioMessage:" + audio.getPath());
      IMMessage audioMsg =
          MessageBuilder.createAudioMessage(mSessionId, mSessionType, audio, audioLength);
      sendMessage(audioMsg, false, true);
    }
  }

  public void sendImageMessage(File imageFile) {
    if (imageFile != null) {
      ALog.d(LIB_TAG, TAG, "sendImageMessage:" + imageFile.getPath());
      IMMessage imageMsg = MessageBuilder.createImageMessage(mSessionId, mSessionType, imageFile);
      sendMessage(imageMsg, false, true);
    }
  }

  public void sendCustomMessage(MsgAttachment attachment, String content) {
    if (attachment != null) {
      ALog.d(LIB_TAG, TAG, "sendCustomMessage:" + attachment.getClass().getName());
      IMMessage customMessage =
          MessageBuilder.createCustomMessage(mSessionId, mSessionType, content, attachment);
      sendMessage(customMessage, false, true);
    }
  }

  public void sendForwardMessage(IMMessage message, String sessionId, SessionTypeEnum sessionType) {
    ALog.d(LIB_TAG, TAG, "sendForwardMessage:" + sessionId);
    IMMessage forwardMessage = MessageBuilder.createForwardMessage(message, sessionId, sessionType);
    sendMessage(forwardMessage, false, TextUtils.equals(sessionId, mSessionId));
  }

  public void replyImageMessage(File imageFile, IMMessage message) {
    if (imageFile != null) {
      ALog.d(LIB_TAG, TAG, "replyImageMessage:" + imageFile.getPath());
      IMMessage imageMsg = MessageBuilder.createImageMessage(mSessionId, mSessionType, imageFile);
      replyMessage(imageMsg, message, false);
    }
  }

  public void sendVideoMessage(
      File videoFile, long duration, int width, int height, String displayName) {
    if (videoFile != null) {
      ALog.d(LIB_TAG, TAG, "sendVideoMessage:" + videoFile.getPath());
      IMMessage message =
          MessageBuilder.createVideoMessage(
              mSessionId, mSessionType, videoFile, duration, width, height, displayName);
      sendMessage(message, false, true);
    }
  }

  public void downloadMessageAttachment(IMMessage message) {
    if (message.getAttachment() instanceof FileAttachment) {
      ALog.d(LIB_TAG, TAG, "downloadMessageAttachment:" + message.getUuid());
      ChatRepo.downloadAttachment(message, false, null);
    }
  }

  public void sendImageOrVideoMessage(Uri uri) {
    ALog.d(LIB_TAG, TAG, "sendImageOrVideoMessage:" + uri.getPath());
    String mimeType = FileUtils.getFileExtension(uri.getPath());
    if (TextUtils.isEmpty(mimeType)) {
      String realPath = UriUtils.uri2FileRealPath(uri);
      mimeType = FileUtils.getFileExtension(realPath);
    }
    if (ImageUtil.isValidPictureFile(mimeType)) {
      SendMediaHelper.handleImage(uri, false, this::sendImageMessage);
    } else if (ImageUtil.isValidVideoFile(mimeType)) {
      SendMediaHelper.handleVideo(
          uri,
          file -> {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
              mmr.setDataSource(file.getPath());
              String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
              String width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
              String height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
              String orientation =
                  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
              if (TextUtils.equals(orientation, Orientation_Vertical)) {
                String local = width;
                width = height;
                height = local;
              }
              ALog.e(
                  LIB_TAG,
                  TAG,
                  "width:" + width + "height" + height + "orientation:" + orientation);
              sendVideoMessage(
                  file,
                  Long.parseLong(duration),
                  Integer.parseInt(width),
                  Integer.parseInt(height),
                  file.getName());
            } catch (Exception e) {
              e.printStackTrace();
            } finally {
              mmr.release();
            }
          });
    } else {
      ALog.e(LIB_TAG, TAG, "invalid file type");
    }
  }

  private void onMessageSend(IMMessage message, boolean resend) {
    ALog.d(LIB_TAG, TAG, "onMessageSend:sending");
    ChatRepo.fetchUserInfo(
        message.getFromAccount(),
        new FetchCallback<UserInfo>() {
          @Override
          public void onSuccess(@Nullable UserInfo param) {
            ALog.d(LIB_TAG, TAG, "onMessageSend:onSuccess");
            IMMessageInfo messageInfo = new IMMessageInfo(message);
            messageInfo.setFromUser(param);
            postMessageSend(messageInfo, resend);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "onMessageSend:onFailed" + code);
            IMMessageInfo messageInfo = new IMMessageInfo(message);
            postMessageSend(messageInfo, resend);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "onMessageSend:onException");
            IMMessageInfo messageInfo = new IMMessageInfo(message);
            postMessageSend(messageInfo, resend);
          }
        });
  }

  private void postMessageSend(IMMessageInfo message, boolean resend) {
    ALog.d(LIB_TAG, TAG, "postMessageSend");
    sendMessageFetchResult.setLoadStatus(LoadStatus.Loading);
    if (resend) {
      sendMessageFetchResult.setType(FetchResult.FetchType.Update);
    } else {
      sendMessageFetchResult.setType(FetchResult.FetchType.Add);
    }
    sendMessageFetchResult.setData(new ChatMessageBean(message));
    sendMessageLiveData.postValue(sendMessageFetchResult);
  }

  public void sendMessage(IMMessage message, boolean resend, boolean needSendMessage) {
    if (message != null) {
      ALog.d(LIB_TAG, TAG, "sendMessage:" + message.getUuid());
      if (SettingRepo.getShowReadStatus()) {
        message.setMsgAck();
      }
      //      if (needSendMessage) {
      //        onMessageSend(message, resend);
      //      }
      ChatRepo.sendMessage(message, resend, null);
    }
  }

  public abstract void sendReceipt(IMMessage message);

  /** called when entering the chat page */
  public void initFetch(IMMessage anchor) {
    ALog.d(LIB_TAG, TAG, "initFetch:" + (anchor == null ? "null" : anchor.getUuid()));
    registerObservers();

    queryRoamMsgHasMoreTime(
        new FetchCallback<Long>() {
          @Override
          public void onSuccess(@Nullable Long param) {
            credibleTimestamp = param == null ? 0 : param;
            ALog.d(
                LIB_TAG,
                TAG,
                "initFetch:queryRoamMsgHasMoreTime -->> credibleTimestamp:" + credibleTimestamp);
            if (anchor == null) {
              fetchMoreMessage(
                  MessageBuilder.createEmptyMessage(mSessionId, mSessionType, 0),
                  QueryDirectionEnum.QUERY_OLD);
            } else {
              fetchMessageListBothDirect(anchor);
            }
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "initFetch:queryRoamMsgHasMoreTime:onFailed" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "initFetch:queryRoamMsgHasMoreTime:onException");
          }
        });
  }

  public void fetchMoreMessage(IMMessage anchor, QueryDirectionEnum direction) {
    if (!isMessageCredible(anchor)) {
      ALog.d(LIB_TAG, TAG, "fetchMoreMessage anchor is not credible");
      if (direction == QueryDirectionEnum.QUERY_NEW) {
        fetchMessageRemoteNewer(anchor);
      } else {
        fetchMessageRemoteOlder(anchor, false);
      }
      return;
    }
    ALog.d(LIB_TAG, TAG, "fetch local anchor time:" + anchor.getTime() + " direction:" + direction);
    ChatRepo.getHistoryMessage(
        anchor,
        direction,
        messagePageSize,
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "fetch local no more messages -->> try remote,onSuccess" + (param == null));
            if (param == null || param.isEmpty()) {
              // no more local messages
              if (direction == QueryDirectionEnum.QUERY_OLD && credibleTimestamp > 0) {
                fetchMessageRemoteOlder(anchor, true);
              } else {
                onListFetchSuccess(param, direction);
              }
              return;
            }
            if (direction == QueryDirectionEnum.QUERY_OLD) {
              if (isMessageCredible(param.get(0).getMessage())) {
                onListFetchSuccess(param, direction);
              } else {
                fetchMessageRemoteOlder(anchor, true);
              }
            } else {
              onListFetchSuccess(param, direction);
            }
          }

          @Override
          public void onFailed(int code) {
            onListFetchFailed(code);
            ALog.d(LIB_TAG, TAG, "fetch local no more messages -->> try remote,onFailed:" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            onListFetchFailed(ChatConstants.ERROR_CODE_FETCH_MSG);
          }
        });
  }

  public void fetchMessageListBothDirect(IMMessage anchor) {
    ALog.d(LIB_TAG, TAG, "fetchMessageListBothDirect");
    fetchMoreMessage(anchor, QueryDirectionEnum.QUERY_OLD);
    fetchMoreMessage(anchor, QueryDirectionEnum.QUERY_NEW);
  }

  private void fetchMessageRemoteOlder(IMMessage anchor, boolean updateCredible) {
    ALog.d(
        LIB_TAG,
        TAG,
        "fetch remote old anchor time:" + anchor.getTime() + " need update:" + updateCredible);
    ChatRepo.fetchHistoryMessage(
        anchor,
        0,
        messagePageSize,
        QueryDirectionEnum.QUERY_OLD,
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "fetchMessageRemoteOlder, fetchHistoryMessage,onSuccess:"
                    + (param == null)
                    + credibleTimestamp);
            if (param != null) {
              Collections.reverse(param);
            }
            if (updateCredible && param != null && param.size() > 0) {
              credibleTimestamp = param.get(0).getMessage().getTime();
              updateRoamMsgHasMoreTag(param.get(0).getMessage());
            }
            onListFetchSuccess(param, QueryDirectionEnum.QUERY_OLD);
          }

          @Override
          public void onFailed(int code) {
            onListFetchFailed(code);
            ALog.d(LIB_TAG, TAG, "fetchMessageRemoteOlder, fetchHistoryMessage,onFailed:" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            onListFetchFailed(ChatConstants.ERROR_CODE_FETCH_MSG);
            ALog.d(LIB_TAG, TAG, "fetchMessageRemoteOlder, fetchHistoryMessage,onException:");
          }
        });
  }

  private void fetchMessageRemoteNewer(IMMessage anchor) {
    ALog.d(LIB_TAG, TAG, "fetchMessageRemoteNewer:" + anchor.getTime());
    ChatRepo.fetchHistoryMessage(
        anchor,
        0,
        messagePageSize,
        QueryDirectionEnum.QUERY_NEW,
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "fetchMessageRemoteNewer,onSuccess:" + (param == null ? "null" : param.size()));
            // no need to update credible time, because all messages behind this
            onListFetchSuccess(param, QueryDirectionEnum.QUERY_NEW);
          }

          @Override
          public void onFailed(int code) {

            ALog.d(LIB_TAG, TAG, "fetchMessageRemoteNewer,onFailed:" + code);
            onListFetchFailed(code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "fetchMessageRemoteNewer,onException:");
            onListFetchFailed(ChatConstants.ERROR_CODE_FETCH_MSG);
          }
        });
  }

  private boolean isMessageCredible(IMMessage message) {
    ALog.d(
        LIB_TAG,
        TAG,
        "isMessageCredible -->> credibleTimestamp:"
            + credibleTimestamp
            + " msgTime:"
            + message.getTime());
    return credibleTimestamp <= 0 || message.getTime() >= credibleTimestamp;
  }

  private void onListFetchSuccess(List<IMMessageInfo> param, QueryDirectionEnum direction) {
    ALog.d(
        LIB_TAG,
        TAG,
        "onListFetchSuccess -->> size:"
            + (param == null ? "null" : param.size())
            + " direction:"
            + direction);

    LoadStatus loadStatus =
        (param == null || param.size() == 0) ? LoadStatus.Finish : LoadStatus.Success;
    messageFetchResult.setLoadStatus(loadStatus);
    messageFetchResult.setData(convert(param));
    messageFetchResult.setTypeIndex(direction == QueryDirectionEnum.QUERY_OLD ? 0 : -1);
    messageLiveData.postValue(messageFetchResult);
  }

  private void onListFetchFailed(int code) {
    ALog.d(LIB_TAG, TAG, "onListFetchFailed code:" + code);
    messageFetchResult.setError(code, R.string.chat_message_fetch_error);
    messageFetchResult.setData(null);
    messageFetchResult.setTypeIndex(-1);
    messageLiveData.postValue(messageFetchResult);
  }

  public void queryRoamMsgHasMoreTime(FetchCallback<Long> callback) {
    ALog.d(LIB_TAG, TAG, "queryRoamMsgHasMoreTime");
    ChatRepo.queryRoamMsgTimestamps(mSessionId, mSessionType, callback);
  }

  public void updateRoamMsgHasMoreTag(IMMessage newTag) {
    ALog.d(LIB_TAG, TAG, "updateRoamMsgHasMoreTag:" + (newTag == null ? "null" : newTag.getTime()));
    ChatRepo.updateRoamMsgTimestamps(newTag);
  }

  private List<ChatMessageBean> convert(List<IMMessageInfo> messageList) {
    if (messageList == null) {
      return null;
    }
    ArrayList<ChatMessageBean> result = new ArrayList<>(messageList.size());
    for (IMMessageInfo message : messageList) {
      result.add(new ChatMessageBean(message));
    }
    return result;
  }

  //**********reply message**************
  public void replyMessage(IMMessage message, IMMessage replyMsg, boolean resend) {
    ALog.d(LIB_TAG, TAG, "replyMessage,message" + (message == null ? "null" : message.getUuid()));
    message.setThreadOption(replyMsg);
    message.setMsgAck();
    //    onMessageSend(message, resend);
    ChatRepo.replyMessage(message, replyMsg, resend, null);
  }

  public void replyTextMessage(String content, IMMessage message, List<String> pushList) {
    ALog.d(
        LIB_TAG, TAG, "replyTextMessage,message" + (message == null ? "null" : message.getUuid()));
    IMMessage textMsg = MessageBuilder.createTextMessage(mSessionId, mSessionType, content);
    appendTeamMemberPush(textMsg, pushList);
    replyMessage(textMsg, message, false);
  }

  //**********Message Pin****************

  private void appendTeamMemberPush(IMMessage message, List<String> pushList) {
    ALog.d(
        LIB_TAG,
        TAG,
        "appendTeamMemberPush,message" + (message == null ? "null" : message.getUuid()));
    if (mSessionType == SessionTypeEnum.Team && pushList != null && !pushList.isEmpty()) {
      MemberPushOption memberPushOption = new MemberPushOption();
      memberPushOption.setForcePush(true);
      memberPushOption.setForcePushContent(message.getContent());
      if (pushList.size() == 1 && pushList.get(0).equals(AitContactsModel.ACCOUNT_ALL)) {
        memberPushOption.setForcePushList(null);
      } else {
        memberPushOption.setForcePushList(pushList);
      }
      message.setMemberPushOption(memberPushOption);
    }
  }

  //********************Message Pin********************

  private final MutableLiveData<Pair<String, MsgPinOption>> addPinMessageLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<String> removePinMessageLiveData = new MutableLiveData<>();

  public MutableLiveData<Pair<String, MsgPinOption>> getAddPinMessageLiveData() {
    return addPinMessageLiveData;
  }

  public MutableLiveData<String> getRemovePinMessageLiveData() {
    return removePinMessageLiveData;
  }

  private final Observer<MsgPinSyncResponseOption> msgPinAddObserver =
      msgPinSyncResponseOption -> {
        Pair<String, MsgPinOption> pinInfo =
            new Pair<>(
                msgPinSyncResponseOption.getKey().getUuid(),
                msgPinSyncResponseOption.getPinOption());
        addPinMessageLiveData.postValue(pinInfo);
      };

  private final Observer<MsgPinSyncResponseOption> msgPinRemoveObserver =
      responseOption -> removePinMessageLiveData.postValue(responseOption.getKey().getUuid());

  public void addMessagePin(IMMessageInfo messageInfo, String ext) {
    ALog.d(
        LIB_TAG,
        TAG,
        "addMessagePin,message"
            + (messageInfo == null ? "null" : messageInfo.getMessage().getUuid()));
    ChatRepo.addMessagePin(
        messageInfo.getMessage(),
        ext,
        new ChatCallback<Long>() {
          @Override
          public void onSuccess(@Nullable Long param) {
            super.onSuccess(param);
            ALog.d(LIB_TAG, TAG, "addMessagePin,onSuccess" + param);
            MsgPinOption pinOption =
                new MsgPinOption() {
                  @Override
                  public String getAccount() {
                    return IMKitClient.account();
                  }

                  @Override
                  public String getExt() {
                    return ext;
                  }

                  @Override
                  public long getCreateTime() {
                    return System.currentTimeMillis();
                  }

                  @Override
                  public long getUpdateTime() {
                    return System.currentTimeMillis();
                  }
                };
            Pair<String, MsgPinOption> pinInfo =
                new Pair<>(messageInfo.getMessage().getUuid(), pinOption);
            addPinMessageLiveData.postValue(pinInfo);
          }
        });
  }

  public void removeMsgPin(IMMessageInfo messageInfo) {
    ALog.d(
        LIB_TAG,
        TAG,
        "removeMsgPin,message"
            + (messageInfo == null ? "null" : messageInfo.getMessage().getUuid()));
    ChatRepo.removeMessagePin(
        messageInfo.getMessage(),
        new ChatCallback<Long>() {
          @Override
          public void onSuccess(@Nullable Long param) {
            super.onSuccess(param);
            ALog.d(LIB_TAG, TAG, "removeMsgPin,onSuccess" + param);
            removePinMessageLiveData.postValue(messageInfo.getMessage().getUuid());
          }
        });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    unregisterObservers();
  }
}
