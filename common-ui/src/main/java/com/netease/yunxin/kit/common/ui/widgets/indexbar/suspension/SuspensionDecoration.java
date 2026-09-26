// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.widgets.indexbar.suspension;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.R;
import java.util.List;

/** custom decoration for Suspension */
public class SuspensionDecoration extends RecyclerView.ItemDecoration {

  private int colorTitleBg;
  private int colorTitleBottomLine;
  private int colorTitleFont;
  private int indexDecorationBg;
  private int mTitleFontSize;
  private int bottomLineHeight;
  private List<? extends ISuspension> mData;
  private final Paint mPaint;
  private final Rect mBounds; //rect for restore text for measure
  private int mTitleHeight;
  private int mHeaderViewCount = 0;

  private float paddingLeft;
  private boolean isFirstTagOffset = true;
  private boolean isFirstTagDraw = true;
  private final int indexDecorationHeight;

  public SuspensionDecoration(Context context, List<? extends ISuspension> datas) {
    super();
    mData = datas;
    mPaint = new Paint();
    mBounds = new Rect();
    colorTitleBg = context.getResources().getColor(R.color.color_ffffff);
    colorTitleBottomLine = context.getResources().getColor(R.color.color_deb0e8);
    colorTitleFont = context.getResources().getColor(R.color.color_b3b7bc);
    indexDecorationBg = context.getResources().getColor(R.color.color_eff1f4);
    mTitleHeight =
        (int)
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 33f, context.getResources().getDisplayMetrics());
    indexDecorationHeight =
        (int)
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 6f, context.getResources().getDisplayMetrics());
    mTitleFontSize =
        (int)
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 14f, context.getResources().getDisplayMetrics());
    bottomLineHeight =
        (int)
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1f, context.getResources().getDisplayMetrics());
    mPaint.setTextSize(mTitleFontSize);
    mPaint.setAntiAlias(true);
  }

  public SuspensionDecoration setTitleHeight(int mTitleHeight) {
    this.mTitleHeight = mTitleHeight;
    return this;
  }

  public SuspensionDecoration setBottomLineHeight(int bottomLineHeight) {
    this.bottomLineHeight = bottomLineHeight;
    return this;
  }

  public SuspensionDecoration setIndexDecorationBg(int indexDecorationBg) {
    this.indexDecorationBg = indexDecorationBg;
    return this;
  }

  public SuspensionDecoration setColorTitleBg(int colorTitleBg) {
    this.colorTitleBg = colorTitleBg;
    return this;
  }

  public SuspensionDecoration setColorTitleFont(int colorTitleFont) {
    this.colorTitleFont = colorTitleFont;
    return this;
  }

  public SuspensionDecoration setTitleFontSize(int mTitleFontSize) {
    this.mTitleFontSize = mTitleFontSize;
    mPaint.setTextSize(mTitleFontSize);
    return this;
  }

  public SuspensionDecoration setColorTitleBottomLine(int colorTitleBottomLine) {
    this.colorTitleBottomLine = colorTitleBottomLine;
    return this;
  }

  public SuspensionDecoration setPaddingLeft(float paddingLeft) {
    this.paddingLeft = paddingLeft;
    return this;
  }

  public SuspensionDecoration setData(List<? extends ISuspension> mData) {
    this.mData = mData;
    return this;
  }

  public int getHeaderViewCount() {
    return mHeaderViewCount;
  }

  public SuspensionDecoration setHeaderViewCount(int headerViewCount) {
    mHeaderViewCount = headerViewCount;
    return this;
  }

  @Override
  public void onDraw(
      @NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
    super.onDraw(c, parent, state);
    final int left = parent.getPaddingLeft();
    final int right = parent.getWidth() - parent.getPaddingRight();
    final int childCount = parent.getChildCount();
    isFirstTagDraw = true;
    for (int i = 0; i < childCount; i++) {
      final View child = parent.getChildAt(i);
      final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
      int position = params.getViewLayoutPosition();
      position -= getHeaderViewCount();
      if (mData == null
          || mData.isEmpty()
          || position > mData.size() - 1
          || position < 0
          || !mData.get(position).isShowDivision()) {
        continue;
      }

      if (position == 0) { //if 0 must have title
        drawTitleArea(c, left, right, child, params, position);
      } else {
        if (null != mData.get(position).getTag()
            && !mData.get(position).getTag().equals(mData.get(position - 1).getTag())) {
          //not empty and different with pre
          drawTitleArea(c, left, right, child, params, position);
        }
      }
    }
  }

  /** draw title area */
  private void drawTitleArea(
      Canvas c,
      int left,
      int right,
      View child,
      RecyclerView.LayoutParams params,
      int position) { //最先调用，绘制在最下层
    if (isFirstTagDraw) {
      mPaint.setColor(indexDecorationBg);
      c.drawRect(
          left,
          child.getTop() - params.topMargin - mTitleHeight - indexDecorationHeight,
          right,
          child.getTop() - params.topMargin - mTitleHeight,
          mPaint);
    }
    isFirstTagDraw = false;

    mPaint.setColor(colorTitleBg);
    c.drawRect(
        left,
        child.getTop() - params.topMargin - mTitleHeight,
        right,
        child.getTop() - params.topMargin,
        mPaint);

    mPaint.setColor(colorTitleBottomLine);
    c.drawRect(
        left + paddingLeft,
        child.getTop() - params.topMargin - bottomLineHeight,
        right,
        child.getTop() - params.topMargin,
        mPaint);

    mPaint.setColor(colorTitleFont);
    mPaint.getTextBounds(
        mData.get(position).getTag(), 0, mData.get(position).getTag().length(), mBounds);
    c.drawText(
        mData.get(position).getTag(),
        child.getPaddingLeft() + paddingLeft,
        child.getTop()
            - params.topMargin
            - ((float) mTitleHeight / 2 - (float) mBounds.height() / 2),
        mPaint);
  }

  @Override
  public void onDrawOver(
      @NonNull Canvas c, final RecyclerView parent, @NonNull RecyclerView.State state) {
    if (parent.getLayoutManager() == null) {
      return;
    }
    int pos = ((LinearLayoutManager) (parent.getLayoutManager())).findFirstVisibleItemPosition();
    pos -= getHeaderViewCount();
    if (mData == null
        || mData.isEmpty()
        || pos > mData.size() - 1
        || pos < 0
        || !mData.get(pos).isSuspension()) {
      return;
    }

    String tag = mData.get(pos).getTag();
    RecyclerView.ViewHolder viewHolder =
        parent.findViewHolderForLayoutPosition(pos + getHeaderViewCount());
    if (viewHolder == null) {
      return;
    }
    View child = viewHolder.itemView;
    boolean flag = false; //flag for canvas have moved
    if ((pos + 1) < mData.size()) {
      if (null != tag && !tag.equals(mData.get(pos + 1).getTag())) {
        if (child.getHeight() + child.getTop() < mTitleHeight) {
          c.save();
          flag = true;

          c.translate(0, child.getHeight() + child.getTop() - mTitleHeight);
        }
      }
    }
    mPaint.setColor(colorTitleBg);
    c.drawRect(
        parent.getPaddingLeft(),
        parent.getPaddingTop(),
        parent.getRight() - parent.getPaddingRight(),
        parent.getPaddingTop() + mTitleHeight,
        mPaint);
    mPaint.setColor(colorTitleFont);
    mPaint.getTextBounds(tag, 0, tag == null ? 0 : tag.length(), mBounds);
    c.drawText(
        tag,
        child.getPaddingLeft() + 40,
        parent.getPaddingTop()
            + mTitleHeight
            - ((float) mTitleHeight / 2 - (float) mBounds.height() / 2),
        mPaint);
    if (flag) c.restore();
  }

  @Override
  public void getItemOffsets(
      @NonNull Rect outRect,
      @NonNull View view,
      @NonNull RecyclerView parent,
      @NonNull RecyclerView.State state) {
    super.getItemOffsets(outRect, view, parent, state);
    int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
    position -= getHeaderViewCount();
    if (mData == null || mData.isEmpty() || position > mData.size() - 1) {
      return;
    }
    if (position >= 0) {
      ISuspension titleCategoryInterface = mData.get(position);
      if (titleCategoryInterface.isShowDivision()) {
        if (position == 0) {
          outRect.set(0, mTitleHeight + (isFirstTagOffset ? indexDecorationHeight : 0), 0, 0);
          isFirstTagOffset = false;
        } else {
          if (null != titleCategoryInterface.getTag()
              && !titleCategoryInterface.getTag().equals(mData.get(position - 1).getTag())) {
            outRect.set(0, mTitleHeight + (isFirstTagOffset ? indexDecorationHeight : 0), 0, 0);
            isFirstTagOffset = false;
          }
        }
      }
    }
  }
}
