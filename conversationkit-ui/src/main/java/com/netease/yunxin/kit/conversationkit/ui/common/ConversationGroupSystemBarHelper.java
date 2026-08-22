// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.common;

import android.app.Activity;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.ColorInt;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public final class ConversationGroupSystemBarHelper {

  private ConversationGroupSystemBarHelper() {}

  @SuppressWarnings("deprecation")
  public static void apply(Activity activity, @ColorInt int color) {
    Window window = activity.getWindow();
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    window.setStatusBarColor(color);
    window.setNavigationBarColor(color);

    WindowInsetsControllerCompat insetsController =
        WindowCompat.getInsetsController(window, window.getDecorView());
    insetsController.setAppearanceLightNavigationBars(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      window.setNavigationBarDividerColor(color);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      window.setNavigationBarContrastEnforced(false);
    }
  }
}
