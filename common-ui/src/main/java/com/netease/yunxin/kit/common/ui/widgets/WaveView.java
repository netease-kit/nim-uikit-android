// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class WaveView extends View {
  private Paint paint;
  private int maxWidth = 90;
  // 是否运行
  private boolean isStarting = false;
  private List<String> alphaList = new ArrayList<>();
  private List<Float> startWidthList = new ArrayList<>();

  public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public WaveView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public WaveView(Context context) {
    super(context);
    init();
  }

  private void init() {
    paint = new Paint();
    // 设置颜色
    paint.setColor(Color.parseColor("#FFFFFF"));
    paint.setStyle(Paint.Style.STROKE);
    // paint.setColor(Color.argb(153,19,154,255));
    alphaList.add("153"); // 圆心的不透明度
    startWidthList.add(0F);
  }

  /**
   * 设置波纹颜色
   *
   * @param alpha 透明度
   * @param red red
   * @param green green
   * @param blue blue
   */
  public void setWaveColor(int alpha, int red, int green, int blue) {
    if (paint != null) {
      paint.setColor(Color.argb(alpha, red, green, blue));
    }
    invalidate();
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    //setBackgroundColor(Color.RED);// 颜色：完全透明
    //setBackground(getResources().getDrawable(R.drawable.circle_shape));
    // 依次绘制 同心圆
    int length = alphaList.size();
    for (int i = 0; i < length; i++) {
      int alpha = Integer.parseInt(alphaList.get(i));
      // 圆半径
      float startWidth = startWidthList.get(i);
      paint.setAlpha(alpha);
      paint.setStrokeWidth(2.5F);
      // 这个半径决定你想要多大的扩散面积
      canvas.drawCircle(getWidth() / 2, getHeight() / 2, startWidth + 10F, paint);
      // 同心圆扩散
      if (isStarting && alpha > 0 && startWidth < maxWidth) {
        alphaList.set(i, (alpha - 1) + "");
        startWidthList.set(i, (startWidth + 0.5F));
      }
    }
    if (isStarting && startWidthList.get(startWidthList.size() - 1) == maxWidth / 5) {
      alphaList.add("153");
      startWidthList.add(0F);
    }
    // 同心圆数量达到5个，删除最外层圆
    if (isStarting && startWidthList.size() == 5) {
      startWidthList.remove(0);
      alphaList.remove(0);
    }
    // 刷新界面
    invalidate();
  }

  // 执行动画
  public void start() {
    isStarting = true;
  }

  // 停止动画
  public void stop() {
    isStarting = false;
  }

  // 判断是都在不在执行
  public boolean isStarting() {
    return isStarting;
  }
}
