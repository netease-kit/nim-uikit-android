// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.model;

import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerMemberInfo;

public class QChatServerMemberBean extends QChatBaseBean {

  public QChatServerMemberInfo serverMember;

  public QChatServerMemberBean(QChatServerMemberInfo member) {
    this.serverMember = member;
    this.viewType = QChatViewType.SERVER_MEMBER_VIEW_TYPE;
  }
}
