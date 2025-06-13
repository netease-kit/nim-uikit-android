// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

public class PermissionHelper {

  public static boolean hasPermissions(Context context, @NonNull String... permissions) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      for (String permission : permissions) {
        if (PermissionChecker.checkSelfPermission(context, permission)
            != PermissionChecker.PERMISSION_GRANTED) {
          return false;
        }
      }
    }
    return true;
  }

  /** 检查是否有本地图片、视频或者文件读取权限 适配 Android不同版本的权限 */
  public static boolean checkImageOrFilePermission(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        && ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            == PERMISSION_GRANTED) {
      // Android 14及以上部分照片和视频访问权限
      return true;
    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU
        && (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                == PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO)
                == PERMISSION_GRANTED)) {
      // Android 13及以上完整照片和视频访问权限
      return true;
    } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        == PERMISSION_GRANTED) {
      // Android 12及以下完整本地读写访问权限
      return true;
    } else {
      // 无本地读写访问权限
      return false;
    }
  }
}
