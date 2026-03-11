// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.search;

import android.content.Context;
import android.text.format.DateFormat;
import com.netease.yunxin.kit.chatkit.ui.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class ChatSearchDateUtils {

  private ChatSearchDateUtils() {}

  public static boolean isThisWeek(Date date) {
    Calendar current = Calendar.getInstance();
    Calendar target = Calendar.getInstance();
    target.setTime(date);
    Calendar weekStart = Calendar.getInstance();
    weekStart.setTimeInMillis(current.getTimeInMillis());
    int dayOfWeek = weekStart.get(Calendar.DAY_OF_WEEK);
    weekStart.add(Calendar.DATE, -(dayOfWeek - 1));
    Calendar weekEnd = Calendar.getInstance();
    weekEnd.setTimeInMillis(weekStart.getTimeInMillis());
    weekEnd.add(Calendar.DATE, 6);
    return (date.after(weekStart.getTime()) && date.before(weekEnd.getTime()))
        || date.equals(weekStart.getTime())
        || date.equals(weekEnd.getTime());
  }

  public static boolean isThisMonthNotWeek(Date date) {
    Calendar current = Calendar.getInstance();
    Calendar target = Calendar.getInstance();
    target.setTime(date);
    boolean isCurrentYearAndMonth =
        (current.get(Calendar.YEAR) == target.get(Calendar.YEAR))
            && (current.get(Calendar.MONTH) == target.get(Calendar.MONTH));
    return isCurrentYearAndMonth && !isThisWeek(date);
  }

  public static String getMonthKey(Date date) {
    SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
    return monthFormat.format(date);
  }

  public static String getMonthDisplayText(Context context, Date date) {
    Calendar current = Calendar.getInstance();
    Calendar target = Calendar.getInstance();
    target.setTime(date);
    if (current.get(Calendar.YEAR) == target.get(Calendar.YEAR)) {
      return DateFormat.format(context.getString(R.string.chat_month_format_current_year), date)
          .toString();
    } else {
      return DateFormat.format(context.getString(R.string.chat_month_format_other_year), date)
          .toString();
    }
  }

  public static long getTodayZeroMillis() {
    Calendar todayCal = Calendar.getInstance();
    todayCal.set(Calendar.HOUR_OF_DAY, 0);
    todayCal.set(Calendar.MINUTE, 0);
    todayCal.set(Calendar.SECOND, 0);
    todayCal.set(Calendar.MILLISECOND, 0);
    return todayCal.getTimeInMillis();
  }

  public static long getMinLocalMillisWithinYears(long todayLocal, int yearsBack) {
    Calendar minCal = Calendar.getInstance();
    minCal.setTimeInMillis(todayLocal);
    minCal.add(Calendar.YEAR, -yearsBack);
    return minCal.getTimeInMillis();
  }
}
