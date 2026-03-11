// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model;

import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import java.util.Collections;
import java.util.List;

public class MessageGroup {
  private String groupKey;
  private String displayText;
  private List<V2NIMMessage> messageList;

  public MessageGroup(String date, List<V2NIMMessage> messages) {
    this.groupKey = date;
    this.displayText = date;
    this.messageList = messages;
  }

  public MessageGroup(String groupKey, String displayText, List<V2NIMMessage> messages) {
    this.groupKey = groupKey;
    this.displayText = displayText;
    this.messageList = messages;
  }

  public String getGroupKey() {
    return groupKey;
  }

  public String getDisplayText() {
    return displayText;
  }

  public List<V2NIMMessage> getMessageList() {
    return messageList;
  }

  public void addMessageList(List<V2NIMMessage> messages) {
    this.messageList.addAll(messages);
    // 合并后按时间正序重新排序（旧→新），保证分页加载后组内顺序一致
    Collections.sort(
        this.messageList, (o1, o2) -> Long.compare(o1.getCreateTime(), o2.getCreateTime()));
  }
}
