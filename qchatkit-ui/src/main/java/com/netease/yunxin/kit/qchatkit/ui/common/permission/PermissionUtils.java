// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.common.permission;

import static com.netease.yunxin.kit.common.ui.photo.TransHelper.KEY_PERMISSION_RESULT_DENIED;
import static com.netease.yunxin.kit.common.ui.photo.TransHelper.KEY_PERMISSION_RESULT_DENIED_FOREVER;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.kit.common.ui.photo.TransHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** The utils of permission. */
public final class PermissionUtils {
  private static final int REQUEST_CODE_PERMISSION = 1091;

  /**
   * check permissions that you owned.
   *
   * @param permissions permission list to be checked
   * @return true, you owned all permissions otherwise, you didn't own some of permissions.
   */
  public static boolean checkPermission(Context context, String... permissions) {
    Objects.requireNonNull(context);
    if (permissions == null || permissions.length == 0) {
      return false;
    }
    boolean result = true;
    for (String permission : permissions) {
      int resultCode = ContextCompat.checkSelfPermission(context, permission);
      result = resultCode == PackageManager.PERMISSION_GRANTED;
      if (!result) {
        break;
      }
    }
    return result;
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
  public static PermissionRequest requirePermissions(Context context, String... permissions) {
    return new PermissionRequest(context, permissions);
  }

  public static class PermissionResult {
    public final String permission;
    public final boolean shouldShowRequestPermissionRationale;

    public PermissionResult(String permission, boolean shouldShowRequestPermissionRationale) {
      this.permission = permission;
      this.shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale;
    }
  }

  public static class PermissionRequest {
    private final String[] permissions;
    private final Context context;
    private final PermissionCallbackWrapper callbackWrapper = new PermissionCallbackWrapper();

    public PermissionRequest(Context context, String[] permissions) {
      this.permissions = permissions;
      this.context = context;
    }

    public void request(PermissionCallback callback) {
      Objects.requireNonNull(context);
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
              callbackWrapper.onException(
                  callback, new IllegalStateException("No permission data."));
              return null;
            }

            ArrayList<String> grantedList =
                intent.getStringArrayListExtra(TransHelper.KEY_PERMISSION_RESULT_GRANTED);
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
