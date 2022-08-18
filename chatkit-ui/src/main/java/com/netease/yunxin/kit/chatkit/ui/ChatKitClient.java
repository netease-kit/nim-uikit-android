// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import com.netease.yunxin.kit.chatkit.repo.ChatMessageRepo;
import com.netease.yunxin.kit.chatkit.ui.custom.CustomAttachParser;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.EmojiManager;

public class ChatKitClient {

  private static ChatUIConfig chatConfig;

  public static void setChatUIConfig(ChatUIConfig config) {
    chatConfig = config;
  }

  public static void init(Context context) {
    ChatMessageRepo.registerCustomAttachParser(new CustomAttachParser());
    EmojiManager.init(context);
  }

  public static ChatUIConfig getChatUIConfig() {
    return chatConfig;
  }
}
