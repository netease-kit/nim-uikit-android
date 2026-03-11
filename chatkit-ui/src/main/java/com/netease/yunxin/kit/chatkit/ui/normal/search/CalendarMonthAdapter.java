// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.ui.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class CalendarMonthAdapter
    extends RecyclerView.Adapter<CalendarMonthAdapter.MonthViewHolder> {
  private final List<Calendar> months;
  private final long minUtc;
  private final long maxUtc;
  private final OnDayClickListener listener;
  private Long selectedDay;

  CalendarMonthAdapter(
      List<Calendar> months, long minUtc, long maxUtc, OnDayClickListener listener) {
    this.months = months;
    this.minUtc = minUtc;
    this.maxUtc = maxUtc;
    this.listener = listener;
  }

  public void setSelectedDay(long selectedDay) {
    this.selectedDay = selectedDay;
    notifyDataSetChanged();
  }

  @Override
  public MonthViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
    View v =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.chat_calendar_month_item, parent, false);
    return new MonthViewHolder(v);
  }

  @Override
  public void onBindViewHolder(MonthViewHolder holder, int position) {
    Calendar m = months.get(position);
    if (holder.monthHeader != null) {
      int monthNum = m.get(Calendar.MONTH) + 1;
      holder.monthHeader.setText(
          String.format(
              holder.itemView.getContext().getString(R.string.chat_calendar_month_number),
              monthNum));
    }
    GridLayoutManager gridLayoutManager = new GridLayoutManager(holder.itemView.getContext(), 7);
    holder.grid.setLayoutManager(gridLayoutManager);
    DayGridAdapter gridAdapter = new DayGridAdapter(m);
    holder.grid.setAdapter(gridAdapter);
  }

  @Override
  public int getItemCount() {
    return months.size();
  }

  class MonthViewHolder extends RecyclerView.ViewHolder {
    RecyclerView grid;
    TextView monthHeader;

    MonthViewHolder(android.view.View itemView) {
      super(itemView);
      grid = itemView.findViewById(R.id.rvMonthGrid);
      monthHeader = itemView.findViewById(R.id.tvMonthHeader);
    }
  }

  class DayGridAdapter extends RecyclerView.Adapter<DayGridAdapter.DayViewHolder> {
    private final List<Long> days = new ArrayList<>();

    DayGridAdapter(Calendar month) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(month.getTimeInMillis());
      cal.set(Calendar.DAY_OF_MONTH, 1);
      int firstDow = cal.get(Calendar.DAY_OF_WEEK);
      int blanks = (firstDow + 6) % 7;
      for (int i = 0; i < blanks; i++) {
        days.add(null);
      }
      int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
      for (int d = 1; d <= maxDay; d++) {
        cal.set(Calendar.DAY_OF_MONTH, d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long ms = cal.getTimeInMillis();
        days.add(ms);
      }
    }

    @Override
    public DayViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
      View v =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.chat_calendar_day_item, parent, false);
      return new DayViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DayViewHolder holder, int position) {
      Long ms = days.get(position);
      TextView tv = holder.day;
      TextView tvToday = holder.todayLabel;
      int screenWidth = holder.itemView.getResources().getDisplayMetrics().widthPixels;
      float dp = holder.itemView.getResources().getDisplayMetrics().density;
      int horizontalPadding = (int) (32 * dp); // month item 16dp start+end
      int itemPaddingTotal = (int) (8 * dp * 7); // 4dp left+right per item * 7 columns
      int size = (screenWidth - horizontalPadding - itemPaddingTotal) / 7;
      ViewGroup.LayoutParams lp = tv.getLayoutParams();
      lp.width = size;
      lp.height = size;
      tv.setLayoutParams(lp);
      if (ms == null) {
        tv.setText("");
        tv.setSelected(false);
        tv.setEnabled(false);
        tv.setOnClickListener(null);
        tvToday.setVisibility(View.GONE);
      } else {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        tv.setText(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
        boolean enabled = ms >= minUtc && ms <= maxUtc;
        tv.setEnabled(enabled);
        if (selectedDay != null && Objects.equals(selectedDay, ms)) {
          tv.setSelected(true);
        } else {
          tv.setSelected(false);
        }
        if (ms > maxUtc) {
          // hide future dates
          tv.setText("");
          tv.setOnClickListener(null);
          tvToday.setVisibility(View.GONE);
        } else if (enabled) {
          tv.setOnClickListener(v -> listener.onDayClick(ms));
          if (ms == maxUtc) {
            tvToday.setVisibility(View.VISIBLE);
          } else {
            tvToday.setVisibility(View.GONE);
          }
        } else {
          tv.setOnClickListener(null);
          tvToday.setVisibility(View.GONE);
        }
      }
    }

    @Override
    public int getItemCount() {
      return days.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
      TextView day;
      TextView todayLabel;

      DayViewHolder(View itemView) {
        super(itemView);
        day = itemView.findViewById(R.id.tvDay);
        todayLabel = itemView.findViewById(R.id.tvTodayLabel);
      }
    }
  }

  interface OnDayClickListener {
    void onDayClick(Long dayMillis);
  }
}
