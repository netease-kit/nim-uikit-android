// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui;

/** 会话UI配置管理类 */
public class LocalConversationKitClient {

  // 会话UI配置
  private static LocalConversationUIConfig conversationConfig;

  public static void setConversationUIConfig(LocalConversationUIConfig config) {
    conversationConfig = config;
  }

  public static LocalConversationUIConfig getConversationUIConfig() {
    return conversationConfig;
  }
}
