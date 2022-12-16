// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.locationkit;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MapNavigator {
  public static final String PACKAGE_NAME_GAODE = "com.autonavi.minimap";
  public static final String PACKAGE_NAME_TENCENT = "com.tencent.map";

  public static void mapNavigation(
      Context context, String action, String address, double lat, double lng) {

    if (PACKAGE_NAME_TENCENT.equals(action)) {
      tencentGuide(context, address, new double[] {lat, lng});
    } else {
      aMapNavigation(context, address, lat, lng);
    }
  }

  public static void aMapNavigation(Context context, String address, double lat, double lng) {
    try {
      Intent intent =
          Intent.parseUri(
              "androidamap://viewMap?sourceApplication=NIMUIKit"
                  + "&poiname="
                  + address
                  + "&lat="
                  + lat
                  + "&lon="
                  + lng
                  + "&dev=0",
              0);
      context.startActivity(intent);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (ActivityNotFoundException activityNotFoundException) {
      Uri uri = Uri.parse("market://details?id=" + PACKAGE_NAME_GAODE);
      Intent marketIntent = new Intent(Intent.ACTION_VIEW, uri);
      context.startActivity(marketIntent);
    }
  }

  /**
   * 腾讯导航
   *
   * @param context
   * @param location
   */
  public static void tencentGuide(Context context, String address, double[] location) {
    String baseUrl = "qqmap://map/";
    String drivePlan =
        "routeplan?type=drive&from="
            + "我的位置"
            + "&fromcoord=&to="
            + address
            + "&tocoord="
            + location[0]
            + ","
            + location[1]
            + "&policy=1";
    String tencentUri = baseUrl + drivePlan + "&referer=imuikit";
    try {
      Intent intent = Intent.parseUri(tencentUri, 0);
      context.startActivity(intent);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (ActivityNotFoundException activityNotFoundException) {
      //市场下载
      Uri uri = Uri.parse("market://details?id=" + PACKAGE_NAME_TENCENT);
      Intent marketIntent = new Intent(Intent.ACTION_VIEW, uri);
      context.startActivity(marketIntent);
    }
  }

  public static ArrayList<ActionItem> getMapChoice() {
    ArrayList<ActionItem> mapList = new ArrayList<>();
    ActionItem aMapItem =
        new ActionItem(PACKAGE_NAME_GAODE, -1, R.string.location_amap_nav_title)
            .setTitleColorResId(R.color.choice_dialog_title);
    mapList.add(aMapItem);
    ActionItem tMapItem =
        new ActionItem(PACKAGE_NAME_TENCENT, -1, R.string.location_tencent_nav_title)
            .setTitleColorResId(R.color.choice_dialog_title);
    mapList.add(tMapItem);
    return mapList;
  }
}
