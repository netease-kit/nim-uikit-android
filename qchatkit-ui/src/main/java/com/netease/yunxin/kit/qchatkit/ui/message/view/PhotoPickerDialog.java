// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.view;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.permission.PermissionUtils;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatDialogPhotoChoiceBinding;
import java.util.Arrays;
import java.util.List;

public class PhotoPickerDialog extends Dialog {
  private static final String TAG = "PhotoChoiceDialog";
  private QChatDialogPhotoChoiceBinding binding;
  private FetchCallback<Integer> callback;
  private final String[] permissionForCamera = new String[] {Manifest.permission.CAMERA};
  private final String[] permissionForAlbum =
      new String[] {
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
      };

  public PhotoPickerDialog(@NonNull Activity activity) {
    super(activity, R.style.BottomDialogTheme);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Window window = getWindow();
    if (window != null) {
      WindowManager.LayoutParams wlp = window.getAttributes();
      wlp.gravity = Gravity.BOTTOM;
      wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
      wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
      window.setAttributes(wlp);
    }
    setContentView(binding.getRoot());
    setCanceledOnTouchOutside(true);
    setCancelable(true);

    setOnDismissListener(dialog -> callback = null);
  }

  /** 页面渲染 */
  private void renderRootView() {
    binding = QChatDialogPhotoChoiceBinding.inflate(getLayoutInflater());
    binding.tvTakePhoto.setOnClickListener(
        v -> {
          FetchCallback<Integer> localCallback = callback;
          PhotoPickerDialog.this.dismiss();
          if (PermissionUtils.checkPermission(getContext(), permissionForCamera)) {
            localCallback.onSuccess(0);
            return;
          }
          PermissionUtils.requirePermissions(getContext(), permissionForCamera)
              .request(
                  new PermissionUtils.PermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                      PhotoPickerDialog.this.dismiss();
                      if (permissionsGranted.containsAll(Arrays.asList(permissionForCamera))) {
                        localCallback.onSuccess(0);
                      } else {
                        Toast.makeText(
                                getContext(), R.string.dialog_permission_tips, Toast.LENGTH_SHORT)
                            .show();
                      }
                    }

                    @Override
                    public void onDenial(
                        List<String> permissionsDenial, List<String> permissionDenialForever) {
                      PhotoPickerDialog.this.dismiss();
                      Toast.makeText(
                              getContext(), R.string.dialog_permission_tips, Toast.LENGTH_SHORT)
                          .show();
                    }

                    @Override
                    public void onException(Exception exception) {
                      PhotoPickerDialog.this.dismiss();
                      Toast.makeText(
                              getContext(), R.string.dialog_permission_tips, Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
        });
    binding.tvGetFromAlbum.setOnClickListener(
        v -> {
          FetchCallback<Integer> localCallback = callback;
          PhotoPickerDialog.this.dismiss();
          if (PermissionUtils.checkPermission(getContext(), permissionForAlbum)) {
            localCallback.onSuccess(1);
            return;
          }
          PermissionUtils.requirePermissions(getContext(), permissionForAlbum)
              .request(
                  new PermissionUtils.PermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                      if (permissionsGranted.containsAll(Arrays.asList(permissionForAlbum))) {
                        localCallback.onSuccess(1);
                      } else {
                        Toast.makeText(
                                getContext(), R.string.dialog_permission_tips, Toast.LENGTH_SHORT)
                            .show();
                      }
                    }

                    @Override
                    public void onDenial(
                        List<String> permissionsDenial, List<String> permissionDenialForever) {
                      Toast.makeText(
                              getContext(), R.string.dialog_permission_tips, Toast.LENGTH_SHORT)
                          .show();
                    }

                    @Override
                    public void onException(Exception exception) {
                      Toast.makeText(
                              getContext(), R.string.dialog_permission_tips, Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
        });
    binding.tvCancel.setOnClickListener(v -> dismiss());
  }

  public void show(FetchCallback<Integer> callback) {
    if (isShowing()) {
      return;
    }
    this.callback = callback;
    renderRootView();
    try {
      super.show();
    } catch (Throwable throwable) {
      ALog.e(TAG, "show PhotoChoiceDialog", throwable);
    }
  }

  public void dismiss() {
    if (!isShowing()) {
      return;
    }
    try {
      super.dismiss();
    } catch (Throwable throwable) {
      ALog.e(TAG, "dismiss PhotoChoiceDialog", throwable);
    }
  }
}
