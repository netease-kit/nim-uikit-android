// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model;

import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.topic.V2NIMTopic;

public class BotSubSessionItem {

  private final V2NIMTopic topic;
  private final String summary;
  private final long time;
  private final boolean unread;

  public BotSubSessionItem(V2NIMTopic topic, @Nullable String summary, long time, boolean unread) {
    this.topic = topic;
    this.summary = summary;
    this.time = time;
    this.unread = unread;
  }

  public V2NIMTopic getTopic() {
    return topic;
  }

  public String getSummary() {
    return summary;
  }

  public long getTime() {
    return time;
  }

  public boolean hasUnread() {
    return unread;
  }
}
