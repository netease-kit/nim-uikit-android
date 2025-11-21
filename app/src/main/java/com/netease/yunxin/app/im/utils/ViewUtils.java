// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.netease.yunxin.app.im.R;
import com.netease.yunxin.kit.common.ui.activities.BrowseActivity;
import com.netease.yunxin.kit.common.utils.SizeUtils;

public class ViewUtils {

  public static String reportUrl = "https://yunxin.163.com/survey/report";

  public static void addTipsView(FrameLayout parent, Context context, boolean showDelete) {
    View tipsView =
        generateTipsView(
            context,
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                parent.removeView(v);
              }
            },
            showDelete);
    ViewGroup.LayoutParams layoutParams =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    parent.setBackgroundResource(R.color.color_FFF5E1);
    parent.addView(tipsView, layoutParams);
  }

  public static View generateTipsView(
      Context context, View.OnClickListener onClickListener, boolean showDelete) {
    TextView textView = new TextView(context);
    String tipsText = "  " + context.getString(R.string.yunxin_tips);
    String reportText = context.getString(R.string.yunxin_report_tips) + "  ";
    String tips = tipsText + reportText;
    ClickableSpan clickableSpan =
        new ClickableSpan() {
          @Override
          public void onClick(View widget) {
            // 点击后跳转到其他页面（示例：跳转到 SecondActivity）
            BrowseActivity.Companion.launch(
                context, context.getString(R.string.yunxin_report_tips), reportUrl);
          }

          // 可选：去除点击后的下划线（默认有下划线）
          @Override
          public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false); // 去掉下划线
            ds.setColor(Color.BLUE); // 手动设置蓝色（也可在布局中通过 textColorLink 统一设置）
          }
        };
    // 4. 为目标文本片段设置 ClickableSpan
    SpannableString spannableString = new SpannableString(tips);
    spannableString.setSpan(
        clickableSpan,
        tipsText.length(),
        tips.length() - 2,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE // Span 范围：不包含两端的文本
        );
    Drawable startDrawable = ContextCompat.getDrawable(context, R.drawable.ic_app_error);
    if (startDrawable != null) {
      // 设置图标大小（可选，避免图标过大）
      startDrawable.setBounds(
          0, -SizeUtils.dp2px(2), SizeUtils.dp2px(12), SizeUtils.dp2px(10)); // 20dp ×
      // 20dp
      ImageSpan startImageSpan = new ImageSpan(startDrawable, ImageSpan.ALIGN_CENTER);
      // 将 ImageSpan 插入到文本开头（索引0位置）
      spannableString.setSpan(startImageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    if (showDelete) {

      Drawable endDrawable = ContextCompat.getDrawable(context, R.drawable.ic_app_clear);
      if (endDrawable != null) {
        endDrawable.setBounds(0, 0, SizeUtils.dp2px(16), SizeUtils.dp2px(16)); // 20dp × 20dp
        ImageSpan endImageSpan = new ImageSpan(endDrawable, ImageSpan.ALIGN_CENTER);
        spannableString.setSpan(
            endImageSpan, tips.length() - 1, tips.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }

      ClickableSpan endClickableSpan =
          new ClickableSpan() {
            @Override
            public void onClick(View widget) {
              // 结尾图标点击逻辑
              if (onClickListener != null) {
                onClickListener.onClick(widget);
              }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
              super.updateDrawState(ds);
              ds.setUnderlineText(false);
            }
          };
      spannableString.setSpan(
          endClickableSpan, tips.length() - 1, tips.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    // 5. 设置文本并启用点击事件（必须添加，否则点击无效）
    textView.setText(spannableString);
    textView.setMovementMethod(LinkMovementMethod.getInstance());
    textView.setTextColor(context.getResources().getColor(R.color.color_EB9718));
    textView.setMaxLines(3);
    textView.setTextSize(13);
    textView.setLineSpacing(1, 1.2f);
    textView.setEllipsize(TextUtils.TruncateAt.END);
    textView.setPadding(
        SizeUtils.dp2px(16), SizeUtils.dp2px(6), SizeUtils.dp2px(16), SizeUtils.dp2px(4));

    return textView;
  }
}
