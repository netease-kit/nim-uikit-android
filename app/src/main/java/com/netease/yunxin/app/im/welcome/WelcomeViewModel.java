/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im.welcome;

import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.yunxin.app.im.NimSDKOptionConfig;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.corekit.im.XKitImClient;
import com.netease.yunxin.kit.corekit.im.repo.ConfigRepo;

public class WelcomeViewModel extends BaseViewModel {

    public void updateNotificationConfig(){
        XKitImClient.toggleNotification(ConfigRepo.getMixNotification());
        StatusBarNotificationConfig config = NimSDKOptionConfig.initStatusBarNotificationConfig();
        XKitImClient.updateStatusBarNotificationConfig(config);
    }
}
