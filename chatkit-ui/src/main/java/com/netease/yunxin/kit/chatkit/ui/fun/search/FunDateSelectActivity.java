// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.search;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatCalendarRangeActivityBinding;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.widgets.datepicker.CustomDatePicker;
import java.util.ArrayList;
import java.util.Calendar;

public class FunDateSelectActivity extends BaseLocalActivity {

  private final long Days7Diff = 7L * 24 * 60 * 60 * 1000;
  private final long Days30Diff = 30L * 24 * 60 * 60 * 1000;
  private final int minYear = 1970;
  private FunChatCalendarRangeActivityBinding binding;
  private Long selectedDay;
  private Long todayLocal;
  private Long monthTitleSelected;
  private TextView tvMonthTitle;
  private ImageView ivMonthDropdown;
  private RecyclerView rvCalendar;
  private FunCalendarMonthAdapter calendarMonthAdapter;
  private ArrayList<Calendar> months;
  private LinearLayoutManager calendarLayoutManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.fun_chat_secondary_page_bg_color);
    binding = FunChatCalendarRangeActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    binding.searchTitleBar.setTitle(R.string.chat_calendar_filter_by_date);
    binding.searchTitleBar.setOnBackIconClickListener(v -> finish());
    binding.searchTitleBar.setActionText(R.string.chat_complete_text);
    binding.searchTitleBar.setActionTextColor(
        getResources().getColor(R.color.fun_chat_search_item_selected_color));

    binding.searchTitleBar.setRightTextViewVisible(View.VISIBLE);
    binding.searchTitleBar.setActionTextListener(
        v -> {
          if (selectedDay != null) {
            onSelectDate(selectedDay);
          }
        });

    Calendar todayCal = Calendar.getInstance();
    todayCal.set(Calendar.HOUR_OF_DAY, 0);
    todayCal.set(Calendar.MINUTE, 0);
    todayCal.set(Calendar.SECOND, 0);
    todayCal.set(Calendar.MILLISECOND, 0);
    todayLocal = todayCal.getTimeInMillis();
    monthTitleSelected = todayLocal;
    Calendar minCal = Calendar.getInstance();
    minCal.set(Calendar.YEAR, minYear);
    minCal.set(Calendar.MONTH, Calendar.JANUARY);
    minCal.set(Calendar.DAY_OF_MONTH, 1);
    minCal.set(Calendar.HOUR_OF_DAY, 0);
    minCal.set(Calendar.MINUTE, 0);
    minCal.set(Calendar.SECOND, 0);
    minCal.set(Calendar.MILLISECOND, 0);
    long minLocal = minCal.getTimeInMillis();

    tvMonthTitle = binding.tvMonthTitle;
    ivMonthDropdown = binding.ivMonthDropdown;
    rvCalendar = binding.rvCalendar;
    calendarLayoutManager = new LinearLayoutManager(this);
    rvCalendar.setLayoutManager(calendarLayoutManager);

    months = new ArrayList<>();
    Calendar cursor = Calendar.getInstance();
    cursor.setTimeInMillis(minLocal);
    cursor.set(Calendar.DAY_OF_MONTH, 1);
    Calendar endCal = Calendar.getInstance();
    endCal.setTimeInMillis(todayLocal);
    endCal.set(Calendar.DAY_OF_MONTH, 1);
    while (!cursor.after(endCal)) {
      Calendar m = Calendar.getInstance();
      m.setTimeInMillis(cursor.getTimeInMillis());
      months.add(m);
      cursor.add(Calendar.MONTH, 1);
    }

    selectedDay = todayLocal;
    calendarMonthAdapter =
        new FunCalendarMonthAdapter(
            months,
            minLocal,
            todayLocal,
            dayMillis -> {
              setSelectedDay(dayMillis);
            });
    calendarMonthAdapter.setSelectedDay(todayLocal);
    rvCalendar.setAdapter(calendarMonthAdapter);

    rvCalendar.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int first = calendarLayoutManager.findFirstVisibleItemPosition();
            int last = calendarLayoutManager.findLastVisibleItemPosition();
            if (first >= 0 && last >= first && last < months.size()) {
              int center = first + ((last - first) >> 1);
              Calendar m = months.get(center);
              Calendar c = Calendar.getInstance();
              c.setTimeInMillis(m.getTimeInMillis());
              monthTitleSelected = c.getTimeInMillis();
              tvMonthTitle.setText(
                  String.format("%d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1));
            }
          }
        });
    if (!months.isEmpty()) {
      Calendar c = Calendar.getInstance();
      long initialMs =
          selectedDay != null ? selectedDay : months.get(months.size() - 1).getTimeInMillis();
      c.setTimeInMillis(initialMs);
      tvMonthTitle.setText(
          String.format("%d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1));
      rvCalendar.scrollToPosition(months.size() - 1);
    }

    View monthTitleLayout = binding.monthTitleLayout;
    View.OnClickListener showPickerListener =
        v -> showYearMonthPicker(minLocal, todayLocal, calendarLayoutManager);
    monthTitleLayout.setOnClickListener(showPickerListener);
    ivMonthDropdown.setOnClickListener(showPickerListener);

    binding.btnToday.setOnClickListener(
        v -> {
          setSelectedDay(todayLocal);
        });

    binding.btnLast7.setOnClickListener(
        v -> {
          setSelectedDayByDiffDays(-7);
        });

    binding.btnLast30.setOnClickListener(
        v -> {
          setSelectedDayByDiffDays(-30);
        });

    binding.btnToday.setSelected(true);
    binding.btnLast7.setSelected(false);
    binding.btnLast30.setSelected(false);
  }

  protected void onSelectDate(long selectedDate) {}

  public void setSelectedDayByDiffDays(int diffDays) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(todayLocal);
    c.add(Calendar.DAY_OF_YEAR, diffDays);
    setSelectedDay(c.getTimeInMillis());
  }

  public void setSelectedDay(long day) {
    if (day == todayLocal) {
      binding.btnToday.setSelected(true);
      binding.btnLast7.setSelected(false);
      binding.btnLast30.setSelected(false);
    } else if (day == todayLocal - Days7Diff) {
      binding.btnToday.setSelected(false);
      binding.btnLast7.setSelected(true);
      binding.btnLast30.setSelected(false);
    } else if (day == todayLocal - Days30Diff) {
      binding.btnToday.setSelected(false);
      binding.btnLast7.setSelected(false);
      binding.btnLast30.setSelected(true);
    } else {
      binding.btnToday.setSelected(false);
      binding.btnLast7.setSelected(false);
      binding.btnLast30.setSelected(false);
    }
    selectedDay = day;
    monthTitleSelected = selectedDay;
    if (calendarMonthAdapter != null) {
      calendarMonthAdapter.setSelectedDay(selectedDay);
    }
    Calendar sel = Calendar.getInstance();
    sel.setTimeInMillis(selectedDay);
    tvMonthTitle.setText(
        String.format("%d-%02d", sel.get(Calendar.YEAR), sel.get(Calendar.MONTH) + 1));
    // 滚动到选中日期所在月份
    int pos = findMonthPosition(sel.get(Calendar.YEAR), sel.get(Calendar.MONTH) + 1);
    if (pos >= 0 && calendarLayoutManager != null) {
      calendarLayoutManager.scrollToPositionWithOffset(pos, 0);
    }
  }

  private void showYearMonthPicker(
      long minLocal, long todayLocal, LinearLayoutManager calendarLayoutManager) {
    CustomDatePicker picker =
        new CustomDatePicker(
            this,
            timestamp -> {
              Calendar target = Calendar.getInstance();
              target.setTimeInMillis(timestamp);
              int y = target.get(Calendar.YEAR);
              int m = target.get(Calendar.MONTH) + 1;
              int pos = findMonthPosition(y, m);
              monthTitleSelected = timestamp;
              tvMonthTitle.setText(String.format("%d-%02d", y, m));
              if (pos >= 0) {
                calendarLayoutManager.scrollToPosition(pos);
              }
            },
            minLocal,
            todayLocal,
            this.getResources().getColor(R.color.fun_chat_color));
    picker.setCancelable(true);
    picker.setYearMonthMode(true);
    picker.show(monthTitleSelected != null ? monthTitleSelected : todayLocal);
  }

  private int findMonthPosition(int year, int month) {
    for (int i = 0; i < months.size(); i++) {
      Calendar c = months.get(i);
      int y = c.get(Calendar.YEAR);
      int m = c.get(Calendar.MONTH) + 1;
      if (y == year && m == month) {
        return i;
      }
    }
    return -1;
  }
}
