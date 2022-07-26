package com.netease.yunxin.kit.chatkit.ui;

import android.app.Activity;

public interface IPermissionListener {

    boolean requestPermissionDenied(Activity activity, String permission);

    boolean permissionDeniedForever(Activity activity, String permission);
}
