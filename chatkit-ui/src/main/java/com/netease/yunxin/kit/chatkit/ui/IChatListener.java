// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;

public interface IChatListener {
  void onConversationChange(String conversationId, V2NIMConversationType typeEnum);
}
