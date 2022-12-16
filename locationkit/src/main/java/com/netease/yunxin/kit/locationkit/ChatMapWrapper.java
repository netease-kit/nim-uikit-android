// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.locationkit;

import static com.netease.yunxin.kit.locationkit.LocationConstant.LIB_TAG;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.LatLng;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.map.IChatMap;
import com.netease.yunxin.kit.chatkit.map.ILocationListener;
import com.netease.yunxin.kit.chatkit.map.MapMode;

public class ChatMapWrapper implements IChatMap {
  public FrameLayout interceptView;
  public MapMode mode;
  public MapView mapView;
  public AMap aMap;
  public ILocationListener locationListener;
  public final String TAG = "ChatMapWrapper";

  public ChatMapWrapper(
      @NonNull Context context, @Nullable Bundle savedInstanceState, @NonNull MapMode mapMode) {
    ALog.d(LIB_TAG, TAG, "construction, mapMode:" + mapMode);
    mode = mapMode;
    interceptView =
        new FrameLayout(context) {
          @Override
          public boolean onInterceptTouchEvent(MotionEvent ev) {
            // 消息中的地图不可点击
            if (mapMode == MapMode.LOCATION) {
              ALog.d(
                  LIB_TAG,
                  TAG,
                  "onInterceptTouchEvent, locationListener:" + (locationListener == null));
              if (locationListener != null) {
                if (ev.getAction() == MotionEvent.ACTION_UP) {
                  int[] screenLocation = new int[2];
                  mapView.getLocationOnScreen(screenLocation);
                  int height = mapView.getHeight();

                  int width = mapView.getWidth();
                  int screenX = screenLocation[0] + width / 2;
                  int screenY = screenLocation[1] + height / 2;
                  Point point = new Point(screenX, screenY);
                  LatLng latLng = mapView.getMap().getProjection().fromScreenLocation(point);
                  ALog.d(
                      LIB_TAG,
                      TAG,
                      "latitude="
                          + latLng.latitude
                          + ",longitude = "
                          + latLng.longitude
                          + ",screenX="
                          + screenX
                          + ","
                          + screenY);
                  locationListener.onScreenLocationChange(latLng.latitude, latLng.longitude);
                }
              }
              return false;
            }
            return false;
          }
        };
    mapView = new MapView(context);
    mapView.onCreate(savedInstanceState);
    interceptView.addView(mapView);
    aMap = mapView.getMap();
    aMap.setOnMapLoadedListener(
        () -> {
          UiSettings uiSettings = aMap.getUiSettings();
          if (mode == MapMode.MESSAGE) {
            uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);
            uiSettings.setAllGesturesEnabled(false);
            uiSettings.setCompassEnabled(false);
            uiSettings.setMyLocationButtonEnabled(false);
          } else {
            uiSettings.setZoomGesturesEnabled(true);
            uiSettings.setScrollGesturesEnabled(true);
          }
          // location, detail
          uiSettings.setZoomControlsEnabled(false);
        });
  }

  @NonNull
  @Override
  public View getMapView() {
    return interceptView;
  }

  @Override
  public void onResume() {
    mapView.onResume();
  }

  @Override
  public void onPause() {
    mapView.onPause();
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void setLocationListener(@NonNull ILocationListener listener) {
    locationListener = listener;
  }

  @Override
  public void onDestroy() {
    mapView.onDestroy();
  }
}
