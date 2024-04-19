// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.multidex.MultiDexApplication;
import com.heytap.msp.push.HeytapPushManager;
import com.huawei.hms.support.common.ActivityMgr;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.yunxin.app.im.crash.AppCrashHandler;
import com.netease.yunxin.app.im.main.MainActivity;
import com.netease.yunxin.app.im.main.mine.MineInfoActivity;
import com.netease.yunxin.app.im.push.PushMessageHandler;
import com.netease.yunxin.app.im.utils.Constant;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.app.im.welcome.WelcomeActivity;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.IMKitUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.locationkit.LocationConfig;
import com.netease.yunxin.kit.locationkit.LocationKitClient;
import com.vivo.push.PushClient;
import com.vivo.push.util.VivoPushException;
import java.util.ArrayList;
import java.util.List;

/** IM application 包含IM UIKit(IM SDK)的初始化，crash异常捕获等 */
public class IMApplication extends MultiDexApplication {

  private static final String TAG = "IMApplication";
  private static boolean coldStart = false;
  private static int foregroundActCount = 0;
  public static final int LOGIN_PARENT_SCOPE = 2;
  public static final int LOGIN_SCOPE = 7;

  @Override
  public void onCreate() {
    super.onCreate();
    ALog.d(Constant.PROJECT_TAG, TAG, "onCreate");
    // app init
    registerActivityLifeCycle();
    AppCrashHandler.getInstance().initCrashHandler(this);
    Thread.setDefaultUncaughtExceptionHandler(AppCrashHandler.getInstance());

    //初始化IM UIKit
    initUIKit();
    // 注册个人页面到路由，点击个人头像根据路由地址跳转到该页面
    XKitRouter.registerRouter(RouterConstant.PATH_MINE_INFO_PAGE, MineInfoActivity.class);
  }

  private void initUIKit() {
    // 设置IM SDK的配置项，包括AppKey，推送配置和一些全局配置等
    SDKOptions options = NimSDKOptionConfig.getSDKOptions(this, DataUtils.readAppKey(this));
    // 初始化IM UIKit，初始化Kit层和IM SDK，将配置信息透传给IM SDK。无需再次初始化IM SDK
    IMKitClient.init(this, options);
    ALog.d(Constant.PROJECT_TAG, TAG, "initUIKit");

    // 如果是主进程，初始化地图组件，推送组件，crash组件
    if (IMKitUtils.isMainProcess(this)) {
      ALog.d(Constant.PROJECT_TAG, TAG, "initUIKit:isMainProcess");
      // 地图组件初始化，LocationConfig中包含高德地图web API的key，主要用于发送位置消息调用该服务生成图片来展示，提高页面加载性能
      LocationConfig locationConfig = new LocationConfig();
      locationConfig.aMapWebServerKey = DataUtils.readAMapAppKey(this);
      LocationKitClient.init(this, locationConfig);
      // huawei push
      ActivityMgr.INST.init(this);
      // oppo push
      HeytapPushManager.init(this, true);
      try {
        // vivo push
        PushClient.getInstance(this).initialize();
      } catch (VivoPushException e) {
        e.printStackTrace();
      }
      //设置推送提醒开关，根据用户设置的推送提醒开关状态，设置是否接收推送消息
      IMKitClient.toggleNotification(SettingRepo.isPushNotify());
      // 注册推送消息处理器，用于处理推送消息
      IMKitClient.registerMixPushMessageHandler(new PushMessageHandler());

    }
  }

  private final List<Activity> activities = new ArrayList<>();

  // 用于系统杀死应用之后，系统恢复应用，可能存在没有登录的异常
  // 此处如果在没有登录的情况下，其他页面打开的时候进行finish();除了MainActivity
  // MainActivity启动进行登录检测，如果没有登录进行登录操作
  private void registerActivityLifeCycle() {
    registerActivityLifecycleCallbacks(
        new ActivityLifecycleCallbacks() {
          @Override
          public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            if (TextUtils.isEmpty(IMKitClient.account())
                && !(activity instanceof MainActivity || activity instanceof WelcomeActivity)
                && !coldStart) {
              activity.finish();
            } else {
              activities.add(activity);
            }
          }

          @Override
          public void onActivityStarted(Activity activity) {
            foregroundActCount++;
          }

          @Override
          public void onActivityResumed(Activity activity) {}

          @Override
          public void onActivityPaused(Activity activity) {}

          @Override
          public void onActivityStopped(Activity activity) {
            foregroundActCount--;
          }

          @Override
          public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

          @Override
          public void onActivityDestroyed(Activity activity) {
            if (activities.isEmpty()) {
              return;
            }
            activities.remove(activity);
          }
        });
  }

  public void clearActivity(Activity exclude) {
    for (int i = 0; i < activities.size(); i++) {
      if (activities.get(i) != null && activities.get(i) != exclude) {
        activities.get(i).finish();
      }
    }
  }

  public static void setColdStart(boolean value) {
    coldStart = value;
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    foregroundActCount = 0;
  }

  public static int getForegroundActCount() {
    return foregroundActCount;
  }
}
