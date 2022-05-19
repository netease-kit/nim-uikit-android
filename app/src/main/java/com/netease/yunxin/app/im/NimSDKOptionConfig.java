/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.text.TextUtils;

import com.netease.nimlib.sdk.NotificationFoldStyle;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.mixpush.MixPushConfig;
import com.netease.yunxin.app.im.main.MainActivity;
import com.netease.yunxin.app.im.push.PushUserInfoProvider;
import com.netease.yunxin.kit.common.utils.media.ImageUtil;
import com.netease.yunxin.kit.corekit.im.repo.ConfigRepo;

import java.io.IOException;

/**
 * Nim SDK config info
 */

public class NimSDKOptionConfig {

    public static final String NOTIFY_SOUND_KEY = "android.resource://com.netease.yunxin.app.im/raw/msg";
    public static final int LED_ON_MS = 1000;
    public static final int LED_OFF_MS = 1500;

    static SDKOptions getSDKOptions(Context context, String appKey) {
        SDKOptions options = new SDKOptions();
        options.appKey = appKey;
        options.statusBarNotificationConfig = initStatusBarNotificationConfig();
        options.sdkStorageRootPath = getAppCacheDir(context);
        options.preloadAttach = true;
        options.thumbnailSize = ImageUtil.getImageThumbMaxEdge();
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
        // 打开消息撤回未读数-1的开关
        options.shouldConsiderRevokedMessageUnreadCount = true;
        return options;
    }

    public static StatusBarNotificationConfig initStatusBarNotificationConfig() {
        // load notification
        StatusBarNotificationConfig config = loadStatusBarNotificationConfig();
        // load user StatusBarNotificationConfig
        StatusBarNotificationConfig userConfig = ConfigRepo.getStatusBarNotificationConfig();
        if (userConfig == null) {
            userConfig = config;
        } else {
            userConfig.notificationEntrance = config.notificationEntrance;
            userConfig.notificationFoldStyle = config.notificationFoldStyle;
            userConfig.notificationColor = config.notificationColor;
        }
        // 持久化生效
        ConfigRepo.localSaveStatusBarNotificationConfig(userConfig);
        // SDK statusBarNotificationConfig 生效
        return userConfig;
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

    /**
     * config app image/voice/file/log directory
     */
    static String getAppCacheDir(Context context) {
        String storageRootPath = null;
        try {
            if (context.getExternalCacheDir() != null) {
                storageRootPath = context.getExternalCacheDir().getCanonicalPath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(storageRootPath)) {
            storageRootPath = Environment.getExternalStorageDirectory() + "/" + context.getPackageName();
        }
        return storageRootPath;
    }

    private static MixPushConfig buildMixPushConfig() {
        MixPushConfig config = new MixPushConfig();
        // xiaomi
//        config.xmAppId = "xiao mi push app id"; //apply in xiaomi
//        config.xmAppKey = "xiao mi push app key";//apply in xiaomi
//        config.xmCertificateName = "Certificate Name";//config in yunxin platform

        // huawei
//        config.hwAppId = "huawei app id";//apply in huawei
//        config.hwCertificateName = "Certificate Name";//config in yunxin platform

        // meizu
//        config.mzAppId = "meizu push app id";//apply in meizu
//        config.mzAppKey = "meizu push app key";//apply in meizu
//        config.mzCertificateName = "Certificate Name";//config in yunxin platform

        // fcm
//        config.fcmCertificateName = "DEMO_FCM_PUSH";

        // vivo
//        config.vivoCertificateName = "Certificate Name";//config in yunxin platform

        // oppo
//        config.oppoAppId = "oppo push app id";//apply in oppo
//        config.oppoAppKey = "oppo push app key";//apply in oppo
//        config.oppoAppSercet = "oppo push app secret"; //apply in oppo
//        config.oppoCertificateName = "Certificate Name";//config in yunxin platform
        return config;
    }
}
