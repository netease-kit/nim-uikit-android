// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.model;

import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelMember;

public class QChatChannelMemberBean extends QChatBaseBean {

  public QChatChannelMember channelMember;
  public float topRadius;
  public float bottomRadius;

  public QChatChannelMemberBean(QChatChannelMember member) {
    this.channelMember = member;
    this.viewType = QChatViewType.CHANNEL_MEMBER_VIEW_TYPE;
  }

  public QChatChannelMemberBean(QChatChannelMember member, float topRadius, float bottomRadius) {
    this.channelMember = member;
    this.topRadius = topRadius;
    this.bottomRadius = bottomRadius;
    this.viewType = QChatViewType.CHANNEL_MEMBER_VIEW_TYPE;
  }
}
