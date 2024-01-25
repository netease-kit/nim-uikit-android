// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 消息内存缓存 主要用于多选功能，将选中的信息保存到这里，在选中状态维护、合并转发、删除等 操作后，清空缓存 */
public class ChatMsgCache {
  private static final Map<String, ChatMessageBean> msgMap = new HashMap<>();

  public static void addMessage(ChatMessageBean message) {
    msgMap.put(message.getMessageData().getMessage().getUuid(), message);
  }

  public static void removeMessage(String uuid) {
    msgMap.remove(uuid);
  }

  public static void removeMessages(List<ChatMessageBean> messages) {
    if (messages == null) {
      return;
    }
    for (ChatMessageBean message : messages) {
      msgMap.remove(message.getMessageData().getMessage().getUuid());
    }
  }

  public static boolean contains(String uuid) {
    return msgMap.containsKey(uuid);
  }

  public static void clear() {
    msgMap.clear();
  }

  public static List<ChatMessageBean> getMessageList() {
    List<ChatMessageBean> list = new ArrayList<>(msgMap.values());
    Collections.sort(
        list,
        (o1, o2) -> {
          if (o1 == null || o2 == null) {
            return 0;
          }
          return (int)
              (o1.getMessageData().getMessage().getTime()
                  - o2.getMessageData().getMessage().getTime());
        });
    return list;
  }

  public static int getMessageCount() {
    return msgMap.size();
  }

  public static List<IMMessageInfo> getMessageInfoList() {
    List<IMMessageInfo> list = new ArrayList<>();
    for (ChatMessageBean bean : msgMap.values()) {
      list.add(bean.getMessageData());
    }
    return ChatUtils.sortMsgByTime(list);
  }
}
