// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import androidx.annotation.ColorInt;
import com.netease.nimlib.sdk.search.model.RecordHitInfo;

public class TextUtils {
  public static SpannableString getSelectSpanText(
      @ColorInt int color, String text, RecordHitInfo hitInfo) {
    SpannableString spannable = new SpannableString(text);
    if (hitInfo != null) {
      spannable.setSpan(
          new ForegroundColorSpan(color),
          hitInfo.start,
          hitInfo.end,
          Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }
    return spannable;
  }
}
