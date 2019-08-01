package com.netease.nim.uikit.api.model.location;

import android.content.Context;

/**
 * 定位信息提供者类。用于提供地理位置消息，以及根据地理位置打开对应的地图。<br>
 * 第三方app可自由选择定位SDK
 */
public interface LocationProvider {

    // 请求定位信息，由callback返回当前地理位置信息
    // 定位成功后，请调用callback.onSuccess，如果取消定位或定位失败，无需调用
    void requestLocation(Context context, Callback callback);

    // 根据当前地理位置打开地图
    void openMap(Context context, double longitude, double latitude, String address);

    // 定位请求的回调函数。如果定位不成功，或者用户取消，不回调即可。
    interface Callback {
        void onSuccess(double longitude, double latitude, String address);
    }
}
