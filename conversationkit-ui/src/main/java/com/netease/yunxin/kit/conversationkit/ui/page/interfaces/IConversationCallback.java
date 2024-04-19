// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page.interfaces;

/** 会话Fragment提供给外部回调 用于获取未读数据变化回调，后续根据需求会继续扩展 */
public interface IConversationCallback {

  void updateUnreadCount(int count);
}
