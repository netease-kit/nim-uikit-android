// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import com.netease.yunxin.kit.chatkit.map.IMessageMapProvider;
import com.netease.yunxin.kit.chatkit.map.IPageMapProvider;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachParser;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;

public class ChatKitClient {

  private static ChatUIConfig chatConfig;
  private static IPageMapProvider pageMapProvider;
  private static IMessageMapProvider messageMapProvider;

  @Deprecated
  public static void init(Context context) {}

  public static void setChatUIConfig(ChatUIConfig config) {
    chatConfig = config;
  }

  public static void setPageMapProvider(IPageMapProvider provider) {
    pageMapProvider = provider;
  }

  public static void setMessageMapProvider(IMessageMapProvider provider) {
    messageMapProvider = provider;
  }

  public static ChatUIConfig getChatUIConfig() {
    return chatConfig;
  }

  public static IPageMapProvider getPageMapProvider() {
    return pageMapProvider;
  }

  public static IMessageMapProvider getMessageMapProvider() {
    return messageMapProvider;
  }

  public static void addCustomAttach(int type, Class<? extends CustomAttachment> attachmentClass) {
    CustomAttachParser.Companion.getSInstance().addCustomAttach(type, attachmentClass);
  }

  public static void removeCustomAttach(int type) {
    CustomAttachParser.Companion.getSInstance().removeCustomAttach(type);
  }

  public static void addCustomViewHolder(
      int type, Class<? extends ChatBaseMessageViewHolder> attachmentClass) {
    ChatDefaultFactory.getInstance().addCustomViewHolder(type, attachmentClass);
  }

  public static void removeCustomViewHolder(int type) {
    ChatDefaultFactory.getInstance().removeCustomViewHolder(type);
  }
}
