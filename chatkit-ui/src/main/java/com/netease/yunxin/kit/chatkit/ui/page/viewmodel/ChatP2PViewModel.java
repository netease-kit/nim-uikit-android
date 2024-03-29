// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageReceiptInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/** P2P chat info view model message receipt, type state for P2P chat page */
public class ChatP2PViewModel extends ChatBaseViewModel {

  private static final String TAG = "ChatP2PViewModel";

  private static final String TYPE_STATE = "typing";

  private long receiptTime = 0L;

  private final MutableLiveData<IMMessageReceiptInfo> messageReceiptLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<Boolean> typeStateLiveData = new MutableLiveData<>();

  //用户信息数据
  private final MutableLiveData<FetchResult<FriendInfo>> friendInfoLiveData =
      new MutableLiveData<>();

  private final FetchResult<FriendInfo> friendInfoFetchResult =
      new FetchResult<>(LoadStatus.Finish);

  private final EventObserver<List<IMMessageReceiptInfo>> messageReceiptObserver =
      new EventObserver<List<IMMessageReceiptInfo>>() {
        @Override
        public void onEvent(@Nullable List<IMMessageReceiptInfo> event) {
          ALog.d(LIB_TAG, TAG, "message receipt:" + (event == null ? "null" : event.size()));
          FetchResult<List<IMMessageReceiptInfo>> receiptResult =
              new FetchResult<>(LoadStatus.Finish);
          receiptResult.setData(event);
          receiptResult.setType(FetchResult.FetchType.Update);
          receiptResult.setTypeIndex(-1);
          if (receiptResult.getData() != null) {
            for (IMMessageReceiptInfo receiptInfo : receiptResult.getData()) {
              if (TextUtils.equals(receiptInfo.getMessageReceipt().getSessionId(), mSessionId)) {
                messageReceiptLiveData.setValue(receiptInfo);
              }
            }
          }
        }
      };

  private final Observer<CustomNotification> customNotificationObserver =
      notification -> {
        ALog.d(
            LIB_TAG,
            TAG,
            "mcustomNotificationObserver:"
                + (notification == null ? "null" : notification.getTime()));
        if (!getSessionId().equals(notification.getSessionId())
            || notification.getSessionType() != SessionTypeEnum.P2P) {
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
      };

  /** chat message read receipt live data */
  public MutableLiveData<IMMessageReceiptInfo> getMessageReceiptLiveData() {
    return messageReceiptLiveData;
  }

  /** chat typing state live data */
  public MutableLiveData<Boolean> getTypeStateLiveData() {
    return typeStateLiveData;
  }

  public MutableLiveData<FetchResult<FriendInfo>> getFriendInfoLiveData() {
    return friendInfoLiveData;
  }

  public void getFriendInfo(String accId) {
    ALog.d(LIB_TAG, TAG, "getFriendInfo:" + accId);
    ContactRepo.getFriendWithUserInfo(
        accId,
        new FetchCallback<FriendInfo>() {
          @Override
          public void onSuccess(@Nullable FriendInfo param) {
            List<FriendInfo> userInfoList = new ArrayList<>();
            userInfoList.add(param);
            ChatUserCache.addFriendInfo(userInfoList);
            friendInfoFetchResult.setData(param);
            friendInfoFetchResult.setLoadStatus(LoadStatus.Success);
            friendInfoLiveData.setValue(friendInfoFetchResult);
          }

          @Override
          public void onFailed(int code) {}

          @Override
          public void onException(@Nullable Throwable exception) {}
        });
  }

  @Override
  public void registerObservers() {
    super.registerObservers();
    ChatObserverRepo.registerMessageReceiptObserve(messageReceiptObserver);
    ChatObserverRepo.registerCustomNotificationObserve(customNotificationObserver);
  }

  @Override
  public void unregisterObservers() {
    super.unregisterObservers();
    ChatObserverRepo.unregisterMessageReceiptObserve(messageReceiptObserver);
    ChatObserverRepo.unregisterCustomNotificationObserve(customNotificationObserver);
  }

  @Override
  public void sendReceipt(IMMessage message) {
    ALog.d(
        LIB_TAG,
        TAG,
        "sendReceipt:" + (message == null ? "null" : message.getUuid() + message.needMsgAck()));
    if (message != null && message.needMsgAck() && showRead && message.getTime() > receiptTime) {
      receiptTime = message.getTime();
      ChatRepo.markP2PMessageRead(mSessionId, message);
    }
  }

  @Override
  public void notifyFriendChange(FriendChangedNotify friendChangedNotify) {
    if (friendChangedNotify != null) {
      boolean hasChange = false;
      for (String account : friendChangedNotify.getDeletedFriends()) {
        if (TextUtils.equals(account, mSessionId)) {
          hasChange = true;
          break;
        }
      }
      if (!hasChange) {
        for (Friend friend : friendChangedNotify.getAddedOrUpdatedFriends()) {
          if (TextUtils.equals(friend.getAccount(), mSessionId)) {
            hasChange = true;
            break;
          }
        }
      }
      if (hasChange) {
        getFriendInfo(mSessionId);
      }
    }
  }

  public void sendInputNotification(boolean isTyping) {
    ALog.d(LIB_TAG, TAG, "sendInputNotification:" + isTyping);
    CustomNotification command = new CustomNotification();
    command.setSessionId(getSessionId());
    command.setSessionType(SessionTypeEnum.P2P);
    CustomNotificationConfig config = new CustomNotificationConfig();
    config.enablePush = false;
    config.enableUnreadCount = false;
    command.setConfig(config);

    try {
      JSONObject json = new JSONObject();
      json.put(TYPE_STATE, isTyping ? 1 : 0);
      command.setContent(json.toString());
    } catch (JSONException e) {
      ALog.e(TAG, e.getMessage());
    }

    ChatRepo.sendCustomNotification(command);
  }
}
