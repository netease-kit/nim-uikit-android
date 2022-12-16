// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.app.Activity;
import android.graphics.Rect;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.netease.yunxin.kit.common.utils.BarUtils;
import com.netease.yunxin.kit.common.utils.ScreenUtils;

public class ActivityWorkaround {

  // For more information, see https://code.google.com/p/android/issues/detail?id=5497

  private FrameLayout mContent;
  private int usableHeightPrevious;
  private LinearLayout.LayoutParams mContentLp;
  private OnKeyboardStateChangeListener mListener;
  private int mStatusBarHeight;
  private boolean isImmersiveTitle;

  public static ActivityWorkaround assistActivity(Activity activity, boolean isImmersiveTitle) {
    return new ActivityWorkaround(activity, isImmersiveTitle);
  }

  private ActivityWorkaround(Activity activity, boolean isImmersiveTitle) {
    this.isImmersiveTitle = isImmersiveTitle;
    setStatusBarHeight(activity);

    mContent = activity.findViewById(android.R.id.content);
    if (mContent != null) {
      mContent
          .getViewTreeObserver()
          .addOnGlobalLayoutListener(
              () -> {
                setStatusBarHeight(activity);

                possiblyResizeChildOfContent();
              });
      if (mContent.getLayoutParams() != null) {
        mContentLp = (LinearLayout.LayoutParams) mContent.getLayoutParams();
      } else {
        mContentLp =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      }
    }
  }

  private void possiblyResizeChildOfContent() {
    Rect r = new Rect();
    mContent.getWindowVisibleDisplayFrame(r);
    int usableHeightNow = r.bottom;
    if (usableHeightNow != usableHeightPrevious) {
      int usableHeightSansKeyboard = ScreenUtils.getDisplayHeight();
      int heightDifference = usableHeightSansKeyboard - usableHeightNow;
      if (heightDifference > (usableHeightSansKeyboard / 4)) {
        mContentLp.height = usableHeightSansKeyboard - heightDifference - mStatusBarHeight;
        mContent.setLayoutParams(mContentLp);
        if (mListener != null) {
          mListener.showKeyboard(mContentLp.height);
        }
      } else {
        mContentLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mContent.setLayoutParams(mContentLp);
        mContent.postDelayed(
            () -> {
              if (mListener != null && mContent != null) {
                mListener.hideKeyboard(mContent.getMeasuredHeight());
              }
            },
            100);
      }
      usableHeightPrevious = usableHeightNow;
    }
  }

  private void setStatusBarHeight(Activity activity) {
    mStatusBarHeight = BarUtils.getStatusBarHeight(activity);
    if (isImmersiveTitle || isFullScreen(activity)) {
      mStatusBarHeight = 0;
    }
  }

  private static boolean isFullScreen(Activity activity) {
    if (activity == null
        || activity.getWindow() == null
        || activity.getWindow().getAttributes() == null) {
      return false;
    }
    int flag = activity.getWindow().getAttributes().flags;
    return (flag & WindowManager.LayoutParams.FLAG_FULLSCREEN)
        == WindowManager.LayoutParams.FLAG_FULLSCREEN;
  }

  public interface OnKeyboardStateChangeListener {
    void showKeyboard(int visibleHeight);

    void hideKeyboard(int visibleHeight);
  }

  public void setOnKeyboardStateChangeListener(OnKeyboardStateChangeListener listener) {
    mListener = listener;
  }
}
