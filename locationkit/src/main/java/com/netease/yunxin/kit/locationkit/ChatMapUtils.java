// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.locationkit;

import android.text.TextUtils;

public class ChatMapUtils {
  public static String AMAP_SERVER_KEY = "78c0eb6bff7db9ed52e07ca7051a97a3";
  public static String AMPS_SERVER_URL =
      "https://restapi.amap.com/v3/staticmap?" + "location=%s,%s&zoom=15&size=500*200&key=%s";

  public static String generateAMapImageUrl(double latitude, double longitude) {
    if (LocationKitClient.getLocationConfig() == null
        || TextUtils.isEmpty(LocationKitClient.getLocationConfig().aMapWebServerKey)) {
      return null;
    }
    return String.format(
        AMPS_SERVER_URL,
        longitude,
        latitude,
        LocationKitClient.getLocationConfig().aMapWebServerKey);
  }
}
