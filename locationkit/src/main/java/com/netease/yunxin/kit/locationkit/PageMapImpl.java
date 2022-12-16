// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.locationkit;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.map.ChatLocationBean;
import com.netease.yunxin.kit.chatkit.map.IChatMap;
import com.netease.yunxin.kit.chatkit.map.ILocationSearchCallback;
import com.netease.yunxin.kit.chatkit.map.IPageMapProvider;
import com.netease.yunxin.kit.chatkit.map.MapMode;
import com.netease.yunxin.kit.common.ui.dialog.BottomChoiceDialog;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PageMapImpl
    implements IPageMapProvider,
        LocationSource,
        AMapLocationListener,
        PoiSearch.OnPoiSearchListener {
  private static final String TAG = "AMapWrapper";

  private WeakReference<Context> wkContext;
  private ChatMapWrapper chatMapWrapper;

  // location
  private LocationSource.OnLocationChangedListener mListener;
  private AMapLocationClient mLocationClient;
  private ChatLocationBean currentLocation;
  private String searchText = "searchText";
  private String searchTag = "";
  private PoiSearch.SearchBound doSearchBound;
  private LatLng markLatLng;
  private List<ChatLocationBean> locationPoiCache;
  private ILocationSearchCallback searchCallback;
  private final int SEARCH_BOUND = 5000;

  @Override
  public void createChatMap(
      @NonNull Context context,
      @Nullable Bundle savedInstanceState,
      @NonNull MapMode mapMode,
      @Nullable ILocationSearchCallback searchCallback) {
    ALog.i(TAG, "createChatMap, mapMode:" + mapMode);
    wkContext = new WeakReference<>(context);
    chatMapWrapper = new ChatMapWrapper(wkContext.get(), savedInstanceState, mapMode);
    this.searchCallback = searchCallback;
    AMapLocationClient.updatePrivacyShow(wkContext.get(), true, true);
    AMapLocationClient.updatePrivacyAgree(wkContext.get(), true);

    AMap aMap = chatMapWrapper.aMap;
    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
    if (chatMapWrapper.mode == MapMode.LOCATION) {
      MyLocationStyle myLocationStyle = new MyLocationStyle();
      // 定位一次，且将视角移动到地图中心点。
      myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
      myLocationStyle.myLocationIcon(
          BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location_in));
      myLocationStyle.anchor(0.5f, 0.5f);
      myLocationStyle.showMyLocation(true);
      aMap.setMyLocationStyle(myLocationStyle);

      // 通过aMap对象设置定位数据源的监听
      aMap.setLocationSource(PageMapImpl.this);
      // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
      aMap.setMyLocationEnabled(true);
    } else {
      aMap.setLocationSource(null);
      aMap.setMyLocationEnabled(false);
    }

    chatMapWrapper.setLocationListener(
        (latitude, longitude) -> {
          //          changeLocation(latitude, longitude, true);
          LatLng latLng = new LatLng(latitude, longitude);
          onMakerChange(latLng);
          // 搜索定位周边
          doSearch(
              "", new PoiSearch.SearchBound(new LatLonPoint(latitude, longitude), SEARCH_BOUND));
        });
  }

  @NonNull
  @Override
  public IChatMap getChatMap() {
    return chatMapWrapper;
  }

  @NonNull
  @Override
  public ChatLocationBean getCurrentLocation() {
    ALog.i(TAG, "map getCurrentLocation");
    return currentLocation;
  }

  @Override
  public void searchPoi(String keyWord) {
    ALog.i(TAG, "map searchPoi:" + keyWord);
    doSearch(
        keyWord,
        new PoiSearch.SearchBound(new LatLonPoint(markLatLng.latitude, markLatLng.longitude), 500));
  }

  private void doSearch(String keyWord, PoiSearch.SearchBound searchBound) {
    if (TextUtils.equals(searchText, keyWord) && isSameBound(searchBound, doSearchBound)) {
      ALog.i(TAG, "doSearch key:" + keyWord + "is same");
      return;
    }
    String city = "";
    if (currentLocation != null) {
      city = currentLocation.getCity();
    }
    searchText = keyWord;
    doSearchBound = searchBound;
    ALog.i(
        TAG, "doSearch key:" + keyWord + ", city:" + city + ", is bound:" + (searchBound != null));
    // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
    PoiSearch.Query mPoiSearchQuery = new PoiSearch.Query(keyWord, "", "");
    mPoiSearchQuery.requireSubPois(true); //true 搜索结果包含POI父子关系; false
    mPoiSearchQuery.setPageSize(10);
    mPoiSearchQuery.setPageNum(0);
    searchTag = String.valueOf(SystemClock.elapsedRealtime());
    ALog.i(TAG, "doSearch tag:" + searchTag);
    mPoiSearchQuery.setExtensions(searchTag);
    try {
      // POI搜索
      PoiSearch poiSearch = new PoiSearch(wkContext.get(), mPoiSearchQuery);
      poiSearch.setOnPoiSearchListener(this);
      if (searchBound != null && TextUtils.isEmpty(keyWord)) {
        poiSearch.setBound(searchBound);
      }
      poiSearch.searchPOIAsyn();
    } catch (AMapException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void doLocation() {
    ALog.i(TAG, "doLocation");
    if (mLocationClient != null) {
      locationPoiCache = null;
      // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
      // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
      // 在定位结束后，在合适的生命周期调用onDestroy()方法
      // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
      mLocationClient.startLocation();
    }
  }

  @Override
  public void onLocationChanged(AMapLocation aMapLocation) {
    if (mListener != null && aMapLocation != null) {
      if (aMapLocation.getErrorCode() == 0) {
        ALog.i(TAG, "onLocationChanged -->> " + aMapLocation.toStr());
        currentLocation =
            new ChatLocationBean(
                aMapLocation.getPoiName(),
                aMapLocation.getAddress(),
                aMapLocation.getCity(),
                aMapLocation.getLatitude(),
                aMapLocation.getLongitude(),
                0,
                true);
        mListener.onLocationChanged(aMapLocation); // 显示系统小蓝点
        // 搜索定位周边
        doSearch(
            "",
            new PoiSearch.SearchBound(
                new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()),
                SEARCH_BOUND));
      } else {
        ALog.e(
            TAG,
            "定位失败, code:" + aMapLocation.getErrorCode() + " -->> " + aMapLocation.getErrorInfo());
      }
    }
  }

  @Override
  public void activate(OnLocationChangedListener onLocationChangedListener) {
    ALog.i(TAG, "LocationSource activate -->> ");
    mListener = onLocationChangedListener;
    if (mLocationClient == null) {
      try {
        mLocationClient = new AMapLocationClient(wkContext.get());
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        // 设置定位监听
        mLocationClient.setLocationListener(this);
        // 设置为高精度定位模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 获取一次定位结果：
        mLocationOption.setOnceLocation(true);
        // 设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        doLocation();
      } catch (Exception e) {
        e.printStackTrace();
        ALog.e(TAG, "location active error: " + e.getMessage());
      }
    } else {
      doLocation();
    }
  }

  @Override
  public void deactivate() {
    ALog.i(TAG, "LocationSource deactivate.");
    mListener = null;
    if (mLocationClient != null) {
      mLocationClient.stopLocation();
      mLocationClient.onDestroy();
    }
    mLocationClient = null;
    searchCallback = null;
    locationPoiCache = null;
    currentLocation = null;
  }

  @Override
  public void changeLocation(double lat, double lng, boolean withAnim) {
    ALog.i(TAG, "changeLocation to lat:" + lat + " lng:" + lng + "location");
    chatMapWrapper.aMap.clear();
    // 添加自己的位置
    if (currentLocation != null) {
      onMakerChange(
          new LatLng(currentLocation.getLat(), currentLocation.getLng()),
          currentLocation.isSameLatLng(lat, lng)
              ? R.drawable.ic_my_location_in
              : R.drawable.ic_my_location_to,
          true);
    }
    LatLng latLng = new LatLng(lat, lng);
    onMakerChange(latLng);
    float zoom = chatMapWrapper.aMap.getCameraPosition().zoom;
    if (withAnim) {
      chatMapWrapper.aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    } else {
      chatMapWrapper.aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }
  }

  private void onMakerChange(LatLng latLng) {
    onMakerChange(latLng, R.drawable.ic_location_marker, false);
  }

  private void onMakerChange(LatLng latLng, int markerId, boolean center) {
    MarkerOptions options =
        new MarkerOptions()
            .icon(BitmapDescriptorFactory.fromResource(markerId))
            .position(latLng)
            .draggable(false);
    if (center) {
      options.anchor(0.5f, 0.5f);
    }
    markLatLng = latLng;
    chatMapWrapper.aMap.addMarker(options);
  }

  /** POI信息查询回调方法 */
  @Override
  public void onPoiSearched(PoiResult poiResult, int code) {
    if (code == AMapException.CODE_AMAP_SUCCESS) {
      if (poiResult != null && poiResult.getQuery() != null) {
        ALog.i(TAG, "onPoiSearched extension:" + poiResult.getQuery().getExtensions());
        if (!TextUtils.equals(searchTag, poiResult.getQuery().getExtensions())) {
          return;
        }
        if (locationPoiCache == null && searchCallback != null && poiResult.getPois() != null) {
          // 缓存定位地址列表
          locationPoiCache = convert(poiResult.getPois(), false);
          // 添加定位地址
          currentLocation.setSelected(true);
          locationPoiCache.add(0, currentLocation);
          ALog.i(TAG, "onPoiSearched locationPoiResult:" + locationPoiCache);
          searchCallback.onSuccess(locationPoiCache);
          return;
        }
        List<PoiItem> poiItems = poiResult.getPois();
        if (searchCallback != null && poiItems != null) {
          ALog.i(TAG, "onPoiSearched result:" + poiItems);
          searchCallback.onSuccess(convert(poiItems, true));
        }
      } else {
        ALog.e(TAG, "onPoiSearched no result:" + code);
        // 没有搜索到相关数据。
        if (searchCallback != null) {
          searchCallback.onFailed();
        }
      }
    } else {
      ALog.e(TAG, "onPoiSearched error:" + code);
      if (searchCallback != null) {
        searchCallback.onError(code);
      }
    }
  }

  @Override
  public void resumeLocationResult() {
    if (searchCallback != null) {
      ALog.i(TAG, "resumeLocationResult locationPoiResult:" + locationPoiCache);
      searchCallback.onSuccess(locationPoiCache);
    }
  }

  private List<ChatLocationBean> convert(List<PoiItem> poiItems, boolean firstSelect) {
    ArrayList<ChatLocationBean> chatLocationBeans = new ArrayList<>();
    boolean first = firstSelect;
    for (PoiItem item : poiItems) {
      LatLonPoint point = item.getLatLonPoint();
      ChatLocationBean bean =
          new ChatLocationBean(
              item.getTitle(),
              item.getSnippet(),
              item.getCityName(),
              point.getLatitude(),
              point.getLongitude(),
              item.getDistance() > 0 ? item.getDistance() : null,
              first);
      first = false;
      chatLocationBeans.add(bean);
    }
    return chatLocationBeans;
  }

  @Override
  public void onPoiItemSearched(PoiItem poiItem, int code) {}

  @Override
  public void jumpOutMap(
      @NonNull Context context, @NonNull String address, double lat, double lng) {
    BottomChoiceDialog choiceDialog = new BottomChoiceDialog(context, MapNavigator.getMapChoice());
    choiceDialog.setOnChoiceListener(
        new BottomChoiceDialog.OnChoiceListener() {
          @Override
          public void onChoice(@NonNull String type) {
            MapNavigator.mapNavigation(context, type, address, lat, lng);
          }

          @Override
          public void onCancel() {}
        });
    choiceDialog.show();
  }

  private boolean isSameBound(PoiSearch.SearchBound search, PoiSearch.SearchBound target) {
    if (search == null && target == null) {
      return true;
    } else if (search != null && target != null) {
      ALog.i(
          TAG,
          "isSameBound"
              + search.getCenter().getLatitude()
              + "="
              + target.getCenter().getLatitude()
              + ","
              + search.getCenter().getLongitude()
              + "="
              + search.getCenter().getLongitude());
      if (TextUtils.equals(
              String.valueOf(search.getCenter().getLatitude()),
              String.valueOf(target.getCenter().getLatitude()))
          && TextUtils.equals(
              String.valueOf(search.getCenter().getLongitude()),
              String.valueOf(search.getCenter().getLongitude()))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void onDestroy() {
    wkContext.clear();
    searchText = "searchText";
    doSearchBound = null;
  }
}
