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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import com.netease.yunxin.kit.common.ui.R;

/** radius TextView */
public class RoundTextView extends AppCompatTextView {

  private Path path;
  private Paint paint;
  private RectF rectF;
  private float topRadius = 0;
  private float bottomRadius = 0;

  public RoundTextView(@NonNull Context context) {
    this(context, null);
  }

  public RoundTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RoundTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
    float[] radiusArray =
        new float[] {
          topRadius,
          topRadius,
          topRadius,
          topRadius,
          bottomRadius,
          bottomRadius,
          bottomRadius,
          bottomRadius
        };
    path.addRoundRect(rectF, radiusArray, Path.Direction.CW);
    return path;
  }

  public void setRadius(float radius) {
    topRadius = radius;
    bottomRadius = radius;
  }

  public void setTopRadius(float radius) {
    topRadius = radius;
  }

  public void setBottomRadius(float bottomRadius) {
    this.bottomRadius = bottomRadius;
  }
}
