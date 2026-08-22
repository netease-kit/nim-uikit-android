// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.model;

public class ConversationGroupLocalConfig {

  private String groupId;
  private ConversationGroupType type;
  private boolean visible;
  private int sortOrder;

  public ConversationGroupLocalConfig(
      String groupId, ConversationGroupType type, boolean visible, int sortOrder) {
    this.groupId = groupId;
    this.type = type;
    this.visible = visible;
    this.sortOrder = sortOrder;
  }

  public String getGroupId() {
    return groupId;
  }

  public ConversationGroupType getType() {
    return type;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }
}
