// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.push;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.mixpush.MixPushMessageHandler;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.app.qchat.NimSDKOptionConfig;
import java.util.ArrayList;
import java.util.Map;

public class PushMessageHandler implements MixPushMessageHandler {

  public static final String PAYLOAD_SESSION_ID = "sessionID";
  public static final String PAYLOAD_SESSION_TYPE = "sessionType";

  @Override
  public boolean onNotificationClicked(Context context, Map<String, String> payload) {
    String sessionId = payload.get(PAYLOAD_SESSION_ID);
    String type = payload.get(PAYLOAD_SESSION_TYPE);
    //
    if (sessionId != null && type != null) {
      int typeValue = Integer.valueOf(type);
      ArrayList<IMMessage> imMessages = new ArrayList<>();
      IMMessage imMessage =
          MessageBuilder.createEmptyMessage(sessionId, SessionTypeEnum.typeOfValue(typeValue), 0);
      imMessages.add(imMessage);
      Intent notifyIntent = new Intent();
      notifyIntent.setComponent(initLaunchComponent(context));
      notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      notifyIntent.setAction(Intent.ACTION_VIEW);
      notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 必须
      notifyIntent.putExtra(NimIntent.EXTRA_NOTIFY_CONTENT, imMessages);

      context.startActivity(notifyIntent);
      return true;
    } else {
      return false;
    }
  }

  private ComponentName initLaunchComponent(Context context) {
    ComponentName launchComponent;
    StatusBarNotificationConfig config = NimSDKOptionConfig.loadStatusBarNotificationConfig();
    Class<? extends Activity> entrance = config.notificationEntrance;
    if (entrance == null) {
      launchComponent =
          context
              .getPackageManager()
              .getLaunchIntentForPackage(context.getPackageName())
              .getComponent();
    } else {
      launchComponent = new ComponentName(context, entrance);
    }
    return launchComponent;
  }

  @Override
  public boolean cleanMixPushNotifications(int pushType) {
    return false;
  }
}
