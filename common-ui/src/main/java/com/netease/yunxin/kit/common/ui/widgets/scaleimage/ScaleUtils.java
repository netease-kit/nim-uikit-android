// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.widgets.scaleimage;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class ScaleUtils {

  static void checkZoomLevels(float minZoom, float midZoom, float maxZoom) {
    if (minZoom >= midZoom) {
      throw new IllegalArgumentException(
          "Minimum zoom has to be less than Medium zoom. Call setMinimumZoom() with a more appropriate value");
    } else if (midZoom >= maxZoom) {
      throw new IllegalArgumentException(
          "Medium zoom has to be less than Maximum zoom. Call setMaximumZoom() with a more appropriate value");
    }
  }

  static boolean hasDrawable(ImageView imageView) {
    return imageView.getDrawable() != null;
  }

  static boolean isSupportedScaleType(final ImageView.ScaleType scaleType) {
    if (scaleType == null) {
      return false;
    }
    switch (scaleType) {
      case MATRIX:
        throw new IllegalStateException("Matrix scale type is not supported");
    }
    return true;
  }

  static int getPointerIndex(int action) {
    return (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
  }

  private static final int SIXTY_FPS_INTERVAL = 1000 / 60;

  public static void postOnAnimation(View view, Runnable runnable) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      postOnAnimationJellyBean(view, runnable);
    } else {
      view.postDelayed(runnable, SIXTY_FPS_INTERVAL);
    }
  }

  @TargetApi(16)
  private static void postOnAnimationJellyBean(View view, Runnable runnable) {
    view.postOnAnimation(runnable);
  }
}
