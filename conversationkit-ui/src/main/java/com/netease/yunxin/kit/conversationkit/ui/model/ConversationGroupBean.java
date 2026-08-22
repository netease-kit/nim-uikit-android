// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.model;

import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversationGroup;

public class ConversationGroupBean {

  public static final String ID_ALL = "ui_all";
  public static final String ID_AIT_ME = "ui_ait_me";
  public static final String ID_UNREAD = "ui_unread";

  private final String id;
  private final ConversationGroupType type;
  private String name;
  private boolean visible;
  private int sortOrder;
  private int unreadOrCount;
  private V2NIMConversationGroup sdkGroup;
  private boolean saving;

  public ConversationGroupBean(
      String id, ConversationGroupType type, String name, boolean visible, int sortOrder) {
    this.id = id;
    this.type = type;
    this.name = name;
    this.visible = visible;
    this.sortOrder = sortOrder;
  }

  public static ConversationGroupBean all(String name) {
    return new ConversationGroupBean(ID_ALL, ConversationGroupType.ALL, name, true, 0);
  }

  public static ConversationGroupBean aitMe(String name, boolean visible, int sortOrder) {
    return new ConversationGroupBean(
        ID_AIT_ME, ConversationGroupType.AIT_ME, name, visible, sortOrder);
  }

  public static ConversationGroupBean unread(String name, boolean visible, int sortOrder) {
    return new ConversationGroupBean(
        ID_UNREAD, ConversationGroupType.UNREAD, name, visible, sortOrder);
  }

  public static ConversationGroupBean custom(
      V2NIMConversationGroup group, boolean visible, int sortOrder) {
    ConversationGroupBean bean =
        new ConversationGroupBean(
            group.getGroupId(), ConversationGroupType.CUSTOM, group.getName(), visible, sortOrder);
    bean.setSdkGroup(group);
    return bean;
  }

  public String getId() {
    return id;
  }

  public ConversationGroupType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public int getUnreadOrCount() {
    return unreadOrCount;
  }

  public void setUnreadOrCount(int unreadOrCount) {
    this.unreadOrCount = unreadOrCount;
  }

  public V2NIMConversationGroup getSdkGroup() {
    return sdkGroup;
  }

  public void setSdkGroup(V2NIMConversationGroup sdkGroup) {
    this.sdkGroup = sdkGroup;
  }

  public boolean isSaving() {
    return saving;
  }

  public void setSaving(boolean saving) {
    this.saving = saving;
  }

  public boolean isDefaultGroup() {
    return type == ConversationGroupType.ALL
        || type == ConversationGroupType.AIT_ME
        || type == ConversationGroupType.UNREAD;
  }

  public boolean canOpenSetting() {
    return type == ConversationGroupType.CUSTOM;
  }

  public ConversationGroupBean copy() {
    ConversationGroupBean bean = new ConversationGroupBean(id, type, name, visible, sortOrder);
    bean.setUnreadOrCount(unreadOrCount);
    bean.setSdkGroup(sdkGroup);
    bean.setSaving(saving);
    return bean;
  }
}
