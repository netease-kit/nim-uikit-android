// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import com.netease.nimlib.coexist.sdk.v2.conversation.enums.V2NIMConversationType;

/** 聊天界面回调接口 该接口定义了聊天界面的回调方法，用于处理会话切换等事件 */
public interface IChatListener {
  /**
   * 会话切换回调
   *
   * @param conversationId 会话ID
   * @param typeEnum 会话类型
   */
  void onConversationChange(String conversationId, V2NIMConversationType typeEnum);
}
