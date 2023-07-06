// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.userinfo;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunContactBottomConfirmDialogBinding;

public class FunContactBottomConfirmDialog extends Dialog {
  private static final String TAG = "FunBottomConfirmDialog";

  private Runnable confirmRunnable;
  private FunContactBottomConfirmDialogBinding binding;

  public FunContactBottomConfirmDialog(@NonNull Activity activity) {
    super(activity, R.style.FunContactBottomDialogTheme);
    renderRootView();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Window window = getWindow();
    if (window != null) {
      WindowManager.LayoutParams wlp = window.getAttributes();
      wlp.gravity = Gravity.BOTTOM;
      wlp.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
      wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
      wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
      window.setAttributes(wlp);
    }
    setContentView(binding.getRoot());
    setCanceledOnTouchOutside(true);
    setCancelable(true);

    setOnDismissListener(dialog -> confirmRunnable = null);
  }

  /** render page */
  private void renderRootView() {
    binding = FunContactBottomConfirmDialogBinding.inflate(getLayoutInflater());
    binding.tvDelete.setOnClickListener(
        v -> {
          if (confirmRunnable != null) {
            confirmRunnable.run();
          }
          dismiss();
        });
    binding.tvCancel.setOnClickListener(v -> dismiss());
  }

  public FunContactBottomConfirmDialog configTip(String tip) {
    binding.tvTip.setText(tip);
    return this;
  }

  public void show(Runnable confirmRunnable) {
    if (isShowing()) {
      return;
    }
    this.confirmRunnable = confirmRunnable;
    try {
      super.show();
    } catch (Throwable throwable) {
      ALog.e(TAG, "show FunBottomConfirmDialog", throwable);
    }
  }

  public void dismiss() {
    if (!isShowing()) {
      return;
    }
    try {
      super.dismiss();
    } catch (Throwable throwable) {
      ALog.e(TAG, "dismiss FunBottomConfirmDialog", throwable);
    }
  }
}
