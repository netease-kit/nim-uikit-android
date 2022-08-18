// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageReceiptInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatMessageRepo;
import com.netease.yunxin.kit.chatkit.repo.ChatServiceObserverRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/** P2P chat info view model message receipt, type state for P2P chat page */
public class ChatP2PViewModel extends ChatBaseViewModel {

  private static final String LOG_TAG = "ChatP2PViewModel";

  private static final String TYPE_STATE = "typing";

  private final MutableLiveData<IMMessageReceiptInfo> messageReceiptLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<Boolean> typeStateLiveData = new MutableLiveData<>();

  //用户信息数据
  private final MutableLiveData<FetchResult<UserInfo>> p2pUserInfoLiveData =
      new MutableLiveData<>();

  private final FetchResult<UserInfo> p2pUserInfoFetchResult = new FetchResult<>(LoadStatus.Finish);

  private final EventObserver<List<IMMessageReceiptInfo>> messageReceiptObserver =
      new EventObserver<List<IMMessageReceiptInfo>>() {
        @Override
        public void onEvent(@Nullable List<IMMessageReceiptInfo> event) {
          ALog.i(LOG_TAG, "message receipt");
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
          ALog.e(LOG_TAG, e.getMessage());
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

  public MutableLiveData<FetchResult<UserInfo>> getP2pUserInfoLiveData() {
    return p2pUserInfoLiveData;
  }

  public void getP2pUserInfo(String accId) {
    ChatMessageRepo.fetchUserInfo(
        accId,
        new FetchCallback<UserInfo>() {
          @Override
          public void onSuccess(@Nullable UserInfo param) {
            p2pUserInfoFetchResult.setData(param);
            p2pUserInfoFetchResult.setLoadStatus(LoadStatus.Success);
            p2pUserInfoLiveData.setValue(p2pUserInfoFetchResult);
          }

          @Override
          public void onFailed(int code) {}

          @Override
          public void onException(@Nullable Throwable exception) {}
        });
  }

  @Override
  public void registerObservers(boolean register) {
    super.registerObservers(register);
    ChatServiceObserverRepo.observeMessageReceipt(messageReceiptObserver, register);
    ChatServiceObserverRepo.observeCustomNotification(customNotificationObserver, register);
  }

  @Override
  public void sendReceipt(IMMessage message) {
    ChatMessageRepo.markP2PMessageRead(mSessionId, message);
  }

  public void sendInputNotification(boolean isTyping) {
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
      ALog.e(LOG_TAG, e.getMessage());
    }

    ChatMessageRepo.sendCustomNotification(command);
  }
}
