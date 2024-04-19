// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.locationkit;

import android.content.Context;
import com.amap.api.location.AMapLocationClient;
import com.netease.yunxin.kit.chatkit.map.IMessageMapProvider;
import com.netease.yunxin.kit.chatkit.map.IPageMapProvider;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;

public class LocationKitClient {

  private static IPageMapProvider pageMapProvider;
  private static IMessageMapProvider messageMapProvider;
  private static LocationConfig locationConfig;

  public static void init(Context context, LocationConfig config) {
    // 地图隐私合规
    AMapLocationClient.updatePrivacyShow(context, true, true);
    AMapLocationClient.updatePrivacyAgree(context, true);
    locationConfig = config;
    ChatKitClient.setMessageMapProvider(getMessageMapProvider());
  }

  public static IPageMapProvider getPageMapProvider() {
    if (pageMapProvider == null) {
      pageMapProvider = new PageMapImpl();
    }
    return pageMapProvider;
  }

  public static IMessageMapProvider getMessageMapProvider() {
    if (messageMapProvider == null) {
      messageMapProvider = new MessageMapImpl();
    }
    return messageMapProvider;
  }

  public static LocationConfig getLocationConfig() {
    return locationConfig;
  }
}
