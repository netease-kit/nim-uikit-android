// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.utils;

import android.content.Context;
import com.netease.yunxin.kit.common.ui.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeFormatUtils {

  public static String formatMillisecond(Context context, long millisecond) {
    long nowTime = System.currentTimeMillis();
    Calendar nowCalendar = Calendar.getInstance();
    nowCalendar.setTimeInMillis(nowTime);
    int nowDay = nowCalendar.get(Calendar.DATE);
    int nowMonth = nowCalendar.get(Calendar.MONTH);
    int nowYear = nowCalendar.get(Calendar.YEAR);
    Calendar timeCalendar = Calendar.getInstance();
    timeCalendar.setTimeInMillis(millisecond);
    int timeDay = timeCalendar.get(Calendar.DATE);
    int timeMonth = timeCalendar.get(Calendar.MONTH);
    int timeYear = timeCalendar.get(Calendar.YEAR);
    Date dateTime = new Date(millisecond);
    SimpleDateFormat fullTimeFormat;
    if (timeYear != nowYear) {
      fullTimeFormat = new SimpleDateFormat(context.getString(R.string.time_format_y_m_d_h_m));
    } else if (timeMonth != nowMonth || timeDay != nowDay) {
      fullTimeFormat = new SimpleDateFormat(context.getString(R.string.time_format_m_d_h_m));
    } else {
      fullTimeFormat = new SimpleDateFormat("HH:mm");
    }
    return fullTimeFormat.format(dateTime);
  }
}
