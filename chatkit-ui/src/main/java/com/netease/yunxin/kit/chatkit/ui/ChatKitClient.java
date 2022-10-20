// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.custom.CustomAttachParser;
import com.netease.yunxin.kit.chatkit.ui.custom.CustomAttachment;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.EmojiManager;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;

public class ChatKitClient {

  private static ChatUIConfig chatConfig;

  public static void setChatUIConfig(ChatUIConfig config) {
    chatConfig = config;
  }

  public static void init(Context context) {
    ChatRepo.registerCustomAttachParser(CustomAttachParser.getInstance());
    EmojiManager.init(context);
  }

  public static ChatUIConfig getChatUIConfig() {
    return chatConfig;
  }

  public static void addCustomAttach(int type, Class<? extends CustomAttachment> attachmentClass) {
    CustomAttachParser.getInstance().addCustomAttach(type, attachmentClass);
  }

  public static void removeCustomAttach(int type) {
    CustomAttachParser.getInstance().removeCustomAttach(type);
  }

  public static void addCustomViewHolder(
      int type, Class<? extends ChatBaseMessageViewHolder> attachmentClass) {
    ChatDefaultFactory.getInstance().addCustomViewHolder(type, attachmentClass);
  }

  public static void removeCustomViewHolder(int type) {
    ChatDefaultFactory.getInstance().removeCustomViewHolder(type);
  }
}
