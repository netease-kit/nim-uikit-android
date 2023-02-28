// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.multidex.MultiDexApplication;
import com.heytap.msp.push.HeytapPushManager;
import com.huawei.hms.support.common.ActivityMgr;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.yunxin.app.qchat.crash.AppCrashHandler;
import com.netease.yunxin.app.qchat.main.MainActivity;
import com.netease.yunxin.app.qchat.main.mine.MineInfoActivity;
import com.netease.yunxin.app.qchat.push.PushMessageHandler;
import com.netease.yunxin.app.qchat.utils.Constant;
import com.netease.yunxin.app.qchat.utils.DataUtils;
import com.netease.yunxin.app.qchat.welcome.WelcomeActivity;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.repo.SettingRepo;
import com.netease.yunxin.kit.corekit.im.utils.IMKitUtils;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.locationkit.LocationKitClient;
import com.vivo.push.PushClient;
import com.vivo.push.util.VivoPushException;
import java.util.ArrayList;
import java.util.List;

public class QChatApplication extends MultiDexApplication {

  private static final String TAG = "QChatApplication";
  private static boolean coldStart = false;
  private static int foregroundActCount = 0;

  @Override
  public void onCreate() {
    super.onCreate();
    ALog.d(Constant.PROJECT_TAG, TAG, "onCreate");
    //app init
    registerActivityLifeCycle();
    AppCrashHandler.getInstance().initCrashHandler(this);
    Thread.setDefaultUncaughtExceptionHandler(AppCrashHandler.getInstance());

    initUIKit();
    // temp register for mine
    XKitRouter.registerRouter(RouterConstant.PATH_MINE_INFO_PAGE, MineInfoActivity.class);
  }

  private void initUIKit() {
    SDKOptions options = NimSDKOptionConfig.getSDKOptions(this, DataUtils.readAppKey(this));
    IMKitClient.init(this, null, options);
    ALog.d(Constant.PROJECT_TAG, TAG, "initUIKit");

    if (IMKitUtils.isMainProcess(this)) {
      ALog.d(Constant.PROJECT_TAG, TAG, "initUIKit:isMainProcess");
      LocationKitClient.init();
      //huawei push
      ActivityMgr.INST.init(this);
      //oppo push
      HeytapPushManager.init(this, true);
      try {
        //vivo push
        PushClient.getInstance(this).initialize();
      } catch (VivoPushException e) {
        e.printStackTrace();
      }
      IMKitClient.toggleNotification(SettingRepo.isPushNotify());
      IMKitClient.registerMixPushMessageHandler(new PushMessageHandler());
    }
  }

  private final List<Activity> activities = new ArrayList<>();

  //用于系统杀死应用之后，系统恢复应用，可能存在没有登录的异常
  //此处如果在没有登录的情况下，其他页面打开的时候进行finish();除了MainActivity
  //MainActivity启动进行登录检测，如果没有登录进行登录操作
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
            ALog.d(Constant.PROJECT_TAG, TAG, "onActivityStarted:" + foregroundActCount);
          }

          @Override
          public void onActivityResumed(Activity activity) {}

          @Override
          public void onActivityPaused(Activity activity) {}

          @Override
          public void onActivityStopped(Activity activity) {
            foregroundActCount--;
            ALog.d(Constant.PROJECT_TAG, TAG, "onActivityStopped:" + foregroundActCount);
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
