// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui;

public class ConversationKitClient {

  private static ConversationUIConfig sConversationConfig;

  public static void setConversationUIConfig(ConversationUIConfig config) {
    sConversationConfig = config;
  }

  public static ConversationUIConfig getConversationUIConfig() {
    return sConversationConfig;
  }
}
