// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options;

import android.view.View;

/** 包含消息的发送状态，消息已读/未读，已经消息发送成功状态 */
public class MessageStatusUIOption {
  /** 默认最大支持已读/未读数 */
  public static final int DEFAULT_MAX_READING_COUNT = 100;
  /** 是否支持状态展示 */
  public Boolean enableStatus;
  /** 最大未读数支持上限 */
  public Integer maxReadingNum;
  /** 是否展示消息发送中状态 */
  public Boolean showSendingStatus;
  /** 是否展示消息失败转台 */
  public Boolean showFailedStatus;
  /** 是否展示已读状态展示 */
  public Boolean showReadStatus;
  /** 群聊已读/未读颜色值 */
  public Integer readProgressColor;
  /** 已读消息图标资源 */
  public Integer readFlagIconRes;
  /** 未读消息图标资源 */
  public Integer unreadFlagIconRes;
  /** 消息失败图标资源 */
  public Integer failedFlagIconRes;
  /** 已读/未读进度点击事件监听 */
  public View.OnClickListener readProcessClickListener;

  @Override
  public String toString() {
    return "MessageStatusUIOption{"
        + "enableStatus="
        + enableStatus
        + ", maxReadingNum="
        + maxReadingNum
        + ", showSendingStatus="
        + showSendingStatus
        + ", showFailedStatus="
        + showFailedStatus
        + ", showReadStatus="
        + showReadStatus
        + ", readProgressColor="
        + readProgressColor
        + ", readFlagIconRes="
        + readFlagIconRes
        + ", unreadFlagIconRes="
        + unreadFlagIconRes
        + ", failedFlagIconRes="
        + failedFlagIconRes
        + ", readProcessClickListener="
        + readProcessClickListener
        + '}';
  }
}
