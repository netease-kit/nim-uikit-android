// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REVOKE_CONTENT_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REVOKE_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REVOKE_TIME_TAG;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Pair;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.CollectInfo;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.GetMessageDirectionEnum;
import com.netease.nimlib.sdk.msg.model.GetMessagesDynamicallyParam;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MemberPushOption;
import com.netease.nimlib.sdk.msg.model.MsgPinOption;
import com.netease.nimlib.sdk.msg.model.MsgPinSyncResponseOption;
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.map.ChatLocationBean;
import com.netease.yunxin.kit.chatkit.media.ImageUtil;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.MessageDynamicallyResult;
import com.netease.yunxin.kit.chatkit.repo.ChatObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatCallback;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.model.ChatConstants;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitContactsModel;
import com.netease.yunxin.kit.chatkit.utils.SendMediaHelper;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
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
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** chat info view model fetch and send messages for chat page */
public abstract class ChatBaseViewModel extends BaseViewModel {
  public static final String TAG = "ChatViewModel";
  //拉取历史消息
  private final MutableLiveData<FetchResult<List<ChatMessageBean>>> messageLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ChatMessageBean>> messageFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  //接受消息
  private final MutableLiveData<FetchResult<List<ChatMessageBean>>> messageRecLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ChatMessageBean>> messageRecFetchResult =
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
  protected boolean mIsTeamGroup = false;
  protected boolean needACK = false;
  protected boolean showRead = true;

  private final int messagePageSize = 100;
  private final String Orientation_Vertical = "90";

  private final EventObserver<List<IMMessageInfo>> receiveMessageObserver =
      new EventObserver<List<IMMessageInfo>>() {
        @Override
        public void onEvent(@Nullable List<IMMessageInfo> event) {
          ALog.d(LIB_TAG, TAG, "receive msg -->> " + (event == null ? "null" : event.size()));
          messageRecFetchResult.setLoadStatus(LoadStatus.Finish);
          messageRecFetchResult.setData(convert(event));
          messageRecFetchResult.setType(FetchResult.FetchType.Add);
          messageRecFetchResult.setTypeIndex(-1);
          messageRecLiveData.setValue(messageRecFetchResult);
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
          if (event != null && TextUtils.equals(event.getMessage().getSessionId(), mSessionId)) {
            sendMessageFetchResult.setLoadStatus(LoadStatus.Finish);
            sendMessageFetchResult.setData(new ChatMessageBean(event));
            sendMessageFetchResult.setType(FetchResult.FetchType.Update);
            sendMessageFetchResult.setTypeIndex(-1);
            sendMessageLiveData.setValue(sendMessageFetchResult);
          }
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
        attachmentProgressMutableLiveData.setValue(result);
      };

  //他人撤回消息底层会收到通知进行处理，并保存到本地。当前账号的撤回需要自行处理
  private Observer<RevokeMsgNotification> revokeMsgObserver =
      revokeMsgNotification -> {
        ALog.d(LIB_TAG, TAG, "revokeMsgObserver");
        ChatMessageBean messageBean =
            new ChatMessageBean(new IMMessageInfo(revokeMsgNotification.getMessage()));
        FetchResult<ChatMessageBean> fetchResult = new FetchResult<>(LoadStatus.Success);
        fetchResult.setData(messageBean);
        revokeMessageLiveData.setValue(fetchResult);
      };

  private final UserInfoObserver userInfoObserver =
      userList -> {
        userInfoFetchResult.setLoadStatus(LoadStatus.Finish);
        userInfoFetchResult.setData(userList);
        userInfoFetchResult.setType(FetchResult.FetchType.Update);
        userInfoFetchResult.setTypeIndex(-1);
        userInfoLiveData.setValue(userInfoFetchResult);
      };

  /** chat message revoke live data */
  public MutableLiveData<FetchResult<ChatMessageBean>> getRevokeMessageLiveData() {
    return revokeMessageLiveData;
  }

  /** query chat message list */
  public MutableLiveData<FetchResult<List<ChatMessageBean>>> getQueryMessageLiveData() {
    return messageLiveData;
  }

  /** receive chat message list */
  public MutableLiveData<FetchResult<List<ChatMessageBean>>> getRecMessageLiveData() {
    return messageRecLiveData;
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
              //他人撤回消息底层会收到通知进行处理，并保存到本地。当前账号的撤回需要自行处理
              saveLocalRevokeMessage(messageBean.getMessageData().getMessage());
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
    this.needACK = SettingRepo.getShowReadStatus();
  }

  public void setChattingAccount() {
    ALog.d(LIB_TAG, TAG, "setChattingAccount sessionId:" + mSessionId);
    ChatRepo.setChattingAccount(mSessionId, mSessionType);
  }

  public void setTeamGroup(boolean group) {
    mIsTeamGroup = group;
  }

  public String getSessionId() {
    return mSessionId;
  }

  public void setShowReadStatus(boolean show) {
    showRead = show;
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

  public void sendLocationMessage(ChatLocationBean locationBean) {
    ALog.d(LIB_TAG, TAG, "sendLocationMessage:" + locationBean);
    IMMessage locationMessage =
        MessageBuilder.createLocationMessage(
            mSessionId,
            mSessionType,
            locationBean.getLat(),
            locationBean.getLng(),
            locationBean.getAddress());
    locationMessage.setContent(locationBean.getTitle());
    sendMessage(locationMessage, false, true);
  }

  public void replyImageMessage(File imageFile, IMMessage message) {
    if (imageFile != null) {
      ALog.d(LIB_TAG, TAG, "replyImageMessage:" + imageFile.getPath());
      IMMessage imageMsg = MessageBuilder.createImageMessage(mSessionId, mSessionType, imageFile);
      replyMessage(imageMsg, message, false);
    }
  }

  private static void saveLocalRevokeMessage(IMMessage message) {
    Map<String, Object> map = new HashMap<>(2);
    map.put(KEY_REVOKE_TAG, true);
    map.put(KEY_REVOKE_TIME_TAG, SystemClock.elapsedRealtime());
    map.put(KEY_REVOKE_CONTENT_TAG, message.getContent());
    if (message.getMsgType() != MsgTypeEnum.text) {
      map.put(RouterConstant.KEY_REVOKE_EDIT_TAG, false);
    } else {
      map.put(RouterConstant.KEY_REVOKE_EDIT_TAG, true);
    }

    IMMessage revokeMsg =
        MessageBuilder.createTextMessage(
            message.getSessionId(),
            message.getSessionType(),
            IMKitClient.getApplicationContext()
                .getResources()
                .getString(R.string.chat_message_revoke_content));
    revokeMsg.setStatus(MsgStatusEnum.success);
    revokeMsg.setDirect(message.getDirect());
    revokeMsg.setFromAccount(message.getFromAccount());
    revokeMsg.setLocalExtension(map);
    CustomMessageConfig config = new CustomMessageConfig();
    config.enableUnreadCount = false;
    revokeMsg.setConfig(config);
    ChatRepo.saveLocalMessageExt(revokeMsg, message.getTime());
    ALog.d(LIB_TAG, TAG, "saveLocalRevokeMessage:" + message.getTime());
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

  public void sendFileMessage(File docsFile, String displayName) {
    if (docsFile != null) {
      ALog.d(LIB_TAG, TAG, "sendFileMessage:" + docsFile.getPath());
      if (TextUtils.isEmpty(displayName)) {
        displayName = docsFile.getName();
      }
      IMMessage message =
          MessageBuilder.createFileMessage(mSessionId, mSessionType, docsFile, displayName);
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
    ALog.d(LIB_TAG, TAG, "sendImageOrVideoMessage:" + uri);
    if (uri == null) {
      return;
    }
    String mimeType = FileUtils.getFileExtension(uri.getPath());
    if (TextUtils.isEmpty(mimeType)) {
      try {
        String realPath = UriUtils.uri2FileRealPath(uri);
        mimeType = FileUtils.getFileExtension(realPath);
      } catch (IllegalStateException e) {
        ToastX.showShortToast(R.string.chat_message_type_resource_error);
        return;
      }
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
      ToastX.showShortToast(R.string.chat_message_type_not_support_tips);
      ALog.e(LIB_TAG, TAG, "invalid file type");
    }
  }

  public void sendFile(Uri uri) {
    ALog.d(LIB_TAG, TAG, "sendFile:" + (uri != null ? uri.getPath() : "uri is null"));
    SendMediaHelper.handleFile(
        uri,
        file -> {
          try {
            String displayName = ChatUtils.getUrlFileName(IMKitClient.getApplicationContext(), uri);
            sendFileMessage(file, displayName);
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
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
    sendMessageLiveData.setValue(sendMessageFetchResult);
  }

  public void sendMessage(IMMessage message, boolean resend, boolean needSendMessage) {
    if (message != null) {
      ALog.d(LIB_TAG, TAG, "sendMessage:" + message.getUuid());
      if (needACK && showRead) {
        message.setMsgAck();
      }
      ChatRepo.sendMessage(message, resend, null);
    }
  }

  public abstract void sendReceipt(IMMessage message);

  /** called when entering the chat page */
  public void initFetch(IMMessage anchor) {
    ALog.d(LIB_TAG, TAG, "initFetch:" + (anchor == null ? "null" : anchor.getUuid()));
    registerObservers();
    if (anchor == null) {
      GetMessagesDynamicallyParam dynamicallyParam =
          new GetMessagesDynamicallyParam(mSessionId, mSessionType);
      dynamicallyParam.setLimit(messagePageSize);
      dynamicallyParam.setDirection(GetMessageDirectionEnum.FORWARD);
      ChatRepo.getMessagesDynamically(
          dynamicallyParam,
          new FetchCallback<MessageDynamicallyResult>() {
            @Override
            public void onSuccess(@Nullable MessageDynamicallyResult result) {
              if (result != null) {
                Collections.reverse(result.getMessageList());
                onListFetchSuccess(result.getMessageList(), GetMessageDirectionEnum.FORWARD);
              }
            }

            @Override
            public void onFailed(int code) {
              ALog.d(LIB_TAG, TAG, "initFetch:getMessagesDynamically:onFailed" + code);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              ALog.d(LIB_TAG, TAG, "initFetch:getMessagesDynamically:onException");
            }
          });
    } else {
      fetchMessageListBothDirect(anchor);
    }
  }

  public void fetchMoreMessage(IMMessage anchor, GetMessageDirectionEnum direction) {
    ALog.d(
        LIB_TAG,
        TAG,
        "fetchMoreMessage:" + anchor.getContent() + anchor.getTime() + " direction:" + direction);

    GetMessagesDynamicallyParam dynamicallyParam =
        new GetMessagesDynamicallyParam(mSessionId, mSessionType);
    dynamicallyParam.setLimit(messagePageSize);
    dynamicallyParam.setDirection(direction);
    if (anchor != null) {
      dynamicallyParam.setAnchorServerId(anchor.getServerId());
      dynamicallyParam.setAnchorClientId(anchor.getUuid());
    }
    if (direction == GetMessageDirectionEnum.FORWARD) {
      dynamicallyParam.setToTime(anchor.getTime());
    } else {
      dynamicallyParam.setFromTime(anchor.getTime());
    }

    ChatRepo.getMessagesDynamically(
        dynamicallyParam,
        new FetchCallback<MessageDynamicallyResult>() {
          @Override
          public void onSuccess(@Nullable MessageDynamicallyResult result) {
            if (result != null && result.getMessageList() != null) {
              if (direction == GetMessageDirectionEnum.FORWARD) {
                Collections.reverse(result.getMessageList());
              }
              ALog.d(LIB_TAG, TAG, "fetchMoreMessage,reverse:" + result.getMessageList().size());
              onListFetchSuccess(result.getMessageList(), direction);
            }
          }

          @Override
          public void onFailed(int code) {
            onListFetchFailed(code);
            ALog.d(LIB_TAG, TAG, "fetchMoreMessage:" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            onListFetchFailed(ChatConstants.ERROR_CODE_FETCH_MSG);
          }
        });
  }

  public void fetchMessageListBothDirect(IMMessage anchor) {
    ALog.d(LIB_TAG, TAG, "fetchMessageListBothDirect");
    fetchMoreMessage(anchor, GetMessageDirectionEnum.FORWARD);
    fetchMoreMessage(anchor, GetMessageDirectionEnum.BACKWARD);
  }

  private void onListFetchSuccess(List<IMMessageInfo> param, GetMessageDirectionEnum direction) {
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
    messageFetchResult.setTypeIndex(direction == GetMessageDirectionEnum.FORWARD ? 0 : -1);
    messageLiveData.setValue(messageFetchResult);
  }

  private void onListFetchFailed(int code) {
    ALog.d(LIB_TAG, TAG, "onListFetchFailed code:" + code);
    messageFetchResult.setError(code, R.string.chat_message_fetch_error);
    messageFetchResult.setData(null);
    messageFetchResult.setTypeIndex(-1);
    messageLiveData.setValue(messageFetchResult);
  }

  private List<ChatMessageBean> convert(List<IMMessageInfo> messageList) {
    if (messageList == null) {
      return null;
    }
    ArrayList<ChatMessageBean> result = new ArrayList<>(messageList.size());
    for (IMMessageInfo message : messageList) {
      if (mIsTeamGroup && message.getMessage().getAttachment() instanceof NotificationAttachment) {
        NotificationAttachment attachment =
            (NotificationAttachment) message.getMessage().getAttachment();
        if (attachment.getType() == NotificationType.TransferOwner) {
          continue;
        }
      }
      result.add(new ChatMessageBean(message));
    }
    return result;
  }

  //**********reply message**************
  public void replyMessage(IMMessage message, IMMessage replyMsg, boolean resend) {
    ALog.d(LIB_TAG, TAG, "replyMessage,message" + (message == null ? "null" : message.getUuid()));
    message.setThreadOption(replyMsg);
    message.setMsgAck();
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
