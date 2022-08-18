// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.model;

import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelRoleInfo;

public class QChatChannelRoleBean extends QChatBaseBean {

  public QChatChannelRoleInfo channelRole;
  public float topRadius;
  public float bottomRadius;

  public QChatChannelRoleBean(QChatChannelRoleInfo roleInfo) {
    this(roleInfo, 0, 0);
  }

  public QChatChannelRoleBean(QChatChannelRoleInfo roleInfo, float topRadius, float bottomRadius) {
    this.channelRole = roleInfo;
    this.topRadius = topRadius;
    this.bottomRadius = bottomRadius;
    this.viewType = QChatViewType.CHANNEL_ROLE_VIEW_TYPE;
  }
}
