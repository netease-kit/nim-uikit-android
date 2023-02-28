// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.netease.yunxin.kit.qchatkit.ui.message.emoji.EmojiManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {
  private static final float DEF_SCALE = 0.6f;
  private static final float SMALL_SCALE = 0.6F;

  public static void identifyFaceExpression(
      Context context, View textView, String value, int align) {
    identifyFaceExpression(context, textView, value, align, DEF_SCALE);
  }

  private static void viewSetText(View textView, SpannableString mSpannableString) {
    if (textView instanceof TextView) {
      TextView tv = (TextView) textView;
      tv.setText(mSpannableString);
    } else if (textView instanceof EditText) {
      EditText et = (EditText) textView;
      et.setText(mSpannableString);
    }
  }

  public static void identifyFaceExpression(
      Context context, View textView, String value, int align, float scale) {
    SpannableString mSpannableString = replaceEmoticons(context, value, scale, align);
    viewSetText(textView, mSpannableString);
  }

  private static SpannableString replaceEmoticons(
      Context context, String value, float scale, int align) {
    if (TextUtils.isEmpty(value)) {
      value = "";
    }

    SpannableString mSpannableString = new SpannableString(value);
    Matcher matcher = EmojiManager.getPattern().matcher(value);
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      String emot = value.substring(start, end);
      Drawable d = getEmotDrawable(context, emot, scale);
      if (d != null) {
        ImageSpan span = new ImageSpan(d, align);
        mSpannableString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
    }
    return mSpannableString;
  }

  private static Pattern mATagPattern = Pattern.compile("<a.*?>.*?</a>");

  public static void replaceEmoticons(Context context, Editable editable, int start, int count) {
    if (count <= 0 || editable.length() < start + count) return;

    CharSequence s = editable.subSequence(start, start + count);
    Matcher matcher = EmojiManager.getPattern().matcher(s);
    while (matcher.find()) {
      int from = start + matcher.start();
      int to = start + matcher.end();
      String emot = editable.subSequence(from, to).toString();
      Drawable d = getEmotDrawable(context, emot, SMALL_SCALE);
      if (d != null) {
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
        editable.setSpan(span, from, to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
    }
  }

  private static Drawable getEmotDrawable(Context context, String text, float scale) {
    Drawable drawable = EmojiManager.getDrawable(context, text);

    // scale
    if (drawable != null) {
      int width = (int) (drawable.getIntrinsicWidth() * scale);
      int height = (int) (drawable.getIntrinsicHeight() * scale);
      drawable.setBounds(0, 0, width, height);
    }

    return drawable;
  }

  private static class ATagSpan extends ClickableSpan {
    private int start;
    private int end;
    private String mUrl;
    private String tag;

    ATagSpan(String tag, String url) {
      this.tag = tag;
      this.mUrl = url;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
      super.updateDrawState(ds);
      ds.setUnderlineText(true);
    }

    public String getTag() {
      return tag;
    }

    public void setRange(int start, int end) {
      this.start = start;
      this.end = end;
    }

    @Override
    public void onClick(View widget) {
      try {
        if (TextUtils.isEmpty(mUrl)) return;
        Uri uri = Uri.parse(mUrl);
        String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) {
          mUrl = "http://" + mUrl;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
