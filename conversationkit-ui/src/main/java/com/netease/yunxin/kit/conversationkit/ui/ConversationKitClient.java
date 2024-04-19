// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui;

/** 会话UI配置管理类 */
public class ConversationKitClient {

  // 会话UI配置
  private static ConversationUIConfig sConversationConfig;

  public static void setConversationUIConfig(ConversationUIConfig config) {
    sConversationConfig = config;
  }

  public static ConversationUIConfig getConversationUIConfig() {
    return sConversationConfig;
  }
}
