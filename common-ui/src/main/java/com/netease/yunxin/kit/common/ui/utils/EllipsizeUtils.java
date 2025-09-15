// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.utils;

import android.graphics.Point;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.core.widget.TextViewCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** EllipsizeUtils */
public class EllipsizeUtils {

  public static final int HIGHLIGHT_FIRST = 0;
  public static final int HIGHLIGHT_LAST = 1;
  public static final int HIGHLIGHT_ALL = 2;

  private static final String ELLIPSIS_NORMAL = "\u2026"; // HORIZONTAL ELLIPSIS (…)

  /**
   * @param content the content to highlight
   * @param keyword the keyword to highlight
   * @param color the color to highlight
   * @param fromIndex the index to begin highlighting from.
   * @param highlightType {@link #HIGHLIGHT_FIRST}, {@link #HIGHLIGHT_LAST}, {@link #HIGHLIGHT_ALL}
   * @param ignoreCase {@code true} if ignore case when highlight
   * @return the content highlighted, or origin content if {@code fromIndex >= content.length() }
   */
  public static SpannableString highlight(
      String content,
      String keyword,
      @ColorInt int color,
      int fromIndex,
      int highlightType,
      boolean ignoreCase) {
    SpannableString ss = new SpannableString(content);
    if (fromIndex >= content.length() || TextUtils.isEmpty(content) || TextUtils.isEmpty(keyword)) {
      return ss;
    }

    fromIndex = Math.max(0, fromIndex);
    String compareContent = ignoreCase ? content.toLowerCase(Locale.ENGLISH) : content;
    String compareKeyword = ignoreCase ? keyword.toLowerCase(Locale.ENGLISH) : keyword;

    if (highlightType == HIGHLIGHT_LAST || highlightType == HIGHLIGHT_FIRST) {
      int index = -1;
      if (highlightType == HIGHLIGHT_LAST) {
        index = compareContent.lastIndexOf(compareKeyword);
        if (index < fromIndex) {
          index = -1;
        }
      } else {
        index = compareContent.indexOf(compareKeyword, fromIndex);
      }
      if (index > -1) {
        ss.setSpan(
            new ForegroundColorSpan(color),
            index,
            index + keyword.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
    } else { // HIGHLIGHT_ALL
      int index = compareContent.indexOf(compareKeyword, fromIndex);
      while (index >= 0) {
        ss.setSpan(
            new ForegroundColorSpan(color),
            index,
            index + keyword.length(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        index = compareContent.indexOf(compareKeyword, index + keyword.length());
      }
    }

    return ss;
  }

  /**
   * Ellipsize the TextView by keyword
   *
   * @param textView the TextView to ellipsize
   * @param content the content to ellipsize
   * @param keyword the keyword to ellipsize
   * @param ignoreCase {@code true} if ignore case when ellipsize
   */
  public static void ellipsizeByKeyword(
      final TextView textView,
      final String content,
      final String keyword,
      final boolean ignoreCase) {
    if (TextUtils.isEmpty(content) || TextUtils.isEmpty(keyword)) {
      textView.setText(null);
      return;
    }

    if (textView.getWidth() <= 0) {
      // Monitor layout completed
      new EllipseListener(textView, content, keyword, ignoreCase);
    } else {
      ellipsizeByKeywordInner(textView, content, keyword, ignoreCase);
    }
  }

  /**
   * Ellipsize the TextView by keyword and highlight the keyword
   *
   * @param textView the TextView to ellipsize
   * @param content the content to ellipsize
   * @param keyword the keyword to ellipsize
   * @param highlightColor the color to highlight
   * @param highlightAll {@code true} if highlight all matched keyword. {@code true} if only
   *     highlight the first matched keyword from ellipised content
   * @param ignoreCase {@code true} if ignore case when ellipsize
   */
  public static void ellipsizeAndHighlight(
      final TextView textView,
      final String content,
      final String keyword,
      @ColorInt final int highlightColor,
      final boolean highlightAll,
      final boolean ignoreCase) {
    if (TextUtils.isEmpty(content) || TextUtils.isEmpty(keyword)) {
      textView.setText(null);
      return;
    }

    if (textView.getWidth() <= 0) {
      // Monitor layout completed
      new EllipseListener(textView, content, keyword, highlightColor, highlightAll, ignoreCase);
    } else {
      ellipsizeByKeywordInner(textView, content, keyword, ignoreCase);

      int type = HIGHLIGHT_ALL;
      if (!highlightAll) {
        if (textView.getEllipsize() == TextUtils.TruncateAt.START) {
          type = HIGHLIGHT_LAST;
        } else {
          type = HIGHLIGHT_FIRST;
        }
      }
      SpannableString s =
          highlight(textView.getText().toString(), keyword, highlightColor, 0, type, ignoreCase);
      textView.setText(s);
    }
  }

  /**
   * Ellipsize the TextView by keyword
   *
   * @param textView the TextView to ellipsize
   * @param content the content to ellipsize
   * @param keyword the keyword to ellipsize
   */
  private static void ellipsizeByKeywordInner(
      final TextView textView, String content, String keyword, boolean ignoreCase) {
    TextPaint paint = textView.getPaint();
    if (paint == null) {
      textView.setText(null);
      return;
    }

    String compareContent = ignoreCase ? content.toLowerCase(Locale.ENGLISH) : content;
    String compareKeyword = ignoreCase ? keyword.toLowerCase(Locale.ENGLISH) : keyword;

    final int keywordStart = compareContent.indexOf(compareKeyword);
    if (keywordStart < 0) { // 找不到关键字
      textView.setText(compareContent);
      return;
    }

    int maxLine = TextViewCompat.getMaxLines(textView);
    if (maxLine <= 0) { // 没有行数限制
      textView.setText(content);
      return;
    }
    // 每行文字的最大显示宽度
    int availableWidth =
        textView.getWidth() - textView.getPaddingLeft() - textView.getPaddingRight();
    // 区分单行和多行做不同的处理
    if (maxLine < 2) { // 单行
      int availableCount = 0; // 一行可显示的字符数
      //　如果关键字可在截断尾部后的内容中找到，则直接截断尾部
      String newCharSeq =
          TextUtils.ellipsize(compareContent, paint, availableWidth, TextUtils.TruncateAt.END)
              .toString();
      availableCount = newCharSeq.length();
      if (newCharSeq.contains(compareKeyword)) {
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setText(content);
        return;
      }

      //　如果关键字可在截断首部后的内容中找到，则直接截断首部
      newCharSeq =
          TextUtils.ellipsize(compareContent, paint, availableWidth, TextUtils.TruncateAt.START)
              .toString();
      availableCount = Math.max(newCharSeq.length(), availableCount);
      if (newCharSeq.contains(compareKeyword)) {
        textView.setEllipsize(TextUtils.TruncateAt.START);
        textView.setText(content);
        return;
      }

      // 关键字太长了，一行不够显示
      if (availableCount <= keyword.length()) {
        // display: ELLIPSIS_NORMAL + keyword + ...
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setText(ELLIPSIS_NORMAL + keyword);
        return;
      }

      // 关键字在内容中间位置，则首尾都要加上省略号
      // display: ELLIPSIS_NORMAL + xxx + keyword + xxx +...
      textView.setEllipsize(TextUtils.TruncateAt.END);
      int start = keywordStart - (availableCount - keyword.length()) / 2;

      String text = ELLIPSIS_NORMAL + content.substring(start >= 0 ? start : 0);
      newCharSeq =
          TextUtils.ellipsize(text, paint, availableWidth, TextUtils.TruncateAt.END).toString();
      if (newCharSeq.contains(compareKeyword)) {
        textView.setText(text);
      } else {
        textView.setText(ELLIPSIS_NORMAL + content.substring(keywordStart));
      }
    } else { // multi line
      List<Point> linesStart =
          getLineStartAndEnd(textView.getPaint(), compareContent, availableWidth);
      int keywordLineStart = getKeywordLine(keywordStart, linesStart); // 在原始内容中，关键字第一个字符的行位置
      int keywordLineEnd =
          getKeywordLine(
              keywordStart + compareKeyword.length() + 1, linesStart); // 在原始内容中，关键字最后一个字符的行位置
      if (keywordLineEnd - keywordLineStart < maxLine) {
        int endLine =
            Math.min(
                keywordLineStart + maxLine / 2,
                linesStart.size() - 1); // linesStart.size() - 1 = lastLines
        int startLine =
            Math.max(endLine - (maxLine - 1) + maxLine % 2, 0); // if maxline is odd, starline+1
        textView.setEllipsize(TextUtils.TruncateAt.END);
        if (startLine == 0) {
          // display: xxx + keyword + xxx +...
          textView.setText(content);
        } else {
          // display: ELLIPSIS_NORMAL + xxx + keyword + xxx +...
          int start = linesStart.get(startLine).x;
          textView.setText(ELLIPSIS_NORMAL + content.substring(start));
        }
      } else { // // 关键字太长了
        // display: ELLIPSIS_NORMAL + keyword + xx ...
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setText(ELLIPSIS_NORMAL + content.substring(keywordStart));
      }
    }
  }

  /** 获取关键字所在的行 */
  private static int getKeywordLine(int keywordStart, List<Point> linesStart) {
    for (int i = 0; i < linesStart.size(); i++) {
      if (keywordStart < linesStart.get(i).y) {
        return i;
      }
    }
    return 0;
  }

  /**
   * 计算每一行的开始字符位置和结束字符位置
   *
   * @return List.size()为总行数．point.x 为当前行的开始字符位置， point.y 为当前行的结束字符位置
   */
  private static List<Point> getLineStartAndEnd(TextPaint tp, CharSequence cs, int lineWidth) {
    // StaticLayout是android中处理文字换行的一个工具类，StaticLayout已经实现了文本绘制换行处理
    StaticLayout layout =
        new StaticLayout(cs, tp, lineWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
    int count = layout.getLineCount();
    List<Point> list = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      list.add(new Point(layout.getLineStart(i), layout.getLineEnd(i)));
    }
    return list;
  }

  /** 监听TextView的layout完成，然后做截断处理 */
  private static class EllipseListener implements ViewTreeObserver.OnPreDrawListener {
    final TextView textView;
    final String content;
    final String keyword;
    final int highlightColor;
    final boolean highlightAll;
    final boolean ignoreCase;
    boolean needHighlight;

    public EllipseListener(TextView tv, String content, String keyword, boolean ignoreCase) {
      this(tv, content, keyword, 0, false, ignoreCase);
      this.needHighlight = false;
    }

    public EllipseListener(
        TextView tv,
        String content,
        String keyword,
        int highlightColor,
        boolean highlightAll,
        boolean ignoreCase) {
      this.textView = tv;
      this.content = content;
      this.keyword = keyword;
      this.highlightColor = highlightColor;
      this.highlightAll = highlightAll;
      this.ignoreCase = ignoreCase;
      this.needHighlight = true;

      tv.getViewTreeObserver().addOnPreDrawListener(this);
    }

    @Override
    public boolean onPreDraw() {
      textView.getViewTreeObserver().removeOnPreDrawListener(this);

      ellipsizeByKeywordInner(textView, content, keyword, ignoreCase);
      if (!needHighlight) {
        return true;
      }

      int type = HIGHLIGHT_ALL;
      if (!highlightAll) {
        if (textView.getEllipsize() == TextUtils.TruncateAt.START) {
          type = HIGHLIGHT_LAST;
        } else {
          type = HIGHLIGHT_FIRST;
        }
      }
      SpannableString s =
          highlight(textView.getText().toString(), keyword, highlightColor, 0, type, ignoreCase);
      textView.setText(s);
      return true;
    }
  }

  public static void ellipsize(TextView textView, String content) {
    TextUtils.TruncateAt ellipsize = textView.getEllipsize();
    if (ellipsize != TextUtils.TruncateAt.START
        && ellipsize != TextUtils.TruncateAt.MIDDLE) { // 只处理start和middle的截断
      textView.setText(content);
      return;
    }

    int maxLine = TextViewCompat.getMaxLines(textView);
    int availableWidth =
        textView.getWidth() - textView.getPaddingLeft() - textView.getPaddingRight();
    if (maxLine < 2) { // 单行，或没做行数限制
      textView.setText(content);
    } else {
      List<Point> linesStart = getLineStartAndEnd(textView.getPaint(), content, availableWidth);
      if (linesStart.size() <= maxLine) { // 行数没有超过限制，不做处理
        textView.setText(content);
        return;
      }

      if (ellipsize == TextUtils.TruncateAt.START) {
        int start = linesStart.get(linesStart.size() - maxLine).x;
        start = Math.max(start + ELLIPSIS_NORMAL.length(), 0);
        String substring = content.substring(start);
        // 裁剪文字，直到在行数范围内可显示
        while (getLayout(textView.getPaint(), ELLIPSIS_NORMAL + substring, availableWidth)
                .getLineCount()
            > maxLine) {
          int firstSpace = substring.indexOf(' '); // 空白字符
          if (firstSpace == -1) {
            substring = substring.substring(1);
          } else {
            substring = substring.substring(firstSpace + 1);
          }
        }
        textView.setText(ELLIPSIS_NORMAL + substring);
      } else { // middle
        int middleLineStart = (maxLine - 1) / 2;
        Point point = linesStart.get(middleLineStart);
        int startEllipsize = point.y - ELLIPSIS_NORMAL.length(); // 在中间行的末尾追加省略号
        final String substringStart = content.substring(0, startEllipsize); // 省略号前面的文字

        int middleLineEnd = linesStart.size() - (maxLine - (maxLine - 1) / 2 - 1);
        Point pointEnd = linesStart.get(middleLineEnd);
        String substringEnd = content.substring(pointEnd.x);
        // 裁剪省略号后面的文字，直到整体在行数范围内可显示
        while (getLayout(
                    textView.getPaint(),
                    substringStart + ELLIPSIS_NORMAL + substringEnd,
                    availableWidth)
                .getLineCount()
            > maxLine) {
          int firstSpace = substringEnd.indexOf(' '); // 空白字符
          if (firstSpace == -1) {
            substringEnd = substringEnd.substring(1);
          } else {
            substringEnd = substringEnd.substring(firstSpace + 1);
          }
        }
        textView.setText(substringStart + ELLIPSIS_NORMAL + substringEnd);
      }
    }
  }

  private static Layout getLayout(TextPaint tp, CharSequence cs, int lineWidth) {
    StaticLayout layout =
        new StaticLayout(cs, tp, lineWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
    return layout;
  }
}
