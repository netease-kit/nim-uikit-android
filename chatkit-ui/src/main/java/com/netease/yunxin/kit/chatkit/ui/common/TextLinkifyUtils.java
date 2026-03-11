// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.PHONE_NUMBER_PATTERN;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.TEL_SCHEME;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

public class TextLinkifyUtils {

  public static void addLinks(
      TextView textView,
      IMessageItemClickListener itemClickListener,
      int position,
      ChatMessageBean currentMessage) {
    SpannableString spannable = new SpannableString(textView.getText());
    Linkify.addLinks(spannable, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
    Linkify.addLinks(spannable, PHONE_NUMBER_PATTERN, TEL_SCHEME);

    // 2. 移除默认的 ClickableSpan，替换为自定义的
    replaceClickableSpans(spannable, itemClickListener, position, currentMessage);
    textView.setText(spannable);
    textView.setMovementMethod(
        new LinkMovementMethod() {
          @Override
          public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
              int x = (int) event.getX();
              int y = (int) event.getY();

              x -= widget.getTotalPaddingLeft();
              y -= widget.getTotalPaddingTop();

              x += widget.getScrollX();
              y += widget.getScrollY();

              // 获取点击位置的布局
              android.text.Layout layout = widget.getLayout();
              int line = layout.getLineForVertical(y);
              int off = layout.getOffsetForHorizontal(line, x);

              // 获取点击位置的 ClickableSpan
              ClickableSpan[] spans = buffer.getSpans(off, off, ClickableSpan.class);

              if (spans.length != 0) {
                // 点击了 ClickableSpan，处理其事件并消费
                if (action == MotionEvent.ACTION_UP) {
                  spans[0].onClick(widget);
                }
                return true; // 消费事件，不传递给 TextView
              } else {
                // 未点击 Span，交由 TextView 的 OnClickListener 处理
                if (action == MotionEvent.ACTION_UP) {
                  widget.performClick();
                }
                return false; // 不消费，让事件继续传递给父级
              }
            }
            return super.onTouchEvent(widget, buffer, event);
          }
        });
  }

  public static void addLinks(TextView textView) {
    SpannableString spannable = new SpannableString(textView.getText());
    Linkify.addLinks(spannable, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
    Linkify.addLinks(textView, PHONE_NUMBER_PATTERN, TEL_SCHEME);

    // 3. 设置自定义的 MovementMethod（可选，用于处理点击）
    textView.setText(spannable);
    textView.setMovementMethod(LinkMovementMethod.getInstance());
  }

  // 替换 Spannable 中的默认 URLSpan 为自定义 ClickableSpan，拦截点击事件
  private static void replaceClickableSpans(
      Spannable spannable,
      IMessageItemClickListener itemClickListener,
      int position,
      ChatMessageBean currentMessage) {
    // 获取所有默认的 URLSpan（Linkify 生成的链接）
    URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
    for (URLSpan span : spans) {
      int start = spannable.getSpanStart(span);
      int end = spannable.getSpanEnd(span);
      int flags = spannable.getSpanFlags(span);
      String url = span.getURL(); // 获取链接内容（如 "tel:13800138000" 或 "https://..."）
      // 创建自定义 ClickableSpan 拦截点击
      ClickableSpan customSpan =
          new ClickableSpan() {
            @Override
            public void onClick(View widget) {
              // 自定义点击处理逻辑
              if (itemClickListener != null) {
                itemClickListener.onMessageClickableSpanClick(
                    widget, position, currentMessage, url);
              }
            }

            // 自定义链接样式（去掉下划线，设置颜色）
            @Override
            public void updateDrawState(TextPaint ds) {
              super.updateDrawState(ds);
              ds.setUnderlineText(false); // 去掉下划线
              ds.setColor(Color.parseColor("#007AFF")); // 设置链接颜色
            }
          };

      // 移除默认的 URLSpan，替换为自定义的 ClickableSpan
      spannable.removeSpan(span);
      spannable.setSpan(customSpan, start, end, flags);
    }
  }
}
