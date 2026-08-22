// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.input;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;
import com.netease.yunxin.kit.chatkit.ui.R;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Lightweight animations for chat input panels. */
public final class InputPanelAnimator {

  private static final long PANEL_ANIMATION_DURATION = 300L;
  private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
  private static final AtomicInteger ANIMATION_TOKEN = new AtomicInteger();

  private InputPanelAnimator() {}

  public static void showPanel(@NonNull View panel) {
    int token = markPanelRequest(panel);
    panel.animate().cancel();
    int targetHeight = getPanelTargetHeight(panel);
    setPanelHeight(panel, 0);
    panel.setVisibility(View.VISIBLE);
    panel.post(
        () -> {
          if (!isCurrentPanelRequest(panel, token)) {
            return;
          }
          animatePanelHeight(panel, 0, targetHeight, token, () -> restorePanelHeight(panel));
        });
  }

  public static void hidePanel(@NonNull View panel) {
    int token = markPanelRequest(panel);
    panel.animate().cancel();
    if (panel.getVisibility() != View.VISIBLE) {
      restorePanelHeight(panel);
      panel.setVisibility(View.GONE);
      return;
    }
    int startHeight = getCurrentPanelHeight(panel);
    animatePanelHeight(
        panel,
        startHeight,
        0,
        token,
        () -> {
          panel.setVisibility(View.GONE);
          restorePanelHeight(panel);
        });
  }

  public static void hidePanelImmediately(@NonNull View panel) {
    markPanelRequest(panel);
    panel.animate().cancel();
    panel.setVisibility(View.GONE);
    restorePanelHeight(panel);
  }

  public static void setupKeyboardTransition(@NonNull View inputRoot) {
    ViewCompat.setWindowInsetsAnimationCallback(
        inputRoot,
        new WindowInsetsAnimationCompat.Callback(
            WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
          private int targetKeyboardHeight;
          private int lastKeyboardHeight;

          @NonNull
          @Override
          public WindowInsetsAnimationCompat.BoundsCompat onStart(
              @NonNull WindowInsetsAnimationCompat animation,
              @NonNull WindowInsetsAnimationCompat.BoundsCompat bounds) {
            if ((animation.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0) {
              targetKeyboardHeight = Math.max(targetKeyboardHeight, bounds.getUpperBound().bottom);
            }
            return bounds;
          }

          @NonNull
          @Override
          public WindowInsetsCompat onProgress(
              @NonNull WindowInsetsCompat insets,
              @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
            boolean hasImeAnimation = false;
            for (WindowInsetsAnimationCompat animation : runningAnimations) {
              if ((animation.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0) {
                hasImeAnimation = true;
                break;
              }
            }
            if (!hasImeAnimation) {
              return insets;
            }
            int keyboardHeight =
                Math.max(0, insets.getInsets(WindowInsetsCompat.Type.ime()).bottom);
            targetKeyboardHeight = Math.max(targetKeyboardHeight, keyboardHeight);
            if (keyboardHeight >= lastKeyboardHeight) {
              inputRoot.setTranslationY(Math.max(0, targetKeyboardHeight - keyboardHeight));
            } else {
              inputRoot.setTranslationY(0f);
            }
            lastKeyboardHeight = keyboardHeight;
            return insets;
          }

          @Override
          public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
            if ((animation.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0) {
              inputRoot.setTranslationY(0f);
              targetKeyboardHeight = 0;
              lastKeyboardHeight = 0;
            }
          }
        });
  }

  private static void resetPanelTransform(@NonNull View panel) {
    panel.setTranslationY(0f);
    panel.setAlpha(1f);
  }

  private static void animatePanelHeight(
      @NonNull View panel, int startHeight, int endHeight, int token, @NonNull Runnable endAction) {
    animateViewHeight(panel, startHeight, endHeight, PANEL_ANIMATION_DURATION, token, endAction);
  }

  private static void animateViewHeight(
      @NonNull View panel,
      int startHeight,
      int endHeight,
      long duration,
      int token,
      @NonNull Runnable endAction) {
    ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
    animator.setDuration(duration);
    animator.setInterpolator(DECELERATE_INTERPOLATOR);
    animator.addUpdateListener(
        animation -> {
          if (!isCurrentPanelRequest(panel, token)) {
            animation.cancel();
            return;
          }
          setPanelHeight(panel, (Integer) animation.getAnimatedValue());
        });
    animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            if (!isCurrentPanelRequest(panel, token)) {
              return;
            }
            endAction.run();
          }
        });
    animator.start();
  }

  private static int getPanelTargetHeight(@NonNull View panel) {
    ViewGroup.LayoutParams params = panel.getLayoutParams();
    int originHeight = getOriginHeight(panel, params);
    if (originHeight > 0) {
      return originHeight;
    }
    ViewParent parent = panel.getParent();
    int parentWidth = parent instanceof View ? ((View) parent).getWidth() : panel.getWidth();
    int widthSpec =
        View.MeasureSpec.makeMeasureSpec(Math.max(0, parentWidth), View.MeasureSpec.EXACTLY);
    int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    panel.measure(widthSpec, heightSpec);
    return panel.getMeasuredHeight() > 0
        ? panel.getMeasuredHeight()
        : Math.round(dpToPx(panel, 188f));
  }

  private static int getOriginHeight(@NonNull View panel, ViewGroup.LayoutParams params) {
    Object originHeight = panel.getTag(R.id.chat_input_panel_origin_height);
    if (originHeight instanceof Integer) {
      return (Integer) originHeight;
    }
    int height = params != null ? params.height : ViewGroup.LayoutParams.WRAP_CONTENT;
    panel.setTag(R.id.chat_input_panel_origin_height, height);
    return height;
  }

  private static void setPanelHeight(@NonNull View panel, int height) {
    ViewGroup.LayoutParams params = panel.getLayoutParams();
    if (params == null) {
      return;
    }
    params.height = Math.max(0, height);
    panel.setLayoutParams(params);
  }

  private static int getCurrentPanelHeight(@NonNull View panel) {
    ViewGroup.LayoutParams params = panel.getLayoutParams();
    if (params != null && params.height >= 0) {
      return params.height;
    }
    return panel.getHeight() > 0 ? panel.getHeight() : getPanelTargetHeight(panel);
  }

  private static void restorePanelHeight(@NonNull View panel) {
    ViewGroup.LayoutParams params = panel.getLayoutParams();
    if (params == null) {
      return;
    }
    Object originHeight = panel.getTag(R.id.chat_input_panel_origin_height);
    if (originHeight instanceof Integer) {
      params.height = (Integer) originHeight;
      panel.setLayoutParams(params);
    }
    resetPanelTransform(panel);
  }

  private static int markPanelRequest(@NonNull View panel) {
    int token = ANIMATION_TOKEN.incrementAndGet();
    panel.setTag(R.id.chat_input_panel_animation_token, token);
    return token;
  }

  private static boolean isCurrentPanelRequest(@NonNull View panel, int token) {
    Object currentToken = panel.getTag(R.id.chat_input_panel_animation_token);
    return currentToken instanceof Integer && ((Integer) currentToken) == token;
  }

  private static float dpToPx(@NonNull View view, float dp) {
    return dp * view.getResources().getDisplayMetrics().density;
  }
}
