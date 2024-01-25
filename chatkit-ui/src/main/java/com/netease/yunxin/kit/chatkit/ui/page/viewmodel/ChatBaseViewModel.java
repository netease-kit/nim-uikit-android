// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.CollectInfo;
import com.netease.nimlib.sdk.msg.model.GetMessageDirectionEnum;
import com.netease.nimlib.sdk.msg.model.GetMessagesDynamicallyParam;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MsgPinOption;
import com.netease.nimlib.sdk.msg.model.MsgPinSyncResponseOption;
import com.netease.nimlib.sdk.msg.model.MsgPinSyncResponseOptionWrapper;
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.map.ChatLocationBean;
import com.netease.yunxin.kit.chatkit.media.ImageUtil;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.MessageDynamicallyResult;
import com.netease.yunxin.kit.chatkit.repo.ChatObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.ContactObserverRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatCallback;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatConfigManager;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.model.AnchorScrollInfo;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.PinEvent;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitService;
import com.netease.yunxin.kit.chatkit.utils.SendMediaHelper;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.EncryptUtils;
import com.netease.yunxin.kit.common.utils.FileUtils;
import com.netease.yunxin.kit.common.utils.ImageUtils;
import com.netease.yunxin.kit.common.utils.UriUtils;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallbackImpl;
import com.netease.yunxin.kit.corekit.im.provider.UserInfoObserver;
import com.netease.yunxin.kit.corekit.im.repo.CommonRepo;
import com.netease.yunxin.kit.corekit.im.repo.SettingRepo;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** chat info view model fetch and send messages for chat page */
public abstract class ChatBaseViewModel extends BaseViewModel {
  public static final String TAG = "ChatViewModel";
  private static final int RES_IN_BLACK_LIST = 7101;
  // 拉取历史消息
  private final MutableLiveData<FetchResult<List<ChatMessageBean>>> messageLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ChatMessageBean>> messageFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  // 接受消息
  private final MutableLiveData<FetchResult<List<ChatMessageBean>>> messageRecLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ChatMessageBean>> messageRecFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final MutableLiveData<FetchResult<List<String>>> userInfoLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<String>> userInfoFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final MutableLiveData<FetchResult<Map<String, MsgPinOption>>> msgPinLiveData =
      new MutableLiveData<>();
  private final FetchResult<Map<String, MsgPinOption>> msgPinFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final MutableLiveData<FetchResult<ChatMessageBean>> sendMessageLiveData =
      new MutableLiveData<>();
  private final FetchResult<ChatMessageBean> sendMessageFetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final MutableLiveData<FetchResult<AttachmentProgress>> attachmentProgressMutableLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<ChatMessageBean>> revokeMessageLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<List<ChatMessageBean>>> deleteMessageLiveData =
      new MutableLiveData<>();

  protected String mSessionId;
  private SessionTypeEnum mSessionType;
  protected boolean mIsTeamGroup = false;
  protected boolean needACK = false;
  protected boolean showRead = true;
  protected boolean hasLoadMessage = false;

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

  private final EventObserver<IMMessageInfo> deleteMsgObserver =
      new EventObserver<IMMessageInfo>() {
        @Override
        public void onEvent(@Nullable IMMessageInfo event) {
          ALog.d(
              LIB_TAG,
              TAG,
              "msg delete -->> " + (event == null ? "null" : event.getMessage().getUuid()));
          if (event != null && TextUtils.equals(event.getMessage().getSessionId(), mSessionId)) {
            ChatMessageBean messageBean =
                new ChatMessageBean(new IMMessageInfo(event.getMessage()));
            FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Success);
            result.setData(Collections.singletonList(messageBean));
            result.setType(FetchResult.FetchType.Remove);
            result.setTypeIndex(-1);
            deleteMessageLiveData.setValue(result);
          }
        }
      };

  private final EventObserver<List<IMMessageInfo>> deleteMsgBatchObserver =
      new EventObserver<List<IMMessageInfo>>() {
        @Override
        public void onEvent(@Nullable List<IMMessageInfo> event) {
          ALog.d(LIB_TAG, TAG, "msg delete batch -->> " + (event == null ? "null" : event.size()));
          if (event != null) {
            ArrayList<ChatMessageBean> deleteList = new ArrayList<>();
            for (IMMessageInfo messageInfo : event) {
              FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Success);
              if (TextUtils.equals(messageInfo.getMessage().getSessionId(), mSessionId)) {
                ChatMessageBean messageBean =
                    new ChatMessageBean(new IMMessageInfo(messageInfo.getMessage()));
                deleteList.add(messageBean);
              }
              if (deleteList.size() > 0) {
                result.setData(deleteList);
                result.setType(FetchResult.FetchType.Remove);
                result.setTypeIndex(-1);
                deleteMessageLiveData.setValue(result);
              }
            }
          }
        }
      };

  // 他人撤回消息底层会收到通知进行处理，并保存到本地。当前账号的撤回需要自行处理
  private final Observer<RevokeMsgNotification> revokeMsgObserver =
      revokeMsgNotification -> {
        ALog.d(LIB_TAG, TAG, "revokeMsgObserver");
        ChatMessageBean messageBean =
            new ChatMessageBean(new IMMessageInfo(revokeMsgNotification.getMessage()));
        if (ChatConfigManager.enableInsertLocalMsgWhenRevoke) {
          messageBean.revokeMsgEdit = false;
          FetchResult<ChatMessageBean> fetchResult = new FetchResult<>(LoadStatus.Success);
          fetchResult.setData(messageBean);
          revokeMessageLiveData.setValue(fetchResult);
        } else {
          FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Success);
          result.setData(Collections.singletonList(messageBean));
          result.setType(FetchResult.FetchType.Remove);
          result.setTypeIndex(-1);
          deleteMessageLiveData.setValue(result);
        }
      };

  private final UserInfoObserver userInfoObserver =
      userList -> {
        ALog.d(LIB_TAG, TAG, "UserInfoObserver:" + userList.size());
        ChatUserCache.addUserInfo(userList);
        List<String> accountList = new ArrayList<>();
        for (UserInfo userInfo : userList) {
          accountList.add(userInfo.getAccount());
        }
        userInfoFetchResult.setLoadStatus(LoadStatus.Finish);
        userInfoFetchResult.setData(accountList);
        userInfoFetchResult.setType(FetchResult.FetchType.Update);
        userInfoLiveData.setValue(userInfoFetchResult);
      };

  private final Observer<FriendChangedNotify> friendChangedObserver =
      friendChangedNotify -> {
        if (friendChangedNotify != null) {
          ALog.d(LIB_TAG, TAG, "friendChangedObserver");
          List<FriendInfo> friendList = new ArrayList<>();
          List<String> accountList = new ArrayList<>();
          for (Friend friend : friendChangedNotify.getAddedOrUpdatedFriends()) {
            friendList.add(new FriendInfo(friend));
            accountList.add(friend.getAccount());
            ALog.d(
                LIB_TAG, TAG, "friendChangedObserver,AddedOrUpdatedFriends:" + friend.getAccount());
          }
          ChatUserCache.addFriendInfo(friendList);
          List<FriendInfo> friendDeleteList = new ArrayList<>();
          if (friendChangedNotify.getDeletedFriends() != null) {
            for (String account : friendChangedNotify.getDeletedFriends()) {
              friendDeleteList.add(new FriendInfo(account, null, null));
              ALog.d(LIB_TAG, TAG, "friendChangedObserver,DeletedFriends:" + account);
            }
            ChatUserCache.addFriendInfo(friendDeleteList);
            accountList.addAll(friendChangedNotify.getDeletedFriends());
          }
          notifyFriendChange(friendChangedNotify);
          userInfoFetchResult.setLoadStatus(LoadStatus.Finish);
          userInfoFetchResult.setData(accountList);
          userInfoFetchResult.setType(FetchResult.FetchType.Update);
          userInfoLiveData.setValue(userInfoFetchResult);
        }
      };

  private final EventNotify<PinEvent> localPin =
      new EventNotify<PinEvent>() {
        @Override
        public void onNotify(@NonNull PinEvent event) {
          ALog.d(LIB_TAG, TAG, "removeMsgPin,onSuccess" + event.msgUuid);
          if (event.isRemove) {
            removePinMessageLiveData.setValue(event.msgUuid);
          }
        }

        @NonNull
        @Override
        public String getEventType() {
          return "PinEvent";
        }
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

  public MutableLiveData<FetchResult<List<String>>> getUserInfoLiveData() {
    return userInfoLiveData;
  }

  public MutableLiveData<FetchResult<Map<String, MsgPinOption>>> getMsgPinLiveData() {
    return msgPinLiveData;
  }

  public MutableLiveData<FetchResult<List<ChatMessageBean>>> getDeleteMessageLiveData() {
    return deleteMessageLiveData;
  }

  public void deleteMessage(ChatMessageBean messageBean) {
    deleteMessage(Collections.singletonList(messageBean));
  }

  public void deleteMessage(List<ChatMessageBean> messageList) {
    if (messageList == null || messageList.isEmpty()) {
      return;
    }
    List<IMMessageInfo> deleteList = new ArrayList<>();
    for (ChatMessageBean messageBean : messageList) {
      if (messageBean.getMessageData().getMessage().getServerId() == 0L
          || messageBean.getMessageData().getMessage().getStatus() == MsgStatusEnum.fail) {
        ChatRepo.deleteMessageLocal(messageBean.getMessageData());
      } else {
        deleteList.add(messageBean.getMessageData());
      }
    }
    if (deleteList.size() < 1) {
      doActionAfterDelete(messageList);
      return;
    }
    ChatRepo.deleteMessage(
        deleteList,
        null,
        new FetchCallbackImpl<Long>() {
          @Override
          public void onSuccess(@Nullable Long param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "deleteMessage,onSuccess:" + String.valueOf(param != null ? param : 0));
            doActionAfterDelete(messageList);
          }

          @Override
          public void onFailed(int code) {

            FetchResult<List<ChatMessageBean>> fetchResult = new FetchResult<>(LoadStatus.Error);
            fetchResult.setError(-1, R.string.chat_message_delete_error);
            deleteMessageLiveData.setValue(fetchResult);
            ALog.d(LIB_TAG, TAG, "deleteMessage,onFailed:" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            FetchResult<List<ChatMessageBean>> fetchResult = new FetchResult<>(LoadStatus.Error);
            fetchResult.setError(-1, R.string.chat_message_delete_error);
            deleteMessageLiveData.setValue(fetchResult);
            ALog.e(LIB_TAG, TAG, "deleteMessage,onException");
          }
        });
  }

  private void doActionAfterDelete(List<ChatMessageBean> messageBean) {
    FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Success);
    result.setData(messageBean);
    result.setType(FetchResult.FetchType.Remove);
    result.setTypeIndex(-1);
    deleteMessageLiveData.setValue(result);
    ALog.d(LIB_TAG, TAG, "deleteMessage, onSuccess");
  }

  public void notifyFriendChange(FriendChangedNotify friendChangedNotify) {}

  public void revokeMessage(ChatMessageBean messageBean) {
    if (messageBean != null && messageBean.getMessageData() != null) {
      ALog.d(LIB_TAG, TAG, "revokeMessage " + messageBean.getMessageData().getMessage().getUuid());
      ChatRepo.revokeMessage(
          messageBean.getMessageData(),
          new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
              if (!TextUtils.isEmpty(messageBean.getPinAccid())) {
                ChatRepo.removeMessagePin(messageBean.getMessageData().getMessage(), null);
              }

              if (ChatConfigManager.enableInsertLocalMsgWhenRevoke) {
                FetchResult<ChatMessageBean> fetchResult = new FetchResult<>(LoadStatus.Success);
                fetchResult.setData(messageBean);
                // 他人撤回消息底层会收到通知进行处理，并保存到本地。当前账号的撤回需要自行处理
                MessageHelper.saveLocalRevokeMessage(
                    messageBean.getMessageData().getMessage(), true);
                revokeMessageLiveData.setValue(fetchResult);
              } else {
                FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Success);
                result.setData(Collections.singletonList(messageBean));
                result.setType(FetchResult.FetchType.Remove);
                result.setTypeIndex(-1);
                deleteMessageLiveData.setValue(result);
              }

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
              revokeMessageLiveData.setValue(fetchResult);
              ALog.d(LIB_TAG, TAG, "revokeMessage,onFailed:" + code);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              FetchResult<ChatMessageBean> fetchResult = new FetchResult<>(LoadStatus.Error);
              fetchResult.setError(-1, R.string.chat_message_revoke_error);
              revokeMessageLiveData.setValue(fetchResult);
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
    SettingRepo.getShowReadStatus(
        new FetchCallbackImpl<Boolean>() {
          @Override
          public void onSuccess(@Nullable Boolean param) {
            needACK = param;
          }
        });
  }

  public void setChattingAccount() {
    ALog.d(LIB_TAG, TAG, "setChattingAccount sessionId:" + mSessionId);
    ChatRepo.setChattingAccount(mSessionId, mSessionType);
    AitService.getInstance().clearAitInfo(mSessionId);
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
    ContactObserverRepo.registerUserInfoObserver(userInfoObserver);
    ChatObserverRepo.registerAddMessagePinObserve(msgPinAddObserver);
    ChatObserverRepo.registerRemoveMessagePinObserve(msgPinRemoveObserver);
    ContactObserverRepo.registerFriendInfoUpdateObserver(friendChangedObserver);
    ChatObserverRepo.registerDeleteMsgSelfObserve(deleteMsgObserver);
    ChatObserverRepo.registerDeleteMsgSelfBatchObserve(deleteMsgBatchObserver);
    EventCenter.registerEventNotify(localPin);
  }

  public void unregisterObservers() {
    ALog.d(LIB_TAG, TAG, "unregisterObservers ");
    ChatUserCache.clear();
    ChatObserverRepo.unregisterReceiveMessageObserve(mSessionId, receiveMessageObserver);
    ChatObserverRepo.unregisterMsgStatusObserve(msgStatusObserver);
    ChatObserverRepo.unregisterAttachmentProgressObserve(attachmentProgressObserver);
    ChatObserverRepo.unregisterMessageSendingObserve(mSessionId, msgSendingObserver);
    ChatObserverRepo.unregisterRevokeMessageObserve(revokeMsgObserver);
    ChatRepo.unregisterUserInfoObserver(userInfoObserver);
    ChatObserverRepo.unregisterAddMessagePinObserve(msgPinAddObserver);
    ChatObserverRepo.unregisterRemoveMessagePinObserve(msgPinRemoveObserver);
    ContactObserverRepo.unregisterFriendInfoUpdateObserver(friendChangedObserver);
    ChatObserverRepo.unregisterDeleteMsgSelfObserve(deleteMsgObserver);
    ChatObserverRepo.unregisterDeleteMsgSelfBatchObserve(deleteMsgBatchObserver);
    EventCenter.unregisterEventNotify(localPin);
  }

  public void sendTextMessage(String content, List<String> pushList) {
    ALog.d(LIB_TAG, TAG, "sendTextMessage:" + (content != null ? content.length() : "null"));
    sendTextMessage(content, pushList, null);
  }

  public void sendTextMessage(String content, String session, SessionTypeEnum sessionType) {
    ALog.d(LIB_TAG, TAG, "sendTextMessage:" + (content != null ? content.length() : "null"));
    IMMessage textMsg = MessageBuilder.createTextMessage(session, sessionType, content);
    sendMessage(textMsg, false, true);
  }

  public void sendRichTextMessage(
      String title, String content, List<String> pushList, Map<String, Object> remoteExtension) {
    ALog.d(LIB_TAG, TAG, "sendTextMessage:" + (content != null ? content.length() : "null"));
    RichTextAttachment attachment = new RichTextAttachment();
    attachment.body = content;
    attachment.title = title;
    IMMessage customMsg = MessageBuilder.createCustomMessage(mSessionId, mSessionType, attachment);
    MessageHelper.appendTeamMemberPush(customMsg, pushList);
    if (remoteExtension != null) {
      customMsg.setRemoteExtension(remoteExtension);
    }
    sendMessage(customMsg, false, true);
  }

  public void sendTextMessage(
      String content, List<String> pushList, Map<String, Object> remoteExtension) {
    ALog.d(LIB_TAG, TAG, "sendTextMessage:" + (content != null ? content.length() : "null"));
    IMMessage textMsg = MessageBuilder.createTextMessage(mSessionId, mSessionType, content);
    MessageHelper.appendTeamMemberPush(textMsg, pushList);
    if (remoteExtension != null) {
      textMsg.setRemoteExtension(remoteExtension);
    }
    sendMessage(textMsg, false, true);
  }

  public void addMsgCollection(IMMessageInfo messageInfo) {
    if (messageInfo == null) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "addMsgCollection:" + messageInfo.getMessage().getUuid());
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
      int[] bounds = ImageUtils.getSize(imageFile);
      IMMessage imageMsg = MessageBuilder.createImageMessage(mSessionId, mSessionType, imageFile);
      ImageAttachment attachment = (ImageAttachment) imageMsg.getAttachment();
      attachment.setWidth(bounds[0]);
      attachment.setHeight(bounds[1]);
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

  public void sendForwardMessage(
      ChatMessageBean message, String inputMsg, String sessionId, SessionTypeEnum sessionType) {
    ALog.d(LIB_TAG, TAG, "sendForwardMessage:" + sessionId);
    if (!message.isRevoked()) {
      IMMessage forwardMessage =
          MessageBuilder.createForwardMessage(
              message.getMessageData().getMessage(), sessionId, sessionType);
      MessageHelper.clearAitAndReplyInfo(forwardMessage);
      sendMessage(forwardMessage, false, TextUtils.equals(sessionId, mSessionId));
    }

    if (!TextUtils.isEmpty(inputMsg) && TextUtils.getTrimmedLength(inputMsg) > 0) {
      new Handler(Looper.getMainLooper())
          .postDelayed(() -> sendTextMessage(inputMsg, sessionId, sessionType), 500);
    }
  }

  public void sendForwardMessages(
      String displayName,
      String inputMsg,
      List<String> sessionInfo,
      SessionTypeEnum sessionType,
      List<ChatMessageBean> messages) {
    if (sessionInfo == null || sessionInfo.isEmpty() || messages == null || messages.isEmpty()) {
      return;
    }

    boolean hasError = false;

    // 合并转发消息需要逆序的，所以获取消息列表是逆序。逐条转发需要正序，所以要倒序遍历
    for (int index = messages.size() - 1; index >= 0; index--) {
      ChatMessageBean message = messages.get(index);
      if (message.isRevoked()) {
        continue;
      }
      if (message.getMessageData().getMessage().getMsgType() == MsgTypeEnum.audio) {
        hasError = true;
        continue;
      }
      for (String session : sessionInfo) {
        IMMessage forwardMessage =
            MessageBuilder.createForwardMessage(
                message.getMessageData().getMessage(), session, sessionType);
        MessageHelper.clearAitAndReplyInfo(forwardMessage);
        sendMessage(forwardMessage, false, TextUtils.equals(session, mSessionId));
      }
    }
    if (hasError) {
      ToastX.showLongToast(R.string.msg_multi_forward_error_tips);
    }

    if (!TextUtils.isEmpty(inputMsg) && TextUtils.getTrimmedLength(inputMsg) > 0) {
      // 保证留言消息在最后，所以延迟发送
      new Handler(Looper.getMainLooper())
          .postDelayed(
              () -> {
                for (String session : sessionInfo) {
                  sendTextMessage(inputMsg, session, sessionType);
                }
              },
              500);
    }
  }

  public void sendMultiForwardMessage(
      String displayName,
      String inputMsg,
      List<String> sessionInfo,
      SessionTypeEnum sessionType,
      List<ChatMessageBean> messages) {
    ALog.d(LIB_TAG, TAG, "sendMultiForwardMessage");
    if (sessionInfo == null || sessionInfo.isEmpty() || messages == null || messages.isEmpty()) {
      return;
    }

    List<IMMessageInfo> iMessageList = new ArrayList<>();
    for (ChatMessageBean message : messages) {
      iMessageList.add(message.getMessageData());
    }

    String msgInfo = MessageHelper.createMultiForwardMsg(iMessageList);
    try {

      File localFile = SendMediaHelper.createTextFile();
      CommonRepo.writeLocalFileAndUploadNOS(
          localFile,
          msgInfo,
          "text",
          new FetchCallback<String>() {
            @Override
            public void onSuccess(@Nullable String param) {
              if (param != null) {
                String fileMD5 = EncryptUtils.md5(localFile);
                MultiForwardAttachment attachment =
                    MessageHelper.createMultiTransmitAttachment(
                        displayName, mSessionId, param, iMessageList);
                attachment.md5 = fileMD5;
                for (String session : sessionInfo) {
                  IMMessage textMsg =
                      MessageBuilder.createCustomMessage(session, sessionType, attachment);
                  sendMessage(textMsg, false, true);
                  if (!TextUtils.isEmpty(inputMsg)) {
                    sendTextMessage(inputMsg, session, sessionType);
                  }
                }
              }
            }

            @Override
            public void onFailed(int code) {}

            @Override
            public void onException(@Nullable Throwable exception) {}
          });

    } catch (IOException e) {

    }
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
      SendMediaHelper.handleImage(uri, true, this::sendImageMessage);
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
              ALog.d(
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
              try {
                mmr.release();
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          });
    } else {
      ToastX.showShortToast(R.string.chat_message_type_not_support_tips);
      ALog.d(LIB_TAG, TAG, "invalid file type");
    }
  }

  public void sendFile(Uri uri) {
    ALog.d(LIB_TAG, TAG, "sendFile:" + (uri != null ? uri.getPath() : "uri is null"));
    if (uri == null) {
      return;
    }
    SendMediaHelper.handleFile(
        uri,
        file -> {
          try {
            String displayName = ChatUtils.getUrlFileName(IMKitClient.getApplicationContext(), uri);
            sendFileMessage(file, displayName);
          } catch (Exception e) {
            e.printStackTrace();
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
      ALog.d(LIB_TAG, TAG, "sendMessage:" + message.getUuid() + "needACK:" + needACK);
      if (needACK && showRead) {
        message.setMsgAck();
      }
      ChatRepo.sendMessage(
          message,
          resend,
          new FetchCallbackImpl<Void>() {

            @Override
            public void onFailed(int code) {
              if (code == RES_IN_BLACK_LIST) {
                MessageHelper.saveLocalBlackTipMessageAndNotify(message);
              }
            }
          });
    }
  }

  public void sendMessage(IMMessage message) {
    if (message != null) {
      sendMessage(message, true, true);
    }
  }

  public abstract void sendReceipt(IMMessage message);

  public void initFetch(IMMessage anchor, boolean needToScrollEnd) {
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
                if (result.getMessageList() != null) {
                  Collections.reverse(result.getMessageList());
                }
                fetchPinInfo();
                onListFetchSuccess(result.getMessageList(), GetMessageDirectionEnum.FORWARD);
              }
              if (!hasLoadMessage) {
                hasLoadMessage = true;
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
      fetchMessageListBothDirect(anchor, needToScrollEnd);
    }
  }

  /** called when entering the chat page */
  public void initFetch(IMMessage anchor) {
    initFetch(anchor, true);
  }

  public void fetchPinInfo() {
    ChatRepo.fetchPinInfo(
        mSessionId,
        mSessionType,
        0L,
        new FetchCallback<MsgPinSyncResponseOptionWrapper>() {
          @Override
          public void onSuccess(@Nullable MsgPinSyncResponseOptionWrapper param) {
            ALog.d(LIB_TAG, TAG, "initFetch:fetchPinInfo:onSuccess");
            if (param != null && param.isChanged()) {
              ALog.d(
                  LIB_TAG,
                  TAG,
                  "initFetch:fetchPinInfo:onSuccess:hasChange:"
                      + param.isChanged()
                      + ",size:"
                      + param.getMsgPinInfoList().size());
              Map<String, MsgPinOption> pinInfoMap = new HashMap<>();
              for (MsgPinSyncResponseOption option : param.getMsgPinInfoList()) {
                pinInfoMap.put(option.getKey().getUuid(), option.getPinOption());
                ALog.d(LIB_TAG, TAG, "initFetch:fetchPinInfo:onSuccess");
              }

              msgPinFetchResult.setLoadStatus(LoadStatus.Finish);
              msgPinFetchResult.setFetchType(FetchResult.FetchType.Update);
              msgPinFetchResult.setData(pinInfoMap);
              msgPinLiveData.setValue(msgPinFetchResult);
            }
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "initFetch:fetchPinInfo:onFailed" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "initFetch:fetchPinInfo:onException");
          }
        });
  }

  public void fetchMoreMessage(
      IMMessage anchor, GetMessageDirectionEnum direction, boolean needToScrollEnd) {
    ALog.d(LIB_TAG, TAG, "fetchMoreMessage:" + " direction:" + direction);

    GetMessagesDynamicallyParam dynamicallyParam =
        new GetMessagesDynamicallyParam(mSessionId, mSessionType);
    dynamicallyParam.setLimit(messagePageSize);
    dynamicallyParam.setDirection(direction);
    if (anchor != null) {
      dynamicallyParam.setAnchorServerId(anchor.getServerId());
      dynamicallyParam.setAnchorClientId(anchor.getUuid());
      if (direction == GetMessageDirectionEnum.FORWARD) {
        dynamicallyParam.setToTime(anchor.getTime());
      } else {
        dynamicallyParam.setFromTime(anchor.getTime());
      }
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
              onListFetchSuccess(anchor, needToScrollEnd, result.getMessageList(), direction);
            }
          }

          @Override
          public void onFailed(int code) {
            onListFetchFailed(code);
            ALog.d(LIB_TAG, TAG, "fetchMoreMessage:" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            onListFetchFailed(ChatKitUIConstant.ERROR_CODE_FETCH_MSG);
          }
        });
  }

  public void fetchMoreMessage(IMMessage anchor, GetMessageDirectionEnum direction) {
    fetchMoreMessage(anchor, direction, true);
  }

  public void fetchMessageListBothDirect(IMMessage anchor) {
    fetchMessageListBothDirect(anchor, true);
  }

  public void fetchMessageListBothDirect(IMMessage anchor, boolean needToScrollEnd) {
    ALog.d(LIB_TAG, TAG, "fetchMessageListBothDirect");
    // 此处避免在获取 anchor 消息后被之前消息添加导致ui移位，因此将 anchor 之前消息请求添加到后续的主线程事件队列中
    new Handler(Looper.getMainLooper())
        .post(() -> fetchMoreMessage(anchor, GetMessageDirectionEnum.FORWARD, needToScrollEnd));
    fetchMoreMessage(anchor, GetMessageDirectionEnum.BACKWARD, needToScrollEnd);
  }

  private void onListFetchSuccess(List<IMMessageInfo> param, GetMessageDirectionEnum direction) {
    onListFetchSuccess(null, true, param, direction);
  }

  private void onListFetchSuccess(
      IMMessage anchorMsg,
      boolean needToScrollEnd,
      List<IMMessageInfo> param,
      GetMessageDirectionEnum direction) {
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
    if (anchorMsg != null && !needToScrollEnd) {
      messageFetchResult.setExtraInfo(new AnchorScrollInfo(anchorMsg));
    }
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

  // **********reply message**************
  public void replyMessage(IMMessage message, IMMessage replyMsg, boolean resend) {
    ALog.d(LIB_TAG, TAG, "replyMessage,message" + (message == null ? "null" : message.getUuid()));
    if (message == null) {
      return;
    }
    Map<String, Object> remote =
        MessageHelper.createReplyExtension(message.getRemoteExtension(), replyMsg);
    message.setRemoteExtension(remote);
    sendMessage(message, resend, true);
  }

  public void replyTextMessage(
      String content,
      IMMessage message,
      List<String> pushList,
      Map<String, Object> remoteExtension) {
    ALog.d(
        LIB_TAG, TAG, "replyTextMessage,message" + (message == null ? "null" : message.getUuid()));
    IMMessage textMsg = MessageBuilder.createTextMessage(mSessionId, mSessionType, content);
    MessageHelper.appendTeamMemberPush(textMsg, pushList);
    if (remoteExtension != null) {
      textMsg.setRemoteExtension(remoteExtension);
    }
    replyMessage(textMsg, message, false);
  }

  // ********************Message Pin********************

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
        addPinMessageLiveData.setValue(pinInfo);
      };

  private final Observer<MsgPinSyncResponseOption> msgPinRemoveObserver =
      responseOption -> removePinMessageLiveData.setValue(responseOption.getKey().getUuid());

  public void addMessagePin(IMMessageInfo messageInfo, String ext) {
    if (messageInfo == null) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "addMessagePin,message" + messageInfo.getMessage().getUuid());

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
            addPinMessageLiveData.setValue(pinInfo);
          }
        });
  }

  public void removeMsgPin(IMMessageInfo messageInfo) {
    if (messageInfo == null) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "removeMsgPin,message" + messageInfo.getMessage().getUuid());
    ChatRepo.removeMessagePin(
        messageInfo.getMessage(),
        new ChatCallback<Long>() {
          @Override
          public void onSuccess(@Nullable Long param) {
            super.onSuccess(param);
            ALog.d(LIB_TAG, TAG, "removeMsgPin,onSuccess" + param);
            removePinMessageLiveData.setValue(messageInfo.getMessage().getUuid());
          }
        });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    unregisterObservers();
  }
}
