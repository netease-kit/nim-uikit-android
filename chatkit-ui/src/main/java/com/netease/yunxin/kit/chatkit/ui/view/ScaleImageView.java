// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import androidx.appcompat.widget.AppCompatImageView;

/** 自定义ImageView，支持缩放和拖动 */
public class ScaleImageView extends AppCompatImageView {

  private Matrix matrix = new Matrix();
  private ScaleGestureDetector scaleDetector;
  private float lastX, lastY;
  private boolean allPointersUp;
  // 支持缩放倍数
  private float scaleSize = 1f;

  public ScaleImageView(Context context) {
    this(context, null);
  }

  public ScaleImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ScaleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    setScaleType(ScaleType.MATRIX);
  }

  public void centerImage() {
    postDelayed(
        () -> {
          reloadCenterImage();
          invalidate(); // 触发重绘
        },
        500);
  }

  private void reloadCenterImage() {
    Drawable drawable = getDrawable();
    if (drawable == null) return;

    // 获取图片和容器尺寸
    int imgWidth = drawable.getIntrinsicWidth();
    int imgHeight = drawable.getIntrinsicHeight();
    int viewWidth = getWidth();
    int viewHeight = getHeight();

    // 计算平移量
    float dx = (viewWidth - imgWidth) / 2f;
    float dy = (viewHeight - imgHeight) / 2f;

    matrix.postTranslate(dx, dy); // 应用初始平移
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    scaleDetector.onTouchEvent(event);
    final int action = event.getActionMasked();
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        lastX = event.getX();
        lastY = event.getY();
        allPointersUp = false;
        break;
      case MotionEvent.ACTION_POINTER_DOWN:
        allPointersUp = true;
        break;
      case MotionEvent.ACTION_MOVE:
        if (!allPointersUp) {
          float dx = event.getX() - lastX;
          float dy = event.getY() - lastY;
          matrix.postTranslate(dx, dy);
          setImageMatrix(matrix);
          lastX = event.getX();
          lastY = event.getY();
        }
        break;
    }
    return true;
  }

  private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      float scaleFactorChange = detector.getScaleFactor();
      // 获取ImageView的中心点作为缩放中心点
      float px = getWidth() / scaleSize;
      float py = getHeight() / scaleSize;
      // 在ImageView的中心点进行缩放
      matrix.postScale(scaleFactorChange, scaleFactorChange, px, py);
      // 应用新的矩阵变换
      setImageMatrix(matrix);
      return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
      super.onScaleEnd(detector);
    }
  }
}
