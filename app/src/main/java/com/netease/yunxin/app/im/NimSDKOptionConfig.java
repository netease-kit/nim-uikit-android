// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im;

import android.content.Context;
import android.graphics.Color;
import com.netease.nimlib.sdk.NotificationFoldStyle;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.ServerAddresses;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.StatusBarNotificationFilter;
import com.netease.nimlib.sdk.mixpush.MixPushConfig;
import com.netease.yunxin.app.im.main.MainActivity;
import com.netease.yunxin.app.im.push.PushUserInfoProvider;
import com.netease.yunxin.app.im.utils.Constant;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.utils.ScreenUtils;

/** Nim SDK config info */
public class NimSDKOptionConfig {

  // 通知音频
  public static final String NOTIFY_SOUND_KEY =
      "android.resource://com.netease.yunxin.app.im/raw/msg";
  public static final int LED_ON_MS = 1000;
  public static final int LED_OFF_MS = 1500;

  // 初始化SDK配置，参数还以可以参考官网 IM SDK接口文档中有详细说明每个字段含义
  static SDKOptions getSDKOptions(Context context, String appKey) {
    SDKOptions options = new SDKOptions();
    options.appKey = appKey;
    initStatusBarNotificationConfig(options);
    options.preloadAttach = true;
    options.thumbnailSize = (int) (222.0 / 375.0 * ScreenUtils.getDisplayWidth());
    options.userInfoProvider = new PushUserInfoProvider(context);
    options.sessionReadAck = true;
    options.animatedImageThumbnailEnabled = true;
    options.asyncInitSDK = true;
    options.reducedIM = false;
    options.checkManifestConfig = false;
    options.enableTeamMsgAck = true;
    options.enableFcs = false;
    options.shouldConsiderRevokedMessageUnreadCount = true;
    // 会话置顶是否漫游
    options.notifyStickTopSession = true;
    options.mixPushConfig = buildMixPushConfig();
    options.serverConfig = configServer(context);
    // 打开消息撤回未读数-1的开关
    options.shouldConsiderRevokedMessageUnreadCount = true;
    return options;
  }

  // 配置海外节点，用户可以参考配置，来进行海外节点配置或者私有化配置
  public static ServerAddresses configServer(Context context) {

    if (DataUtils.getServerConfigType(context) == Constant.OVERSEA_CONFIG) {
      ServerAddresses serverAddresses = new ServerAddresses();
      serverAddresses.lbs = "https://lbs.netease.im/lbs/conf.jsp";
      serverAddresses.nosUploadLbs = "http://wannos.127.net/lbs";
      serverAddresses.nosUploadDefaultLink = "https://nosup-hz1.127.net";
      serverAddresses.nosDownloadUrlFormat = "{bucket}-nosdn.netease.im/{object}";
      serverAddresses.nosUpload = "nosup-hz1.127.net";
      serverAddresses.nosSupportHttps = true;
      ALog.d("ServerAddresses", "ServerConfig:use Singapore node");
      return serverAddresses;
    }
    return null;
  }

  public static void initStatusBarNotificationConfig(SDKOptions options) {
    // load notification
    StatusBarNotificationConfig config = loadStatusBarNotificationConfig();
    // load 用户的 StatusBarNotificationConfig 设置项
    // SDK statusBarNotificationConfig 生效
    config.notificationFilter =
        imMessage ->
            IMApplication.getForegroundActCount() > 0
                ? StatusBarNotificationFilter.FilterPolicy.DENY
                : StatusBarNotificationFilter.FilterPolicy.DEFAULT;
    options.statusBarNotificationConfig = config;
  }

  // config StatusBarNotificationConfig
  public static StatusBarNotificationConfig loadStatusBarNotificationConfig() {
    StatusBarNotificationConfig config = new StatusBarNotificationConfig();
    config.notificationEntrance = MainActivity.class;
    config.notificationSmallIconId = R.mipmap.ic_logo;
    config.notificationColor = Color.parseColor("#3a9efb");
    config.notificationSound = NOTIFY_SOUND_KEY;
    config.notificationFoldStyle = NotificationFoldStyle.ALL;
    config.downTimeEnableNotification = true;
    config.ledARGB = Color.GREEN;
    config.ledOnMs = LED_ON_MS;
    config.ledOffMs = LED_OFF_MS;
    config.showBadge = true;
    return config;
  }

  // 推送配置，包括小米，华为，魅族，FCM，VIVO，OPPO，参考官网文档说明
  private static MixPushConfig buildMixPushConfig() {
    MixPushConfig config = new MixPushConfig();
    // xiaomi
//    config.xmAppId = "";
//    config.xmAppKey = "";
//    config.xmCertificateName = "";

    // huawei
//    config.hwAppId = "";
//    config.hwCertificateName = "";

    // meizu
//    config.mzAppId = "";
//    config.mzAppKey = "";
//    config.mzCertificateName = "";

    // fcm
    //        config.fcmCertificateName = "";

    // vivo
//    config.vivoCertificateName = "";

    // oppo
//    config.oppoAppId = "";
//    config.oppoAppKey = "";
//    config.oppoAppSercet = "";
//    config.oppoCertificateName = "";
    return null;
  }
}
