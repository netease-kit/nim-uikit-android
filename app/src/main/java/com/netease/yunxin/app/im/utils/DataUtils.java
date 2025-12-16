// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import com.netease.yunxin.app.im.main.SettingKitConfig;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;

public class DataUtils {

  private static String appKey = null;
  private static String aMapServerKey = null;
  private static int serverConfigType = -1;
  private static String serverConfig = null;
  private static Boolean serverConfigSwitch = null;

  private static SettingKitConfig kitConfig = null;

  private static Boolean cloudConversation = null;
  private static Boolean teamApplyMode = null;
  private static Boolean aiStream = null;
  private static Boolean notificationHideContent = null;
  private static Boolean toggleNotification = null;
  private static Boolean togglePushConfig = null;
  private static String pushConfigContent = null;

  /** read appKey from manifest */
  public static String readAppKey(Context context) {
    if (appKey != null) {
      return appKey;
    }
    if (context != null) {

      try {
        ApplicationInfo appInfo =
            context
                .getPackageManager()
                .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        if (appInfo != null) {
          String keyStr =
              getServerConfigType(context) == Constant.CHINA_CONFIG
                  ? Constant.CONFIG_APPKEY_KEY
                  : Constant.CONFIG_APPKEY_KEY_OVERSEA;
          appKey = appInfo.metaData.getString(keyStr);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return appKey;
  }

  /**
   * 获取高德地图web server KEY 用于高德地图消息展示获取位置图片
   *
   * @param context
   * @return
   */
  public static String readAMapAppKey(Context context) {
    if (aMapServerKey != null) {
      return aMapServerKey;
    }
    if (context != null) {

      try {
        ApplicationInfo appInfo =
            context
                .getPackageManager()
                .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        aMapServerKey = appInfo.metaData.getString(Constant.CONFIG_AMAP_SERVER_KEY);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return aMapServerKey;
  }

  // 获取是否采用海外节点配置
  public static int getServerConfigType(Context context) {
    if (serverConfigType < 0) {
      serverConfigType =
          getConfigShared(context).getInt(Constant.SERVER_CONFIG, Constant.CHINA_CONFIG);
    }
    return serverConfigType;
  }

  // 获取私有化配置开关
  public static boolean getServerPrivateConfigSwitch(Context context) {
    if (serverConfigSwitch == null) {
      SharedPreferences sharedPreferences =
          context.getSharedPreferences(
              Constant.SERVER_PRIVATE_CONFIG_SWITCH_FILE, Context.MODE_MULTI_PROCESS);
      serverConfigSwitch = sharedPreferences.getBoolean(Constant.SERVER_CONFIG_SWITCH_PARAM, false);
    }
    return serverConfigSwitch;
  }

  // 获取本地会话配置开关
  public static boolean getCloudConversationConfigSwitch(Context context) {
    if (cloudConversation == null) {
      SharedPreferences sharedPreferences =
          context.getSharedPreferences(
              Constant.CONVERSATION_CONFIG_FILE, Context.MODE_MULTI_PROCESS);
      cloudConversation = sharedPreferences.getBoolean(Constant.CONVERSATION_LOCAL_CONFIG, false);
      teamApplyMode = sharedPreferences.getBoolean(Constant.TEAM_MODE_CONFIG, true);
    }
    return cloudConversation;
  }

  // 保存私有化配置开关
  public static void saveCloudConversationConfigSwitch(Context context, boolean configSwitch) {
    SharedPreferences.Editor editor =
        context
            .getSharedPreferences(Constant.CONVERSATION_CONFIG_FILE, Context.MODE_MULTI_PROCESS)
            .edit();
    editor.putBoolean(Constant.CONVERSATION_LOCAL_CONFIG, configSwitch);
    cloudConversation = configSwitch;
    editor.commit();
  }

  // 获取群邀请模式
  public static boolean getTeamModeConfigSwitch(Context context) {
    if (teamApplyMode == null) {
      SharedPreferences sharedPreferences =
          context.getSharedPreferences(
              Constant.CONVERSATION_CONFIG_FILE, Context.MODE_MULTI_PROCESS);
      teamApplyMode = sharedPreferences.getBoolean(Constant.TEAM_MODE_CONFIG, true);
    }
    return teamApplyMode;
  }

  // 保存群邀请模式
  public static void saveTeamModeConfigSwitch(Context context, boolean configSwitch) {
    SharedPreferences.Editor editor =
        context
            .getSharedPreferences(Constant.CONVERSATION_CONFIG_FILE, Context.MODE_MULTI_PROCESS)
            .edit();
    editor.putBoolean(Constant.TEAM_MODE_CONFIG, configSwitch);
    teamApplyMode = configSwitch;
    editor.commit();
  }

  // 获取AI流式输出配置开关
  public static boolean getAIStreamConfigSwitch(Context context) {
    if (aiStream == null) {
      SharedPreferences sharedPreferences =
          context.getSharedPreferences(Constant.AI_STREAM_CONFIG_FILE, Context.MODE_MULTI_PROCESS);
      aiStream = sharedPreferences.getBoolean(Constant.AI_STREAM_CONFIG, true);
    }
    return aiStream;
  }

  // 保存AI流式输出配置开关
  public static void saveAIStreamConfigSwitch(Context context, boolean configSwitch) {
    SharedPreferences.Editor editor =
        context
            .getSharedPreferences(Constant.AI_STREAM_CONFIG_FILE, Context.MODE_MULTI_PROCESS)
            .edit();
    editor.putBoolean(Constant.AI_STREAM_CONFIG, configSwitch);
    aiStream = configSwitch;
    editor.commit();
  }

  // 保存私有化配置开关
  public static void saveServerPrivateConfigSwitch(Context context, boolean configSwitch) {
    SharedPreferences.Editor editor =
        context
            .getSharedPreferences(
                Constant.SERVER_PRIVATE_CONFIG_SWITCH_FILE, Context.MODE_MULTI_PROCESS)
            .edit();
    editor.putBoolean(Constant.SERVER_CONFIG_SWITCH_PARAM, configSwitch);
    serverConfigSwitch = configSwitch;
    editor.commit();
  }

  // 获取私有化配置内容
  public static String getServerConfig(Context context) {
    if (TextUtils.isEmpty(serverConfig)) {
      SharedPreferences sharedPreferences =
          context.getSharedPreferences(
              Constant.SERVER_PRIVATE_CONFIG_FILE, Context.MODE_MULTI_PROCESS);
      serverConfig = sharedPreferences.getString(Constant.SERVER_CONFIG_PARAM, "");
    }
    return serverConfig;
  }

  // 保存私有化配置
  public static void saveServerConfig(Context context, String content) {
    SharedPreferences.Editor editor =
        context
            .getSharedPreferences(Constant.SERVER_PRIVATE_CONFIG_FILE, Context.MODE_MULTI_PROCESS)
            .edit();
    editor.putString(Constant.SERVER_CONFIG_PARAM, content);
    serverConfig = content;
    editor.commit();
  }

  public static SharedPreferences getConfigShared(Context context) {
    SharedPreferences sharedPreferences =
        context.getSharedPreferences(Constant.SERVER_CONFIG_FILE, Context.MODE_MULTI_PROCESS);
    return sharedPreferences;
  }

  // 获取在线通知是否展示内容配置开关
  public static boolean getNotificationHideContent(Context context) {
    if (notificationHideContent == null) {
      SharedPreferences sharedPreferences =
          context.getSharedPreferences(
              Constant.NOTIFICATION_CONFIG_FILE, Context.MODE_MULTI_PROCESS);
      notificationHideContent =
          sharedPreferences.getBoolean(Constant.NOTIFICATION_HIDE_CONFIG, true);
      if (toggleNotification == null) {
        toggleNotification =
            sharedPreferences.getBoolean(Constant.TOGGLE_NOTIFICATION_CONFIG, true);
      }
    }
    return notificationHideContent;
  }

  // 保存AI流式输出配置开关
  public static void saveNotificationHideContent(Context context, boolean configSwitch) {
    SharedPreferences.Editor editor =
        context
            .getSharedPreferences(Constant.NOTIFICATION_CONFIG_FILE, Context.MODE_MULTI_PROCESS)
            .edit();
    editor.putBoolean(Constant.NOTIFICATION_HIDE_CONFIG, configSwitch);
    notificationHideContent = configSwitch;
    editor.commit();
  }

  // 获取在线通知是否展示内容配置开关
  public static boolean getToggleNotification(Context context) {
    if (toggleNotification == null) {
      SharedPreferences sharedPreferences =
          context.getSharedPreferences(
              Constant.NOTIFICATION_CONFIG_FILE, Context.MODE_MULTI_PROCESS);
      toggleNotification = sharedPreferences.getBoolean(Constant.TOGGLE_NOTIFICATION_CONFIG, true);
      if (notificationHideContent == null) {
        notificationHideContent =
            sharedPreferences.getBoolean(Constant.NOTIFICATION_HIDE_CONFIG, true);
      }
    }
    return toggleNotification;
  }

  // 保存AI流式输出配置开关
  public static void saveToggleNotification(Context context, boolean configSwitch) {
    SharedPreferences.Editor editor =
        context
            .getSharedPreferences(Constant.NOTIFICATION_CONFIG_FILE, Context.MODE_MULTI_PROCESS)
            .edit();
    editor.putBoolean(Constant.TOGGLE_NOTIFICATION_CONFIG, configSwitch);
    toggleNotification = configSwitch;
    editor.commit();
  }

  public static SettingKitConfig getSettingKitConfig() {
    if (kitConfig == null) {
      kitConfig = new SettingKitConfig();
    }
    kitConfig.hasTeamApplyMode = getTeamModeConfigSwitch(IMKitClient.getApplicationContext());
    return kitConfig;
  }

  // 保存PUSH配置开关
  public static void savePushConfig(Context context, boolean configSwitch, String pushContent) {
    SharedPreferences.Editor editor =
        context.getSharedPreferences(Constant.PUSH_CONFIG_FILE, Context.MODE_MULTI_PROCESS).edit();
    editor.putBoolean(Constant.PUSH_CONFIG_TOGGLE, configSwitch);
    editor.putString(Constant.PUSH_CONFIG_CONTENT, pushContent);
    togglePushConfig = configSwitch;
    pushConfigContent = pushContent;
    editor.commit();
  }

  public static boolean getPushConfigToggle(Context context) {
    if (togglePushConfig == null) {
      SharedPreferences sharedPreferences =
          context.getSharedPreferences(Constant.PUSH_CONFIG_FILE, Context.MODE_MULTI_PROCESS);
      togglePushConfig = sharedPreferences.getBoolean(Constant.PUSH_CONFIG_TOGGLE, false);
      pushConfigContent = sharedPreferences.getString(Constant.PUSH_CONFIG_CONTENT, "");
    }
    return togglePushConfig;
  }

  public static String getPushConfigContent(Context context) {
    if (togglePushConfig == null) {
      SharedPreferences sharedPreferences =
          context.getSharedPreferences(Constant.PUSH_CONFIG_FILE, Context.MODE_MULTI_PROCESS);
      togglePushConfig = sharedPreferences.getBoolean(Constant.PUSH_CONFIG_TOGGLE, false);
      pushConfigContent = sharedPreferences.getString(Constant.PUSH_CONFIG_CONTENT, "");
    }
    return pushConfigContent;
  }

  public static float getSizeToM(long size) {
    return size / (1024.0f * 1024.0f);
  }
}
