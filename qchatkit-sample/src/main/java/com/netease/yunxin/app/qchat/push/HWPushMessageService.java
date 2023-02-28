// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.push;

import com.huawei.hms.push.RemoteMessage;
import com.netease.yunxin.kit.alog.ALog;

public class HWPushMessageService extends com.netease.nimlib.sdk.mixpush.HWPushMessageService {

  private static final String TAG = "HWPushMessageService";

  public void onNewToken(String token) {
    ALog.i(TAG, " onNewToken, token=" + token);
  }

  /**
   * 透传消息， 需要用户自己弹出通知
   *
   * @param remoteMessage
   */
  public void onMessageReceived(RemoteMessage remoteMessage) {
    ALog.i(TAG, " onMessageReceived");
  }

  public void onMessageSent(String s) {
    ALog.i(TAG, " onMessageSent");
  }

  public void onDeletedMessages() {
    ALog.i(TAG, " onDeletedMessages");
  }

  public void onSendError(String var1, Exception var2) {
    ALog.e(TAG, " onSendError, " + var1, var2);
  }
}
