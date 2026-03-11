// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.content.Context;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.model.MessageGroup;
import com.netease.yunxin.kit.chatkit.ui.normal.search.ChatSearchDateUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MessageSearchUtils {

  private static final String TAG = "MessageSearchUtils";
  private static final String DATE_M_D_FORMATE = "yyyy-MM-dd";

  /**
   * 按日期（天）分组图片数据并排序 - 核心逻辑：按「天」生成唯一key（yyyy-MM-dd），每一个天对应一个分组 - 每组内按时间正序排列（旧→新） -
   * 分组整体按时间倒序展示（最新的天在前，如今天→昨天→2024-10-01）
   *
   * @param images 所有图片数据列表
   */
  public static List<MessageGroup> groupMessageByDay(Context context, List<V2NIMMessage> images) {
    // 1. 空值保护
    if (images == null || images.isEmpty()) {
      return new ArrayList<>();
    }

    // 按「天」分组（key：yyyy-MM-dd，TreeMap保证key按时间倒序排列）
    // 注：TreeMap的Comparator.reverseOrder() 让最新的日期key排在前面
    Map<String, List<V2NIMMessage>> dailyImages = new TreeMap<>();
    SimpleDateFormat dayKeyFormat = new SimpleDateFormat(DATE_M_D_FORMATE);

    // 2. 遍历图片，按天分组
    for (V2NIMMessage message : images) {
      long createTime = message.getCreateTime();
      Date date = new Date(createTime);
      String dayKey = dayKeyFormat.format(date); // 生成天级唯一key

      // 初始化分组列表
      if (!dailyImages.containsKey(dayKey)) {
        dailyImages.put(dayKey, new ArrayList<>());
      }
      dailyImages.get(dayKey).add(message);
    }

    // 3. 每组内按时间正序排序（旧→新：o1时间 < o2时间）
    for (List<V2NIMMessage> dayImageList : dailyImages.values()) {
      Collections.sort(
          dayImageList,
          new Comparator<V2NIMMessage>() {
            @Override
            public int compare(V2NIMMessage o1, V2NIMMessage o2) {
              // 正序排列：o2.createTime - o1.createTime（新的在前，旧的在后）
              return Long.compare(o1.getCreateTime(), o2.getCreateTime());
            }
          });
    }

    // 4. 构建ImageGroup列表（生成友好的日期展示文本：今天/昨天/yyyy-MM-dd）
    List<MessageGroup> newGroups = new ArrayList<>();
    for (Map.Entry<String, List<V2NIMMessage>> entry : dailyImages.entrySet()) {
      String dayKey = entry.getKey();
      List<V2NIMMessage> dayImages = entry.getValue();

      // 生成分组展示文本（友好化：今天/昨天/具体日期）
      String displayText;
      try {
        Date groupDate = dayKeyFormat.parse(dayKey);
        displayText = getFriendlyDateText(context, groupDate);
      } catch (ParseException e) {
        displayText = dayKey; // 解析失败则直接显示key
      }

      newGroups.add(new MessageGroup(dayKey, displayText, dayImages));
    }

    return newGroups;
  }

  /**
   * 生成日期展示文本（今天/昨天/yyyy-MM-dd）
   *
   * @param date 分组日期
   * @return 友好文本
   */
  public static String getFriendlyDateText(Context context, Date date) {
    long currentTime = System.currentTimeMillis();
    long targetTime = date.getTime();
    long oneDayMs = 24 * 60 * 60 * 1000L; // 一天的毫秒数

    // 计算与今天0点的时间差
    Date today = new Date();
    long todayZeroMs = getZeroTime(today).getTime();
    long targetZeroMs = getZeroTime(date).getTime();

    long dayDiff = (todayZeroMs - targetZeroMs) / oneDayMs;

    //        if (dayDiff == 0) {
    //            return this.getResources().getString(R.string.message_search_today); // 今天
    //        } else {
    // 其他日期显示具体的yyyy-MM-dd
    Calendar currentCalendar = Calendar.getInstance();
    int currentYear = currentCalendar.get(Calendar.YEAR);

    Calendar targetCalendar = Calendar.getInstance();
    targetCalendar.setTime(date);
    int targetYear = targetCalendar.get(Calendar.YEAR);
    // 5. 线程安全的SimpleDateFormat（避免多线程问题）
    SimpleDateFormat sdf;
    if (targetYear == currentYear) {
      // 今年：仅展示月-日
      sdf = new SimpleDateFormat(context.getString(R.string.chat_date_m_d_formate));
    } else {
      // 非今年：展示年-月-日
      sdf = new SimpleDateFormat(context.getString(R.string.chat_date_y_m_d_formate));
    }
    return sdf.format(date);
    //        }
  }

  public static Comparator getFileSortComparator() {
    return (Comparator<V2NIMMessage>)
        (o1, o2) -> {
          // 正序排列：o2.createTime - o1.createTime（新的在前，旧的在后）
          return Long.compare(o2.getCreateTime(), o1.getCreateTime());
        };
  }

  public static void groupMessageByMonth(
      Context context, List<MessageGroup> groups, List<V2NIMMessage> messages) {
    // key 使用 "yyyy-MM" 格式，保证字典序 == 时间序，reverseOrder() 排序才正确
    Map<String, List<V2NIMMessage>> monthlyFiles = new TreeMap<>(Collections.reverseOrder());
    for (V2NIMMessage message : messages) {
      // 用纯时间 key 分组，与展示文本完全解耦
      String monthKey = ChatSearchDateUtils.getMonthKey(new Date(message.getCreateTime()));
      if (monthlyFiles.containsKey(monthKey)) {
        monthlyFiles.get(monthKey).add(message);
      } else {
        ArrayList<V2NIMMessage> messageList = new ArrayList<>();
        messageList.add(message);
        monthlyFiles.put(monthKey, messageList);
      }
    }
    for (List<V2NIMMessage> list : monthlyFiles.values()) {
      Collections.sort(list, getFileSortComparator());
    }
    for (Map.Entry<String, List<V2NIMMessage>> entry : monthlyFiles.entrySet()) {
      String monthKey = entry.getKey();
      int idx = findGroupIndexByMonthKey(groups, monthKey);
      if (idx >= 0) {
        List<V2NIMMessage> existing = groups.get(idx).getMessageList();
        existing.addAll(entry.getValue());
        Collections.sort(existing, getFileSortComparator());
      } else {
        // 根据 "yyyy-MM" key 解析出 Date，再计算 UI 展示文本
        String displayText;
        try {
          Date groupDate = new SimpleDateFormat("yyyy-MM").parse(monthKey);
          displayText = ChatSearchDateUtils.getMonthDisplayText(context, groupDate);
        } catch (Exception e) {
          displayText = monthKey;
        }
        MessageGroup newGroup = new MessageGroup(monthKey, displayText, entry.getValue());
        // 插入到正确位置（按 "yyyy-MM" 字典序倒序）
        int insertPos = 0;
        while (insertPos < groups.size()) {
          String mk = groups.get(insertPos).getGroupKey();
          if (mk.compareTo(monthKey) >= 0) {
            insertPos++;
          } else {
            break;
          }
        }
        groups.add(insertPos, newGroup);
      }
    }
  }

  public static int findGroupIndexByMonthKey(List<MessageGroup> groups, String monthKey) {
    for (int i = 0; i < groups.size(); i++) {
      if (monthKey.equals(groups.get(i).getGroupKey())) {
        return i;
      }
    }
    return -1;
  }

  /** 获取指定日期的0点时间（用于计算天差） */
  private static Date getZeroTime(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_M_D_FORMATE);
    try {
      return sdf.parse(sdf.format(date));
    } catch (ParseException e) {
      return date;
    }
  }
}
