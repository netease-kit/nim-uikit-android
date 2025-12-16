// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.locationkit;

import android.content.Context;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ChatService;
import com.netease.yunxin.kit.chatkit.ui.BuildConfig;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class LocationKitService extends ChatService {
  @NonNull
  @Override
  public String getServiceName() {
    return "LocationKit";
  }

  @NonNull
  @Override
  public String getVersionName() {
    return BuildConfig.versionName;
  }

  @NonNull
  @Override
  public ChatService create(@NonNull Context context) {
    // 注册路由地图详情页
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_LOCATION_PAGE, LocationPageActivity.class);
    return this;
  }
}
