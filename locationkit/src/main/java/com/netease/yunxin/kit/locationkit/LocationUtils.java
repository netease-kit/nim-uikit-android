// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.locationkit;

import com.amap.api.services.poisearch.PoiSearch;
import com.netease.yunxin.kit.chatkit.map.ChatLocationBean;

public class LocationUtils {

  public static boolean isSameLatLng(ChatLocationBean bean, PoiSearch.SearchBound searchBound) {

    if (bean == null && (searchBound == null || searchBound.getCenter() == null)) {
      return true;
    } else if (bean != null && searchBound != null && searchBound.getCenter() != null) {
      return bean.isSameLatLng(
          searchBound.getCenter().getLatitude(), searchBound.getCenter().getLongitude());
    }
    return false;
  }
}
