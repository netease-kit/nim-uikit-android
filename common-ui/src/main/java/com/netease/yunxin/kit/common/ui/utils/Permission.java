// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.utils;

import static com.netease.yunxin.kit.common.ui.photo.TransHelper.KEY_PERMISSION_RESULT_DENIED;
import static com.netease.yunxin.kit.common.ui.photo.TransHelper.KEY_PERMISSION_RESULT_DENIED_FOREVER;
import static com.netease.yunxin.kit.common.ui.photo.TransHelper.KEY_PERMISSION_RESULT_GRANTED;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.netease.yunxin.kit.common.ui.photo.TransHelper;
import com.netease.yunxin.kit.common.utils.PermissionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Permission {
  private static final int REQUEST_CODE_PERMISSION = 1091;

  public static boolean hasPermissions(Context context, @NonNull String... permissions) {
    return PermissionUtils.hasPermissions(context, permissions);
  }

  public static List<PermissionResult> shouldShowRequestPermissionRationale(
      Activity activity, String... permissions) {
    List<PermissionResult> results = new ArrayList<>();
    for (String permission : permissions) {
      results.add(
          new PermissionResult(
              permission,
              ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)));
    }
    return results;
  }

  /** require permissions */
  public static Permission requirePermissions(Context context, String... permissions) {
    return new Permission(context, permissions);
  }

  public static class PermissionResult {
    public final String permission;
    public final boolean shouldShowRequestPermissionRationale;

    public PermissionResult(String permission, boolean shouldShowRequestPermissionRationale) {
      this.permission = permission;
      this.shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale;
    }
  }

  private final String[] permissions;
  private final Context context;
  private final PermissionCallbackWrapper callbackWrapper = new PermissionCallbackWrapper();

  public Permission(Context context, String[] permissions) {
    this.permissions = permissions;
    this.context = context;
  }

  public void request(PermissionCallback callback) {
    Objects.requireNonNull(context);
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      if (callback != null) {
        callback.onGranted(Arrays.asList(permissions));
      }
      return;
    }
    TransHelper.launchTask(
        context,
        REQUEST_CODE_PERMISSION,
        (activity, integer) -> {
          ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_PERMISSION);
          return null;
        },
        intentResultInfo -> {
          Intent intent = intentResultInfo.getValue();
          if (intent == null) {
            callbackWrapper.onException(callback, new IllegalStateException("No permission data."));
            return null;
          }
          ArrayList<String> grantedList =
              intent.getStringArrayListExtra(KEY_PERMISSION_RESULT_GRANTED);
          if (grantedList != null && !grantedList.isEmpty()) {
            callbackWrapper.onGranted(callback, grantedList);
          }

          List<String> deniedList = intent.getStringArrayListExtra(KEY_PERMISSION_RESULT_DENIED);
          List<String> deniedForeverList =
              intent.getStringArrayListExtra(KEY_PERMISSION_RESULT_DENIED_FOREVER);
          if ((deniedList != null && !deniedList.isEmpty())
              || deniedForeverList != null && !deniedForeverList.isEmpty()) {
            callbackWrapper.onDenial(callback, deniedList, deniedForeverList);
          }
          return null;
        });
  }

  private static class PermissionCallbackWrapper {

    public void onGranted(PermissionCallback callback, List<String> permissionsGranted) {
      if (callback == null) {
        return;
      }
      callback.onGranted(permissionsGranted);
    }

    public void onDenial(
        PermissionCallback callback,
        List<String> permissionsDenial,
        List<String> permissionDenialForever) {
      if (callback == null) {
        return;
      }
      callback.onDenial(permissionsDenial, permissionDenialForever);
    }

    public void onException(PermissionCallback callback, Exception exception) {
      if (callback == null) {
        return;
      }
      callback.onException(exception);
    }
  }

  public interface PermissionCallback {
    void onGranted(List<String> permissionsGranted);

    void onDenial(List<String> permissionsDenial, List<String> permissionDenialForever);

    void onException(Exception exception);
  }
}
