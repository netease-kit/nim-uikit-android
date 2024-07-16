// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.ui.R;

/** radius FrameLayout */
public class RoundFrameLayout extends FrameLayout {

  private Path path;
  private Paint paint;
  private RectF rectF;
  private float topRadius = -1;
  private float bottomRadius = -1;
  private float setTopRadius = -1;
  private float setBottomRadius = -1;

  public RoundFrameLayout(@NonNull Context context) {
    this(context, null);
  }

  public RoundFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RoundFrameLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundFrameLayout);
    float radius = typedArray.getDimension(R.styleable.RoundFrameLayout_corner_radius, 0f);
    topRadius = typedArray.getDimension(R.styleable.RoundFrameLayout_corner_topRadius, radius);
    bottomRadius =
        typedArray.getDimension(R.styleable.RoundFrameLayout_corner_bottomRadius, radius);
    init();
  }

  private void init() {
    path = new Path();
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    rectF = new RectF();
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    rectF.set(0, 0, w, h);
  }

  @Override
  public void draw(Canvas canvas) {
    canvas.save();
    canvas.clipPath(getPath());
    super.draw(canvas);
    canvas.restore();
  }

  private Path getPath() {
    path.reset();
    float top = topRadius;
    float bottom = bottomRadius;
    if (setTopRadius > -1 || setBottomRadius > -1) {
      top = setTopRadius > -1 ? setTopRadius : 0;
      bottom = setBottomRadius > -1 ? setBottomRadius : 0;
    }
    float[] radiusArray = new float[] {top, top, top, top, bottom, bottom, bottom, bottom};

    path.addRoundRect(rectF, radiusArray, Path.Direction.CW);
    return path;
  }

  public void setRadius(float radius) {
    setTopRadius = radius;
    setBottomRadius = radius;
  }

  public void setTopRadius(float radius) {
    setTopRadius = radius;
  }

  public void setBottomRadius(float bottomRadius) {
    this.setBottomRadius = bottomRadius;
  }
}
