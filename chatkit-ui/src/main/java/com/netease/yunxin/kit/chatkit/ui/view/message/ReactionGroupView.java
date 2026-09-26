// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.model.MessageReactionState;
import java.util.List;

/** Owns the layout of one message's Reaction area. */
public class ReactionGroupView extends ViewGroup {
  private ReactionStyle style;
  private OnReactionClickListener clickListener;

  public ReactionGroupView(@NonNull Context context) {
    this(context, null);
  }

  public ReactionGroupView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setVisibility(GONE);
  }

  public void bind(
      @Nullable List<MessageReactionState.ReactionSummary> summaries,
      @NonNull ReactionDrawableProvider drawableProvider,
      @NonNull ReactionStyle style,
      @Nullable OnReactionClickListener clickListener) {
    this.style = style;
    this.clickListener = clickListener;
    removeReactionItems();
    setPadding(
        style.groupPaddingLeft,
        style.groupPaddingTop,
        style.groupPaddingRight,
        style.groupPaddingBottom);
    if (summaries == null || summaries.isEmpty()) {
      setVisibility(GONE);
      return;
    }

    if (clickListener != null) {
      MarginLayoutParams addParams =
          new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
      addParams.rightMargin = style.addButtonMarginEnd;
      addView(createAddReactionView(clickListener), addParams);
    }
    int renderedCount = 0;
    for (MessageReactionState.ReactionSummary summary : summaries) {
      if (summary == null || renderedCount >= 100) {
        break;
      }
      Drawable drawable = drawableProvider.getDrawable(summary.getIndex());
      if (drawable == null) {
        continue;
      }
      ReactionItemView itemView = new ReactionItemView(getContext());
      itemView.bind(
          summary,
          drawable,
          drawableProvider.getVisualScale(summary.getIndex()),
          style,
          clickListener);
      MarginLayoutParams itemParams =
          new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
      itemParams.rightMargin = style.itemMarginEnd;
      addView(itemView, itemParams);
      renderedCount++;
    }
    setVisibility(renderedCount == 0 ? GONE : VISIBLE);
  }

  private TextView createAddReactionView(@NonNull OnReactionClickListener listener) {
    TextView addReaction = new TextView(getContext());
    addReaction.setGravity(Gravity.CENTER);
    addReaction.setPadding(style.addPadding, style.addPadding, style.addPadding, style.addPadding);
    addReaction.setBackground(style.createItemBackground(getContext(), false));
    Drawable addDrawable =
        ContextCompat.getDrawable(getContext(), R.drawable.ic_chat_emoji_pop_add);
    if (addDrawable != null) {
      addDrawable.setBounds(0, 0, style.addIconSize, style.addIconSize);
      addReaction.setCompoundDrawables(addDrawable, null, null, null);
    }
    addReaction.setOnClickListener(listener::onAddClick);
    return addReaction;
  }

  private void removeReactionItems() {
    while (getChildCount() > 0) {
      removeViewAt(getChildCount() - 1);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (style == null || getVisibility() == GONE) {
      setMeasuredDimension(0, 0);
      return;
    }
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int contentWidth =
        widthMode == MeasureSpec.UNSPECIFIED
            ? Integer.MAX_VALUE
            : Math.max(0, widthSize - getPaddingLeft() - getPaddingRight());
    int lineWidth = 0;
    int lineHeight = 0;
    int maxLineWidth = 0;
    int flowHeight = 0;
    boolean hasChildInLine = false;
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
      child.measure(
          MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.AT_MOST),
          MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
      int childWidth = child.getMeasuredWidth() + params.leftMargin + params.rightMargin;
      int childHeight = child.getMeasuredHeight() + params.topMargin + params.bottomMargin;
      if (hasChildInLine && lineWidth + childWidth > contentWidth) {
        maxLineWidth = Math.max(maxLineWidth, lineWidth);
        flowHeight += lineHeight + style.rowSpacing;
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
      flowHeight += lineHeight;
    }
    int desiredWidth = maxLineWidth + getPaddingLeft() + getPaddingRight();
    int measuredWidth =
        widthMode == MeasureSpec.EXACTLY
            ? widthSize
            : widthMode == MeasureSpec.AT_MOST ? Math.min(desiredWidth, widthSize) : desiredWidth;
    int measuredHeight = getPaddingTop() + flowHeight + getPaddingBottom();
    setMeasuredDimension(measuredWidth, resolveSize(measuredHeight, heightMeasureSpec));
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    if (style == null || getVisibility() == GONE) {
      return;
    }
    int contentLeft = getPaddingLeft();
    int contentRight = right - left - getPaddingRight();
    int flowTop = getPaddingTop();

    int childLeft = contentLeft;
    int childTop = flowTop;
    int lineHeight = 0;
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
      int nextRight = childLeft + params.leftMargin + child.getMeasuredWidth() + params.rightMargin;
      if (childLeft > contentLeft && nextRight > contentRight) {
        childLeft = contentLeft;
        childTop += lineHeight + style.rowSpacing;
        lineHeight = 0;
      }
      int childLeftWithMargin = childLeft + params.leftMargin;
      int childTopWithMargin = childTop + params.topMargin;
      child.layout(
          childLeftWithMargin,
          childTopWithMargin,
          childLeftWithMargin + child.getMeasuredWidth(),
          childTopWithMargin + child.getMeasuredHeight());
      childLeft += params.leftMargin + child.getMeasuredWidth() + params.rightMargin;
      lineHeight =
          Math.max(lineHeight, params.topMargin + child.getMeasuredHeight() + params.bottomMargin);
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

  public interface ReactionDrawableProvider {
    @Nullable
    Drawable getDrawable(long index);

    float getVisualScale(long index);
  }

  public interface OnReactionClickListener {
    void onReactionClick(@NonNull View view, long index, boolean hasSelf);

    void onAddClick(@NonNull View view);
  }

  public static final class ReactionStyle {
    public final int groupPaddingLeft;
    public final int groupPaddingTop;
    public final int groupPaddingRight;
    public final int groupPaddingBottom;
    public final int rowSpacing;
    public final int itemMarginEnd;
    public final int addButtonMarginEnd;
    public final int itemHorizontalPadding;
    public final int itemVerticalPadding;
    public final int emojiSize;
    public final int countMarginStart;
    public final float countTextSizeSp;
    public final int addPadding;
    public final int addIconSize;
    public final float cornerRadius;
    @ColorInt public final int backgroundColor;
    @ColorInt public final int highlightTextColor;

    public ReactionStyle(
        int groupPaddingLeft,
        int groupPaddingTop,
        int groupPaddingRight,
        int groupPaddingBottom,
        int rowSpacing,
        int itemMarginEnd,
        int addButtonMarginEnd,
        int itemHorizontalPadding,
        int itemVerticalPadding,
        int emojiSize,
        int countMarginStart,
        float countTextSizeSp,
        int addPadding,
        int addIconSize,
        float cornerRadius,
        @ColorInt int backgroundColor,
        @ColorInt int highlightTextColor) {
      this.groupPaddingLeft = groupPaddingLeft;
      this.groupPaddingTop = groupPaddingTop;
      this.groupPaddingRight = groupPaddingRight;
      this.groupPaddingBottom = groupPaddingBottom;
      this.rowSpacing = rowSpacing;
      this.itemMarginEnd = itemMarginEnd;
      this.addButtonMarginEnd = addButtonMarginEnd;
      this.itemHorizontalPadding = itemHorizontalPadding;
      this.itemVerticalPadding = itemVerticalPadding;
      this.emojiSize = emojiSize;
      this.countMarginStart = countMarginStart;
      this.countTextSizeSp = countTextSizeSp;
      this.addPadding = addPadding;
      this.addIconSize = addIconSize;
      this.cornerRadius = cornerRadius;
      this.backgroundColor = backgroundColor;
      this.highlightTextColor = highlightTextColor;
    }

    Drawable createItemBackground(Context context, boolean self) {
      GradientDrawable background = new GradientDrawable();
      background.setColor(backgroundColor);
      background.setCornerRadius(cornerRadius);
      return background;
    }
  }
}
