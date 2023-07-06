// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options;

import android.graphics.drawable.Drawable;

public class CommonUIOption {
  /** 默认消息之间的时间间隔 */
  public static final long DEFAULT_MESSAGE_TIME_INTERVAL_MILLISECOND = 5 * 60 * 1000;
  /** 消息时间间隔 */
  public Long messageTimeIntervalMillisecond;
  /** 消息时间是否展示 */
  public Boolean timeVisible;
  /** 时间大小 */
  public Integer timeSize;
  /** 时间展示颜色 */
  public Integer timeColor;
  /** 时间格式 */
  public String timeFormat;
  /** 消息体布局的居左/居中/居右 */
  public @MessageContentLayoutGravity Integer messageContentLayoutGravity;
  /** 当前用户消息背景 */
  public Drawable myMessageBg;
  /** 当前用户消息背景资源 id */
  public Integer myMessageBgRes;
  /** 非当前用户消息背景 */
  public Drawable otherUserMessageBg;
  /** 非当前用户消息背景资源 id */
  public Integer otherUserMessageBgRes;
  /** 文本消息文字颜色 */
  public Integer messageTextColor;
  /** 文本消息文字大小 */
  public Integer messageTextSize;

  @Override
  public String toString() {
    return "CommonUIOption{"
        + "messageTimeIntervalMillisecond="
        + messageTimeIntervalMillisecond
        + ", timeVisible="
        + timeVisible
        + ", timeSize="
        + timeSize
        + ", timeColor="
        + timeColor
        + ", timeFormat='"
        + timeFormat
        + '\''
        + ", messageContentLayoutGravity="
        + messageContentLayoutGravity
        + ", myMessageBg="
        + myMessageBg
        + ", myMessageBgRes="
        + myMessageBgRes
        + ", otherUserMessageBg="
        + otherUserMessageBg
        + ", otherUserMessageBgRes="
        + otherUserMessageBgRes
        + ", messageTextColor="
        + messageTextColor
        + ", messageTextSize="
        + messageTextSize
        + '}';
  }

  public @interface MessageContentLayoutGravity {
    /** 消息内容居右 */
    float right = 1;
    /** 消息内容居左 */
    float left = 0;
    /** 消息内容居中 */
    float center = 0.5f;
  }
}
