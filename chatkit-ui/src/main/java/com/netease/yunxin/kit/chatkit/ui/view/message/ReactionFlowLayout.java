// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @deprecated Use {@link ReactionGroupView}. This class is retained for source compatibility and is
 *     no longer used by the message ViewHolder.
 */
@Deprecated
public class ReactionFlowLayout extends ViewGroup {
  private final int rowSpacing;

  public ReactionFlowLayout(@NonNull Context context, int rowSpacing) {
    super(context);
    this.rowSpacing = Math.max(0, rowSpacing);
  }

  public ReactionFlowLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    rowSpacing = 0;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int contentWidth =
        widthMode == MeasureSpec.UNSPECIFIED
            ? Integer.MAX_VALUE
            : Math.max(0, widthSize - getPaddingLeft() - getPaddingRight());
    int lineWidth = 0;
    int lineHeight = 0;
    int maxLineWidth = 0;
    int totalHeight = getPaddingTop();
    boolean hasChildInLine = false;

    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child.getVisibility() == GONE) continue;
      MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
      int childWidthSpec =
          widthMode == MeasureSpec.UNSPECIFIED
              ? MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
              : MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.AT_MOST);
      child.measure(childWidthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
      int childWidth = child.getMeasuredWidth() + params.leftMargin + params.rightMargin;
      int childHeight = child.getMeasuredHeight() + params.topMargin + params.bottomMargin;
      if (hasChildInLine && lineWidth + childWidth > contentWidth) {
        maxLineWidth = Math.max(maxLineWidth, lineWidth);
        totalHeight += lineHeight + rowSpacing;
        lineWidth = 0;
        lineHeight = 0;
        hasChildInLine = false;
      }
      lineWidth += childWidth;
      lineHeight = Math.max(lineHeight, childHeight);
      hasChildInLine = true;
    }
    if (hasChildInLine) {
      maxLineWidth = Math.max(maxLineWidth, lineWidth);
      totalHeight += lineHeight;
    }
    totalHeight += getPaddingBottom();
    int desiredWidth = maxLineWidth + getPaddingLeft() + getPaddingRight();
    int measuredWidth =
        widthMode == MeasureSpec.EXACTLY
            ? widthSize
            : widthMode == MeasureSpec.AT_MOST ? Math.min(desiredWidth, widthSize) : desiredWidth;
    setMeasuredDimension(measuredWidth, resolveSize(totalHeight, heightMeasureSpec));
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    int contentRight = right - left - getPaddingRight();
    int childLeft = getPaddingLeft();
    int childTop = getPaddingTop();
    int lineHeight = 0;

    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child.getVisibility() == GONE) continue;
      MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
      int childWidth = child.getMeasuredWidth();
      int childHeight = child.getMeasuredHeight();
      int nextRight = childLeft + params.leftMargin + childWidth + params.rightMargin;
      if (childLeft > getPaddingLeft() && nextRight > contentRight) {
        childLeft = getPaddingLeft();
        childTop += lineHeight + rowSpacing;
        lineHeight = 0;
      }
      int childLeftWithMargin = childLeft + params.leftMargin;
      int childTopWithMargin = childTop + params.topMargin;
      child.layout(
          childLeftWithMargin,
          childTopWithMargin,
          childLeftWithMargin + childWidth,
          childTopWithMargin + childHeight);
      childLeft += params.leftMargin + childWidth + params.rightMargin;
      lineHeight = Math.max(lineHeight, params.topMargin + childHeight + params.bottomMargin);
    }
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
  }

  @Override
  protected LayoutParams generateLayoutParams(LayoutParams params) {
    return new MarginLayoutParams(params);
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new MarginLayoutParams(getContext(), attrs);
  }

  @Override
  protected boolean checkLayoutParams(LayoutParams params) {
    return params instanceof MarginLayoutParams;
  }
}
