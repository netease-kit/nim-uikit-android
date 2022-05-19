/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.multidex.MultiDexApplication;

import com.heytap.msp.push.HeytapPushManager;
import com.huawei.hms.support.common.ActivityMgr;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.yunxin.app.im.crash.AppCrashHandler;
import com.netease.yunxin.app.im.main.mine.UserInfoActivity;
import com.netease.yunxin.app.im.push.PushMessageHandler;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.alog.BasicInfo;
import com.netease.yunxin.kit.common.ui.CommonUIClient;
import com.netease.yunxin.kit.corekit.im.XKitImClient;
import com.netease.yunxin.kit.corekit.im.repo.ConfigRepo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.im.utils.XKitImUtils;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.vivo.push.PushClient;
import com.vivo.push.util.VivoPushException;

import java.util.ArrayList;
import java.util.List;


public class IMApplication extends MultiDexApplication {

    private static final String TAG = "IMApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        ALog.d(TAG, "onCreate");
        //app init
        registerActivityLifeCycle();
        AppCrashHandler.getInstance().initCrashHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(AppCrashHandler.getInstance());

        initUIKit();
        initALog(this);
        // temp register for mine
        XKitRouter.registerRouter(RouterConstant.PATH_MINE_USER_INFO, UserInfoActivity.class);
        //set custom config for chat UI
        KitCustomConfig.initChatUICustom();

        //set custom config for contact ui
        KitCustomConfig.initContactUICustom();
    }

    private void initALog(Context context) {
        ALog.logFirst(new BasicInfo.Builder()
                .packageName(context)
                .imVersion(NIMClient.getSDKVersion())
                .deviceId(context)
                .platform("Android")
                .name("XKit", true)
                .build());
    }

    private void initUIKit() {
        XKitImClient.init(this);
        SDKOptions options = NimSDKOptionConfig.getSDKOptions(this, DataUtils.readAppKey(this));
        XKitImClient.config(null,options);
        if (XKitImUtils.isMainProcess(this)) {
            //huawei push
            ActivityMgr.INST.init(this);
            //oppo push
            HeytapPushManager.init(this, true);
            try {
                //vivo push
                PushClient.getInstance(this).initialize();
            }catch (VivoPushException e){

            }
            XKitImClient.toggleNotification(ConfigRepo.getMixNotification());
            XKitImClient.registerMixPushMessageHandler(new PushMessageHandler());
        }
        //config ui
        CommonUIClient.init(this);
    }

    private final List<Activity> activities = new ArrayList<>();

    private void registerActivityLifeCycle() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                activities.add(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

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
}
