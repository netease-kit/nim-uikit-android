// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.locationkit;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.netease.yunxin.kit.chatkit.map.IChatMap;
import com.netease.yunxin.kit.chatkit.map.IMessageMapProvider;
import com.netease.yunxin.kit.chatkit.map.MapMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageMapImpl implements IMessageMapProvider {
  private static final String TAG = "MessageMapImpl";
  private final Map<String, List<IChatMap>> chatMapInterfaceMap = new HashMap<>();

  @NonNull
  @Override
  public IChatMap createChatMap(
      @NonNull String key, @NonNull Context context, @Nullable Bundle savedInstanceState) {
    IChatMap chatMap = new ChatMapWrapper(context, savedInstanceState, MapMode.MESSAGE);
    putValueToMap(key, chatMap);
    return chatMap;
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

  @Override
  public void destroyChatMap(@NonNull String key, @Nullable IChatMap chatMap) {
    if (chatMap == null) {
      return;
    }
    chatMap.onDestroy();
    removeValueFromMap(key, chatMap);
  }

  @Override
  public void releaseAllChatMap(@NonNull String key) {
    List<IChatMap> tempChatMapList = new ArrayList<>(getKeyFromMap(key));
    clearValueFromMap(key);
    for (IChatMap chatMap : tempChatMapList) {
      chatMap.onDestroy();
    }
  }

  private void putValueToMap(String key, IChatMap chatMap) {
    getKeyFromMap(key).add(chatMap);
  }

  private void removeValueFromMap(String key, IChatMap chatMap) {
    getKeyFromMap(key).remove(chatMap);
  }

  private void clearValueFromMap(String key) {
    getKeyFromMap(key).clear();
  }

  @NonNull
  @Override
  public String getChatMpaItemImage(double latitude, double longitude) {
    return ChatMapUtils.generateAMapImageUrl(latitude, longitude);
  }

  private List<IChatMap> getKeyFromMap(String key) {
    List<IChatMap> chatMapList = chatMapInterfaceMap.get(key);
    if (chatMapList == null) {
      chatMapList = new ArrayList<>();
      chatMapInterfaceMap.put(key, chatMapList);
    }
    return chatMapList;
  }
}
