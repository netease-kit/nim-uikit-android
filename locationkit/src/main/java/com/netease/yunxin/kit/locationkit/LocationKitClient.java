// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.locationkit;

import android.content.Context;
import com.amap.api.location.AMapLocationClient;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;

public class LocationKitClient {
  public static void init(Context context) {
    //地图隐私合规
    AMapLocationClient.updatePrivacyShow(context, true, true);
    AMapLocationClient.updatePrivacyAgree(context, true);
    ChatKitClient.setPageMapProvider(new PageMapImpl());
    ChatKitClient.setMessageMapProvider(new MessageMapImpl());
  }
}
