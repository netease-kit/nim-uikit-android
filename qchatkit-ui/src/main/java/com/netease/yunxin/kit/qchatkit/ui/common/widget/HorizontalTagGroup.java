// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.common.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleInfo;
import java.util.ArrayList;
import java.util.List;

public class HorizontalTagGroup extends LinearLayout {
  private static final int COLOR_ITEM_TAG_BG = Color.parseColor("#fff2f4f5");
  private static final int COLOR_ITEM_TAG_TEXT = Color.parseColor("#ff656a72");
  private static final int RADIUS_DP_ITEM_TAG = 4;
  private static final int TEXT_FONT_SIZE_ITEM_TAG = 12;
  private static final int HORIZONTAL_ITEM_TAG_PADDING = 8;
  private static final int VERTICAL_ITEM_TAG_PADDING = 4;
  private static final int HORIZONTAL_ITEM_MARGIN = 6;
  private static final int VERTICAL_ITEM_MARGIN = 6;
  private static final int MAX_ROW_COUNT = 2;

  private final List<QChatServerRoleInfo> tagList = new ArrayList<>();

  public HorizontalTagGroup(Context context) {
    super(context);
    init();
  }

  public HorizontalTagGroup(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public HorizontalTagGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setOrientation(LinearLayout.HORIZONTAL);
  }

  public void setData(List<QChatServerRoleInfo> tagList) {
    this.tagList.clear();
    if (tagList != null) {
      this.tagList.addAll(tagList);
    }
    removeAllViews();
    for (QChatServerRoleInfo tag : this.tagList) {
      TextView textView = prepareTextView();
      textView.setText(tag.getName());
      textView.setBackground(prepareTagBg());
      addView(textView);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    measureChildren(widthMeasureSpec, heightMeasureSpec);

    int horizontalMargin = SizeUtils.dp2px(HORIZONTAL_ITEM_MARGIN);
    int verticalMargin = SizeUtils.dp2px(VERTICAL_ITEM_MARGIN);

    int width;
    int height = 0;

    int row = 0;
    int rowWidth = 0;
    int rowMaxHeight = 0;

    final int count = getChildCount();
    for (int i = 0; i < count; i++) {
      View child = getChildAt(i);
      int childWidth = child.getMeasuredWidth();
      int childHeight = child.getMeasuredHeight();

      if (child.getVisibility() != GONE) {
        if (row + 1 >= MAX_ROW_COUNT && rowWidth + childWidth > widthSize) {
          break;
        }
        rowWidth += childWidth;
        if (rowWidth > widthSize) {
          rowWidth = childWidth;
          height += rowMaxHeight + verticalMargin;
          rowMaxHeight = childHeight;
          row++;
        } else {
          rowMaxHeight = Math.max(rowMaxHeight, childHeight);
        }
        rowWidth += horizontalMargin;
      }
    }

    height += getPaddingTop() + getPaddingBottom() + rowMaxHeight;
    if (row == 0) {
      width = rowWidth;
      width += getPaddingLeft() + getPaddingRight();
    } else {
      width = widthSize;
    }

    setMeasuredDimension(
        widthMode == MeasureSpec.EXACTLY ? widthSize : width,
        heightMode == MeasureSpec.EXACTLY ? heightSize : height);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    final int parentLeft = getPaddingLeft();
    final int parentRight = r - l - getPaddingRight();
    final int parentTop = getPaddingTop();

    int horizontalMargin = SizeUtils.dp2px(HORIZONTAL_ITEM_MARGIN);
    int verticalMargin = SizeUtils.dp2px(VERTICAL_ITEM_MARGIN);

    int childLeft = parentLeft;
    int childTop = parentTop;

    int row = 0;
    int rowMaxHeight = 0;

    int count = getChildCount();
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      final int width = child.getMeasuredWidth();
      final int height = child.getMeasuredHeight();

      if (child.getVisibility() != GONE) {
        if (row + 1 >= MAX_ROW_COUNT && childLeft + width + horizontalMargin > parentRight) {
          break;
        }
        if (childLeft + width > parentRight) { // Next line
          childLeft = parentLeft;
          childTop += rowMaxHeight + verticalMargin;
          rowMaxHeight = height;
          row++;
        } else {
          rowMaxHeight = Math.max(rowMaxHeight, height);
        }

        child.layout(childLeft, childTop, childLeft + width, childTop + height);
        childLeft += width + horizontalMargin;
      }
    }
  }

  private TextView prepareTextView() {
    TextView textView = new TextView(getContext());
    textView.setTextColor(COLOR_ITEM_TAG_TEXT);
    textView.setTextSize(TEXT_FONT_SIZE_ITEM_TAG);
    int horizontalPadding = SizeUtils.dp2px(HORIZONTAL_ITEM_TAG_PADDING);
    int verticalPadding = SizeUtils.dp2px(VERTICAL_ITEM_TAG_PADDING);
    textView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
    textView.setMaxLines(1);
    textView.setEllipsize(TextUtils.TruncateAt.END);
    return textView;
  }

  private Drawable prepareTagBg() {
    GradientDrawable drawable = new GradientDrawable();
    drawable.setColor(COLOR_ITEM_TAG_BG);
    drawable.setCornerRadius(SizeUtils.dp2px(RADIUS_DP_ITEM_TAG));
    return drawable;
  }
}
