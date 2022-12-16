// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.locationkit;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.netease.yunxin.kit.chatkit.map.IChatMap;
import com.netease.yunxin.kit.chatkit.map.IMessageMapProvider;
import com.netease.yunxin.kit.chatkit.map.MapMode;

public class MessageMapImpl implements IMessageMapProvider {
  private static final String TAG = "MessageMapImpl";

  @NonNull
  @Override
  public IChatMap createChatMap(@NonNull Context context, @Nullable Bundle savedInstanceState) {
    return new ChatMapWrapper(context, savedInstanceState, MapMode.MESSAGE);
  }

  @NonNull
  @Override
  public View setLocation(@NonNull IChatMap chatMap, double lat, double lng) {
    if (chatMap instanceof ChatMapWrapper) {
      AMap aMap = ((ChatMapWrapper) chatMap).aMap;
      LatLng latLng = new LatLng(lat, lng);
      aMap.moveCamera(
          CameraUpdateFactory.newCameraPosition(
              CameraPosition.builder().target(latLng).zoom(17).build()));
      aMap.clear();
      MarkerOptions options =
          new MarkerOptions()
              .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker))
              .position(latLng)
              .draggable(false);
      aMap.addMarker(options);
    }
    return chatMap.getMapView();
  }
}
