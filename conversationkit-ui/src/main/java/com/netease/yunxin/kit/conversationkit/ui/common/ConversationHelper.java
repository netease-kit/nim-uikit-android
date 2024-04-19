// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 会话模块帮助类 */
public class ConversationHelper {

  // 会话中是否包含@消息，内存缓存
  private static Map<String, Boolean> aitInfo = new HashMap<>();

  /**
   * 更新会话中是否包含@消息
   *
   * @param sessionIdList 会话ID列表
   * @param hasAit 是否包含@消息
   */
  public static void updateAitInfo(List<String> sessionIdList, boolean hasAit) {
    if (sessionIdList != null) {
      for (String sessionId : sessionIdList) {
        aitInfo.put(sessionId, hasAit);
      }
    }
  }

  /**
   * 获取会话中是否包含@消息
   *
   * @param sessionId 会话ID
   * @return 是否包含@消息
   */
  public static boolean hasAit(String sessionId) {
    if (aitInfo.containsKey(sessionId)) {
      return aitInfo.get(sessionId);
    }
    return false;
  }
}
