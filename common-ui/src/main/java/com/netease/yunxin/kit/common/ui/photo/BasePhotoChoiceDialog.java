// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.photo;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.R;
import com.netease.yunxin.kit.common.ui.utils.CommonCallback;
import com.netease.yunxin.kit.common.ui.utils.Permission;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.PermissionUtils;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** This dialog supports to take photos or get a image from album and upload to nos service. */
public abstract class BasePhotoChoiceDialog extends Dialog {
  private static final String TAG = "PhotoChoiceDialog";

  private final View rootView;
  protected View takePhotoView;
  protected View getFromAlbumView;
  protected View cancelView;

  protected CommonCallback<File> callback;
  protected final String[] permissionForCamera = new String[] {Manifest.permission.CAMERA};
  protected String[] permissionForAlbum =
      new String[] {
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
      };

  public BasePhotoChoiceDialog(@NonNull Activity activity, int themeResId) {
    super(activity, themeResId);
    rootView = initViewAndGetRootView();
    checkViews();
  }

  protected abstract View initViewAndGetRootView();

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(cancelView);
    Objects.requireNonNull(takePhotoView);
    Objects.requireNonNull(getFromAlbumView);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Window window = getWindow();
    if (window != null) {
      WindowManager.LayoutParams wlp = window.getAttributes();
      wlp.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
      wlp.gravity = Gravity.BOTTOM;
      wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
      wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
      window.setAttributes(wlp);
    }
    setContentView(rootView);
    setCanceledOnTouchOutside(true);
    setCancelable(true);
    setOnDismissListener(dialog -> callback = null);
  }

  /** 页面渲染 */
  private void renderRootView() {
    takePhotoView.setOnClickListener(
        view -> {
          onTakePhotoClick(view);
        });
    getFromAlbumView.setOnClickListener(
        view -> {
          onGetFromAlbumClick(view);
        });
    cancelView.setOnClickListener(v -> dismiss());
  }

  public void onGetFromAlbumClick(View view) {
    openSystemAlbum();
  }

  public void onTakePhotoClick(View view) {
    openCamera();
  }

  public void openSystemAlbum() {
    if (!NetworkUtils.isConnected()) {
      Toast.makeText(getContext(), R.string.common_network_error, Toast.LENGTH_SHORT).show();
      return;
    }
    CommonCallback<File> localCallback = callback;
    BasePhotoChoiceDialog.this.dismiss();
    // 根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_IMAGES
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissionForAlbum =
          new String[] {
            Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
          };
    } else {
      permissionForAlbum =
          new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
          };
    }
    if (PermissionUtils.hasPermissions(getContext(), permissionForAlbum)) {
      PhotoPicker.getInstance().getAPhotoFromAlbumCropAndUpload(getContext(), localCallback);
      return;
    }
    Permission.requirePermissions(getContext(), permissionForAlbum)
        .request(
            new Permission.PermissionCallback() {
              @Override
              public void onGranted(List<String> permissionsGranted) {
                if (new HashSet<>(permissionsGranted)
                    .containsAll(Arrays.asList(permissionForAlbum))) {
                  PhotoPicker.getInstance()
                      .getAPhotoFromAlbumCropAndUpload(getContext(), localCallback);
                } else {
                  Toast.makeText(
                          getContext(),
                          getContext().getResources().getString(R.string.dialog_permission_tips),
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onDenial(
                  List<String> permissionsDenial, List<String> permissionDenialForever) {
                // 兼容Android13 部分允许的权限
                if (PermissionUtils.checkImageOrFilePermission(getContext())) {
                  PhotoPicker.getInstance()
                      .getAPhotoFromAlbumCropAndUpload(getContext(), localCallback);
                } else {
                  Toast.makeText(
                          getContext(),
                          getContext().getResources().getString(R.string.dialog_permission_tips),
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onException(Exception exception) {
                Toast.makeText(
                        getContext(),
                        getContext().getResources().getString(R.string.dialog_permission_tips),
                        Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  public void openCamera() {
    if (!NetworkUtils.isConnected()) {
      Toast.makeText(getContext(), R.string.common_network_error, Toast.LENGTH_SHORT).show();
      return;
    }
    CommonCallback<File> localCallback = callback;
    BasePhotoChoiceDialog.this.dismiss();
    if (PermissionUtils.hasPermissions(getContext(), permissionForCamera)) {
      PhotoPicker.getInstance().takePhotoCorpAndUpload(getContext(), localCallback);
      return;
    }
    Permission.requirePermissions(getContext(), permissionForCamera)
        .request(
            new Permission.PermissionCallback() {
              @Override
              public void onGranted(List<String> permissionsGranted) {
                if (permissionsGranted != null
                    && new HashSet<>(permissionsGranted)
                        .containsAll(Arrays.asList(permissionForCamera))) {
                  PhotoPicker.getInstance().takePhotoCorpAndUpload(getContext(), localCallback);
                } else {
                  Toast.makeText(
                          getContext(),
                          getContext().getResources().getString(R.string.dialog_permission_tips),
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onDenial(
                  List<String> permissionsDenial, List<String> permissionDenialForever) {
                Toast.makeText(
                        getContext(),
                        getContext().getResources().getString(R.string.dialog_permission_tips),
                        Toast.LENGTH_SHORT)
                    .show();
              }

              @Override
              public void onException(Exception exception) {
                Toast.makeText(
                        getContext(),
                        getContext().getResources().getString(R.string.dialog_permission_tips),
                        Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  public void show(CommonCallback<File> callback) {
    if (isShowing()) {
      return;
    }
    this.callback = callback;
    renderRootView();
    try {
      super.show();
    } catch (Throwable throwable) {
    }
  }

  public void dismiss() {
    if (!isShowing()) {
      return;
    }
    try {
      super.dismiss();
    } catch (Throwable throwable) {
    }
  }

  public void onSuccess(File file) {
    if (callback != null) {
      callback.onSuccess(file);
    }
    dismiss();
  }

  public void onFailed(int code) {
    if (callback != null) {
      callback.onFailed(code);
    }
    dismiss();
  }

  public void onException(Throwable throwable) {
    if (callback != null) {
      callback.onException(throwable);
    }
    dismiss();
  }
}
