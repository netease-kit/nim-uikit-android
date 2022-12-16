// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REVOKE_CONTENT_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REVOKE_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REVOKE_TIME_TAG;

import android.os.SystemClock;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.lifecycle.SdkLifecycleObserver;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ChatObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import java.util.HashMap;
import java.util.Map;

public class RevokeObserver {

  public static final String TAG = "RevokeObserver";
  private boolean hasInit = false;

  private Observer<RevokeMsgNotification> revokeMsgObserver =
      revokeMsgNotification -> {
        saveLocalRevokeMessage(revokeMsgNotification.getMessage());
      };

  private RevokeObserver() {}

  public void init() {
    if (!hasInit) {
      NIMClient.getService(SdkLifecycleObserver.class)
          .observeMainProcessInitCompleteResult(
              (Observer<Boolean>)
                  aBoolean -> {
                    ChatObserverRepo.registerRevokeMessageObserve(revokeMsgObserver);
                  },
              true);
      hasInit = true;
    }
  }

  private void saveLocalRevokeMessage(IMMessage message) {
    Map<String, Object> map = new HashMap<>(2);
    map.put(KEY_REVOKE_TAG, true);
    map.put(KEY_REVOKE_TIME_TAG, SystemClock.elapsedRealtime());
    map.put(KEY_REVOKE_CONTENT_TAG, message.getContent());
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

  private static RevokeObserver revokeObserver = new RevokeObserver();

  public static RevokeObserver getInstance() {
    return revokeObserver;
  }
}
