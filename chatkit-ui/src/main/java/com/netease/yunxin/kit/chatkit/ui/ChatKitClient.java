// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.map.IMessageMapProvider;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.CommonBaseMessageViewHolder;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachParser;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;

/** Chat模块定制能力入口类。 */
public class ChatKitClient {

  private static ChatUIConfig chatConfig;
  private static IMessageMapProvider messageMapProvider;

  @Deprecated
  public static void init(Context context) {}

  public static void setChatUIConfig(ChatUIConfig config) {
    chatConfig = config;
  }

  public static void setMessageMapProvider(IMessageMapProvider provider) {
    messageMapProvider = provider;
  }

  public static ChatUIConfig getChatUIConfig() {
    return chatConfig;
  }

  public static @Nullable IMessageMapProvider getMessageMapProvider() {
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
    ChatViewHolderDefaultFactory.getInstance().addCustomViewHolder(type, attachmentClass);
  }

  public static void removeCustomViewHolder(int type) {
    ChatViewHolderDefaultFactory.getInstance().removeCustomViewHolder(type);
  }

  public static void addCommonCustomViewHolder(
      int type,
      Class<? extends CommonBaseMessageViewHolder> holderClass,
      @LayoutRes int layoutRes) {
    ChatViewHolderDefaultFactory.getInstance()
        .addCommonCustomViewHolder(type, holderClass, layoutRes);
  }

  public static void removeCommonCustomViewHolder(int type) {
    ChatViewHolderDefaultFactory.getInstance().removeCommonCustomViewHolder(type);
  }
}
