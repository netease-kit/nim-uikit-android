// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncState;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncType;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMP2PMessageReadReceipt;
import com.netease.nimlib.sdk.v2.notification.V2NIMBroadcastNotification;
import com.netease.nimlib.sdk.v2.notification.V2NIMCustomNotification;
import com.netease.nimlib.sdk.v2.notification.V2NIMNotificationListener;
import com.netease.nimlib.sdk.v2.notification.config.V2NIMNotificationConfig;
import com.netease.nimlib.sdk.v2.notification.config.V2NIMNotificationPushConfig;
import com.netease.nimlib.sdk.v2.notification.config.V2NIMNotificationRouteConfig;
import com.netease.nimlib.sdk.v2.notification.params.V2NIMSendCustomNotificationParams;
import com.netease.nimlib.sdk.v2.subscription.V2NIMSubscribeListener;
import com.netease.nimlib.sdk.v2.subscription.model.V2NIMUserStatus;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.OnlineStatusManager;
import com.netease.yunxin.kit.chatkit.impl.LoginDetailListenerImpl;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.listener.ContactChangeType;
import com.netease.yunxin.kit.corekit.im2.listener.ContactListener;
import com.netease.yunxin.kit.corekit.im2.model.FriendAddApplicationInfo;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

/** P2P chat info view model message receipt, type state for P2P chat page */
public class ChatP2PViewModel extends ChatBaseViewModel {

  private static final String TAG = "ChatP2PViewModel";

  private static final String TYPE_STATE = "typing";

  private long receiptTime = 0L;

  private final MutableLiveData<V2NIMP2PMessageReadReceipt> messageReceiptLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<Boolean> typeStateLiveData = new MutableLiveData<>();

  //用户信息数据
  private final MutableLiveData<FetchResult<UserWithFriend>> friendInfoLiveData =
      new MutableLiveData<>();

  private final FetchResult<UserWithFriend> friendInfoFetchResult =
      new FetchResult<>(LoadStatus.Finish);

  // 更新在线状态
  private final MutableLiveData<FetchResult<String>> updateLiveData = new MutableLiveData<>();

  // 正在输入功能监听，监听通知消息来展示对方是否正在输入
  private final V2NIMNotificationListener notificationListener =
      new V2NIMNotificationListener() {
        @Override
        public void onReceiveCustomNotifications(
            List<V2NIMCustomNotification> customNotifications) {
          ALog.d(
              LIB_TAG,
              TAG,
              "mcustomNotificationObserver:"
                  + (customNotifications == null ? "null" : customNotifications.size()));
          if (customNotifications == null) {
            return;
          }
          for (V2NIMCustomNotification notification : customNotifications) {
            if (!TextUtils.equals(notification.getReceiverId(), IMKitClient.account())
                || !TextUtils.equals(notification.getSenderId(), mChatAccountId)
                || notification.getConversationType()
                    != V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
              return;
            }
            String content = notification.getContent();
            try {
              JSONObject json = new JSONObject(content);
              int id = json.getInt(TYPE_STATE);
              if (id == 1) {
                typeStateLiveData.postValue(true);
              } else {
                typeStateLiveData.postValue(false);
              }
            } catch (JSONException e) {
              ALog.e(TAG, e.getMessage());
            }
          }
        }

        @Override
        public void onReceiveBroadcastNotifications(
            List<V2NIMBroadcastNotification> broadcastNotifications) {
          //do nothing
        }
      };

  protected V2NIMSubscribeListener onlineListener =
      userStatusList -> {
        if (userStatusList == null || userStatusList.isEmpty()) {
          return;
        }
        String userId = "";
        for (V2NIMUserStatus userStatus : userStatusList) {
          if (userStatus != null && Objects.equals(userStatus.getAccountId(), mChatAccountId)) {
            userId = userStatus.getAccountId();
            break;
          }
        }
        if (!userId.isEmpty()) {
          FetchResult<String> updateResult = new FetchResult<>(FetchResult.FetchType.Update);
          updateResult.setData(userId);
          updateResult.setLoadStatus(LoadStatus.Finish);
          updateLiveData.setValue(updateResult);
        }
      };

  // 好友信息变更监听
  private final ContactListener friendListener =
      new ContactListener() {
        @Override
        public void onContactChange(
            @NonNull ContactChangeType changeType,
            @NonNull List<? extends UserWithFriend> contactList) {

          List<UserWithFriend> needFriendList = new ArrayList<>();
          List<String> accountList = new ArrayList<>();
          for (UserWithFriend friendInfo : contactList) {
            if (friendInfo != null
                && (TextUtils.equals(friendInfo.getAccount(), mChatAccountId)
                    || TextUtils.equals(friendInfo.getAccount(), IMKitClient.account()))) {
              needFriendList.add(friendInfo);
              accountList.add(friendInfo.getAccount());
            }
          }

          if (!needFriendList.isEmpty()) {
            FetchResult<List<String>> userInfoFetchResult = new FetchResult<>(LoadStatus.Finish);
            userInfoFetchResult.setData(accountList);
            userInfoFetchResult.setType(FetchResult.FetchType.Update);
            userChangeLiveData.setValue(userInfoFetchResult);
          }
          if (changeType == ContactChangeType.Update) {
            for (UserWithFriend friend : contactList) {
              if (friend.getAccount().equals(mChatAccountId)) {
                notifyFriendChange(friend);
              }
            }
          }
        }

        @Override
        public void onFriendAddApplication(@NonNull FriendAddApplicationInfo friendApplication) {}

        @Override
        public void onFriendAddRejected(@NonNull FriendAddApplicationInfo rejectionInfo) {}
      };

  private final LoginDetailListenerImpl loginDetailListener =
      new LoginDetailListenerImpl() {
        @Override
        public void onDataSync(V2NIMDataSyncType type, V2NIMDataSyncState state, V2NIMError error) {
          ALog.d(LIB_TAG, TAG, "onDataSync:" + type + "," + state);
          if (type == V2NIMDataSyncType.V2NIM_DATA_SYNC_MAIN
              && state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED) {
            subscribeOnlineStatus(true);
          }
        }
      };

  /** chat message read receipt live data */
  public MutableLiveData<V2NIMP2PMessageReadReceipt> getMessageReceiptLiveData() {
    return messageReceiptLiveData;
  }

  /** chat typing state live data */
  public MutableLiveData<Boolean> getTypeStateLiveData() {
    return typeStateLiveData;
  }

  public MutableLiveData<FetchResult<UserWithFriend>> getFriendInfoLiveData() {
    return friendInfoLiveData;
  }

  public MutableLiveData<FetchResult<String>> getUpdateLiveData() {
    return updateLiveData;
  }

  public void getP2PData(V2NIMMessage anchorMessage) {
    getFriendInfo(mChatAccountId);
    getMessageList(anchorMessage, false);
    getP2PMessageReadReceipts();
    subscribeOnlineStatus(false);
  }

  private void subscribeOnlineStatus(boolean needRefresh) {
    if (IMKitConfigCenter.getEnableOnlineStatus()
        && !AIUserManager.isAIUser(mChatAccountId)
        && !OnlineStatusManager.isOnlineSubscribe(mChatAccountId)) {
      List<String> accountList = new ArrayList<>();
      accountList.add(mChatAccountId);
      OnlineStatusManager.subscribeUserStatus(accountList);
    }
    // 重新订阅移除当前状态重新刷新
    if (needRefresh) {
      FetchResult<String> updateResult = new FetchResult<>(FetchResult.FetchType.Update);
      updateResult.setData(mChatAccountId);
      updateResult.setLoadStatus(LoadStatus.Finish);
      updateLiveData.setValue(updateResult);
    }
  }

  public void getFriendInfo(String accId) {
    ALog.d(LIB_TAG, TAG, "getFriendInfo:" + accId);
    ContactRepo.getFriendUserInfo(
        accId,
        false,
        new FetchCallback<UserWithFriend>() {

          @Override
          public void onSuccess(@Nullable UserWithFriend data) {
            if (data != null) {
              ChatUserCache.getInstance().addUserInfo(data.getUserInfo());
              friendInfoFetchResult.setData(data);
              friendInfoFetchResult.setLoadStatus(LoadStatus.Success);
              friendInfoLiveData.setValue(friendInfoFetchResult);
            }
          }

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(TAG, "getFriendInfo error:" + errorCode + " errorMsg:" + errorMsg);
          }
        });
  }

  //获取点对点消息已读时间戳
  protected void getP2PMessageReadReceipts() {
    ChatRepo.getP2PMessageReceipt(
        mConversationId,
        new FetchCallback<V2NIMP2PMessageReadReceipt>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(
                LIB_TAG,
                TAG,
                "getP2PMessageReadReceipts error:" + errorCode + " errorMsg:" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable V2NIMP2PMessageReadReceipt data) {
            if (data != null && Objects.equals(data.getConversationId(), mConversationId)) {
              receiptTime = data.getTimestamp();
              messageReceiptLiveData.setValue(data);
            }
          }
        });
  }

  @Override
  protected void onP2PMessageReadReceipts(List<V2NIMP2PMessageReadReceipt> readReceipts) {
    super.onP2PMessageReadReceipts(readReceipts);
    ALog.d(
        LIB_TAG, TAG, "message receipt:" + (readReceipts == null ? "null" : readReceipts.size()));
    FetchResult<List<V2NIMP2PMessageReadReceipt>> receiptResult =
        new FetchResult<>(LoadStatus.Finish);
    receiptResult.setData(readReceipts);
    receiptResult.setType(FetchResult.FetchType.Update);
    receiptResult.setTypeIndex(-1);
    if (receiptResult.getData() != null) {
      for (V2NIMP2PMessageReadReceipt receiptInfo : receiptResult.getData()) {
        if (TextUtils.equals(receiptInfo.getConversationId(), mConversationId)) {
          messageReceiptLiveData.setValue(receiptInfo);
        }
      }
    }
  }

  @Override
  public void addListener() {
    super.addListener();
    if (IMKitConfigCenter.getEnableTypingStatus()) {
      ChatRepo.addNotificationListener(notificationListener);
    }
    // 如果开启在线状态
    if (IMKitConfigCenter.getEnableOnlineStatus()) {
      OnlineStatusManager.addUserOnlineListener(onlineListener);
    }
    ContactRepo.addContactListener(friendListener);
    IMKitClient.addLoginDetailListener(loginDetailListener);
  }

  @Override
  public void removeListener() {
    super.removeListener();
    if (IMKitConfigCenter.getEnableTypingStatus()) {
      ChatRepo.removeNotificationListener(notificationListener);
    }
    OnlineStatusManager.removeUserOnlineListener(onlineListener);
    ContactRepo.removeContactListener(friendListener);
    IMKitClient.removeLoginDetailListener(loginDetailListener);
  }

  @Override
  public void sendReceipt(V2NIMMessage message) {
    ALog.d(
        LIB_TAG,
        TAG,
        "sendReceipt:"
            + (message == null
                ? "null"
                : message.getMessageClientId()
                    + message.getMessageConfig().isReadReceiptEnabled()));
    if (message != null
        && message.getMessageConfig().isReadReceiptEnabled()
        && showRead
        && message.getCreateTime() > receiptTime) {
      receiptTime = message.getCreateTime();
      ChatRepo.markP2PMessageRead(message, null);
    }
  }

  @Override
  public void notifyFriendChange(UserWithFriend friend) {
    if (friend.getAccount().equals(mChatAccountId)) {
      friendInfoFetchResult.setData(friend);
      friendInfoFetchResult.setLoadStatus(LoadStatus.Success);
      friendInfoLiveData.setValue(friendInfoFetchResult);
    }
  }

  /** 是否是好友 */
  public boolean isFriend(String account) {
    return ContactRepo.isFriend(account);
  }

  /** 保存非好友通话提示 */
  public void saveNotFriendCallTips() {
    MessageHelper.saveLocalNotFriendCallTipMessageAndNotify(
        getConversationId(), IMKitClient.account());
  }

  public void sendInputNotification(boolean isTyping) {
    ALog.d(LIB_TAG, TAG, "sendInputNotification:" + isTyping);

    // 功能控制开关，支持关闭该功能
    if (!IMKitConfigCenter.getEnableTypingStatus()) {
      return;
    }
    try {
      JSONObject json = new JSONObject();
      json.put(TYPE_STATE, isTyping ? 1 : 0);
      String content = json.toString();
      V2NIMNotificationPushConfig pushConfig =
          V2NIMNotificationPushConfig.V2NIMNotificationPushConfigBuilder.builder()
              .withPushEnabled(false)
              .build();

      V2NIMNotificationConfig notificationConfig =
          V2NIMNotificationConfig.V2NIMNotificationConfigBuilder.builder()
              .withUnreadEnabled(false)
              .withOfflineEnabled(false)
              .build();

      V2NIMNotificationRouteConfig.V2NIMNotificationRouteConfigBuilder routerConfig =
          V2NIMNotificationRouteConfig.V2NIMNotificationRouteConfigBuilder.builder();
      routerConfig.withRouteEnabled(false);
      V2NIMSendCustomNotificationParams params =
          V2NIMSendCustomNotificationParams.V2NIMSendCustomNotificationParamsBuilder.builder()
              .withPushConfig(pushConfig)
              .withRouteConfig(routerConfig.build())
              .withNotificationConfig(notificationConfig)
              .build();

      ChatRepo.sendCustomNotification(mConversationId, content, params, null);

    } catch (JSONException e) {
      ALog.e(TAG, e.getMessage());
    }
  }
}
