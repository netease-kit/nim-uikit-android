// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.widgets;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/** A single-line TextView that keeps the searched range visible when the text is truncated. */
public class HitHighlightTextView extends AppCompatTextView {
  private static final String ELLIPSIS = "\u2026";

  private String originalText = "";
  private int hitStart = -1;
  private int hitEnd = -1;
  private int highlightColor;
  private boolean hitTextEnabled;
  private boolean rendering;
  private int renderedWidth = -1;

  public HitHighlightTextView(@NonNull Context context) {
    this(context, null);
  }

  public HitHighlightTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public HitHighlightTextView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void setHitText(
      @Nullable String text, int hitStart, int hitEnd, @ColorInt int highlightColor) {
    originalText = text == null ? "" : text;
    this.hitStart = hitStart;
    this.hitEnd = hitEnd;
    this.highlightColor = highlightColor;
    hitTextEnabled = isValidHit(originalText, hitStart, hitEnd);
    renderedWidth = -1;
    renderText();
  }

  public void clearHitText(@Nullable String text) {
    originalText = text == null ? "" : text;
    hitStart = -1;
    hitEnd = -1;
    hitTextEnabled = false;
    renderedWidth = -1;
    setEllipsize(TextUtils.TruncateAt.END);
    setText(originalText);
  }

  @Override
  protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
    super.onSizeChanged(width, height, oldWidth, oldHeight);
    if (width != oldWidth) {
      renderedWidth = -1;
      renderText();
    }
  }

  private void renderText() {
    if (rendering) {
      return;
    }
    if (getWidth() <= 0) {
      setEllipsize(TextUtils.TruncateAt.END);
      setText(originalText);
      return;
    }
    int availableWidth = getWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
    if (availableWidth <= 0) {
      return;
    }
    if (!hitTextEnabled) {
      clearHitText(originalText);
      return;
    }
    if (renderedWidth == availableWidth) {
      return;
    }

    renderedWidth = availableWidth;
    TextPaint paint = getPaint();
    if (paint == null || paint.measureText(originalText) <= availableWidth) {
      setEllipsize(null);
      applyText(originalText, hitStart, hitEnd);
      return;
    }
    setEllipsize(null);
    applyText(buildTruncatedText(paint, availableWidth));
  }

  private SpannableString buildTruncatedText(TextPaint paint, int availableWidth) {
    String hitText = originalText.substring(hitStart, hitEnd);
    float hitWidth = paint.measureText(hitText);
    if (hitWidth >= availableWidth) {
      String visibleHit =
          TextUtils.ellipsize(hitText, paint, availableWidth, TextUtils.TruncateAt.END).toString();
      return createHighlightedText(visibleHit, 0, Math.min(hitText.length(), visibleHit.length()));
    }

    float ellipsisWidth = paint.measureText(ELLIPSIS);
    int suffixCount = countFromEnd(paint, originalText, availableWidth - ellipsisWidth);
    int suffixStart = originalText.length() - suffixCount;
    if (hitStart >= suffixStart) {
      String suffix = originalText.substring(suffixStart);
      String text = ELLIPSIS + suffix;
      int displayedHitStart = ELLIPSIS.length() + hitStart - suffixStart;
      int displayedHitEnd = ELLIPSIS.length() + hitEnd - suffixStart;
      return createHighlightedText(text, displayedHitStart, displayedHitEnd);
    }

    // When the hit is in the latter half, keep a continuous suffix window instead of
    // falling back to a prefix window that hides the context after the hit.
    if (hitStart >= originalText.length() / 2) {
      return buildHitAndSuffixText(paint, availableWidth, ellipsisWidth, hitText, hitWidth);
    }

    int prefixCount = countFromStart(paint, originalText, availableWidth - ellipsisWidth);
    if (hitEnd <= prefixCount) {
      String prefix = originalText.substring(0, prefixCount);
      String text = prefix + ELLIPSIS;
      return createHighlightedText(text, hitStart, hitEnd);
    }

    return buildHitAndSuffixText(paint, availableWidth, ellipsisWidth, hitText, hitWidth);
  }

  private SpannableString buildHitAndSuffixText(
      TextPaint paint, int availableWidth, float ellipsisWidth, String hitText, float hitWidth) {
    if (availableWidth - ellipsisWidth - hitWidth <= 0) {
      String visibleHit =
          TextUtils.ellipsize(
                  hitText,
                  paint,
                  Math.max(0, availableWidth - ellipsisWidth),
                  TextUtils.TruncateAt.END)
              .toString();
      String text = ELLIPSIS + visibleHit;
      return createHighlightedText(text, ELLIPSIS.length(), text.length());
    }
    float suffixEllipsisWidth = hitEnd < originalText.length() ? ellipsisWidth : 0f;
    float suffixWidth = availableWidth - ellipsisWidth - hitWidth - suffixEllipsisWidth;
    int suffixCount = countFromStart(paint, originalText.substring(hitEnd), suffixWidth);
    String suffix = originalText.substring(hitEnd, hitEnd + suffixCount);
    boolean suffixTruncated = hitEnd + suffixCount < originalText.length();
    StringBuilder result = new StringBuilder(ELLIPSIS);
    int displayedHitStart = result.length();
    result.append(hitText);
    int displayedHitEnd = result.length();
    result.append(suffix);
    if (suffixTruncated) {
      result.append(ELLIPSIS);
    }
    return createHighlightedText(result.toString(), displayedHitStart, displayedHitEnd);
  }

  private int countFromEnd(TextPaint paint, String text, float maxWidth) {
    if (TextUtils.isEmpty(text) || maxWidth <= 0) {
      return 0;
    }
    return paint.breakText(text, false, maxWidth, null);
  }

  private int countFromStart(TextPaint paint, String text, float maxWidth) {
    if (TextUtils.isEmpty(text) || maxWidth <= 0) {
      return 0;
    }
    return paint.breakText(text, true, maxWidth, null);
  }

  private void applyText(String text, int start, int end) {
    applyText(createHighlightedText(text, start, end));
  }

  private void applyText(SpannableString text) {
    rendering = true;
    setText(text, BufferType.SPANNABLE);
    rendering = false;
  }

  private SpannableString createHighlightedText(String text, int start, int end) {
    SpannableString result = new SpannableString(text);
    if (start >= 0 && end > start && end <= result.length()) {
      result.setSpan(
          new ForegroundColorSpan(highlightColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    return result;
  }

  private boolean isValidHit(String text, int start, int end) {
    return start >= 0 && end > start && end <= text.length();
  }
}
