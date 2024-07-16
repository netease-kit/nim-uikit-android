// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.widgets.datepicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatUtils {

  private static final String DATE_FORMAT_PATTERN_YMD = "yyyy-MM-dd";
  private static final String DATE_FORMAT_PATTERN_YMD_HM = "yyyy-MM-dd HH:mm";
  private static final String DATE_FORMAT_PATTERN_HS = "mm:ss";

  /** timestamp to data string */
  public static String long2Str(long timestamp, boolean isPreciseTime) {
    return long2Str(timestamp, getFormatPattern(isPreciseTime));
  }

  public static String long2StrHS(long timestamp) {
    return long2Str(timestamp, DATE_FORMAT_PATTERN_HS);
  }

  private static String long2Str(long timestamp, String pattern) {
    return new SimpleDateFormat(pattern, Locale.CHINA).format(new Date(timestamp));
  }

  /** date time to timestamp */
  public static long str2Long(String dateStr, boolean isPreciseTime) {
    return str2Long(dateStr, getFormatPattern(isPreciseTime));
  }

  private static long str2Long(String dateStr, String pattern) {
    try {
      return new SimpleDateFormat(pattern, Locale.CHINA).parse(dateStr).getTime();
    } catch (Throwable ignored) {
    }
    return 0;
  }

  private static String getFormatPattern(boolean showSpecificTime) {
    if (showSpecificTime) {
      return DATE_FORMAT_PATTERN_YMD_HM;
    } else {
      return DATE_FORMAT_PATTERN_YMD;
    }
  }
}
