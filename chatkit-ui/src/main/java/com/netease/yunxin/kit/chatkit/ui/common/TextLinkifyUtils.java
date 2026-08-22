// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.PHONE_NUMBER_PATTERN;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.TEL_SCHEME;

import android.content.Context;
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
import androidx.core.content.ContextCompat;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;

public class TextLinkifyUtils {

  public static void addLinks(
      TextView textView,
      IMessageItemClickListener itemClickListener,
      int position,
      ChatMessageBean currentMessage) {
    addLinks(textView, itemClickListener, position, currentMessage, getGlobalMessageProperties());
  }

  public static void addLinks(
      TextView textView,
      IMessageItemClickListener itemClickListener,
      int position,
      ChatMessageBean currentMessage,
      MessageProperties properties) {
    int mask = getTextLinkifyMask(properties);
    if (mask == 0) {
      textView.setMovementMethod(null);
      return;
    }
    SpannableString spannable = new SpannableString(textView.getText());
    addLinksByMask(spannable, mask);

    // 2. 移除默认的 ClickableSpan，替换为自定义的
    replaceClickableSpans(
        spannable, textView.getContext(), itemClickListener, position, currentMessage, properties);
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
    addLinks(textView, getGlobalMessageProperties());
  }

  public static void addLinks(TextView textView, MessageProperties properties) {
    int mask = getTextLinkifyMask(properties);
    if (mask == 0) {
      textView.setMovementMethod(null);
      return;
    }
    SpannableString spannable = new SpannableString(textView.getText());
    addLinksByMask(spannable, mask);

    // 3. 设置自定义的 MovementMethod（可选，用于处理点击）
    textView.setText(spannable);
    textView.setMovementMethod(LinkMovementMethod.getInstance());
  }

  // 替换 Spannable 中的默认 URLSpan 为自定义 ClickableSpan，拦截点击事件
  private static void replaceClickableSpans(
      Spannable spannable,
      Context context,
      IMessageItemClickListener itemClickListener,
      int position,
      ChatMessageBean currentMessage,
      MessageProperties properties) {
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
              ds.setUnderlineText(Boolean.TRUE.equals(getTextLinkUnderline(properties)));
              ds.setColor(getTextLinkColor(context, properties));
            }
          };

      // 移除默认的 URLSpan，替换为自定义的 ClickableSpan
      spannable.removeSpan(span);
      spannable.setSpan(customSpan, start, end, flags);
    }
  }

  private static void addLinksByMask(Spannable spannable, int mask) {
    int systemMask = 0;
    if ((mask & MessageProperties.TEXT_LINKIFY_EMAIL_ADDRESSES) != 0) {
      systemMask |= Linkify.EMAIL_ADDRESSES;
    }
    if ((mask & MessageProperties.TEXT_LINKIFY_WEB_URLS) != 0) {
      systemMask |= Linkify.WEB_URLS;
    }
    if (systemMask != 0) {
      Linkify.addLinks(spannable, systemMask);
    }
    if ((mask & MessageProperties.TEXT_LINKIFY_PHONE_NUMBERS) != 0) {
      Linkify.addLinks(spannable, PHONE_NUMBER_PATTERN, TEL_SCHEME);
    }
  }

  private static int getTextLinkifyMask(MessageProperties properties) {
    if (properties != null && properties.textLinkifyMask != null) {
      return properties.textLinkifyMask;
    }
    return MessageProperties.TEXT_LINKIFY_ALL;
  }

  private static int getTextLinkColor(Context context, MessageProperties properties) {
    if (properties != null && properties.textLinkColor != null) {
      return properties.textLinkColor;
    }
    return ContextCompat.getColor(context, R.color.color_007aff);
  }

  private static Boolean getTextLinkUnderline(MessageProperties properties) {
    if (properties != null) {
      return properties.textLinkUnderline;
    }
    return false;
  }

  private static MessageProperties getGlobalMessageProperties() {
    if (ChatKitClient.getChatUIConfig() != null) {
      return ChatKitClient.getChatUIConfig().messageProperties;
    }
    return null;
  }
}
