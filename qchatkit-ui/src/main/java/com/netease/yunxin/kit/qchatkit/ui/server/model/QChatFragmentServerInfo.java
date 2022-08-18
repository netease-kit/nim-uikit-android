// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server.model;

import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatUnreadInfoItem;
import java.util.Map;
import java.util.Objects;

/** This is the combination of server info and unread info. */
public class QChatFragmentServerInfo {
  public QChatServerInfo serverInfo;

  public Map<Long, QChatUnreadInfoItem> unreadInfoItemMap;

  public QChatFragmentServerInfo(QChatServerInfo serverInfo) {
    this.serverInfo = serverInfo;
  }

  public static QChatFragmentServerInfo generateWithServerId(long serverId) {
    return new QChatFragmentServerInfo(new QChatServerInfo(serverId));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    QChatFragmentServerInfo that = (QChatFragmentServerInfo) o;

    return serverInfo != null
        ? serverInfo.getServerId() == that.serverInfo.getServerId()
        : that.serverInfo == null;
  }

  @Override
  public int hashCode() {
    return serverInfo != null ? Objects.hashCode(serverInfo.getServerId()) : 0;
  }

  @Override
  public String toString() {
    return "QChatFragmentServerInfo{"
        + "serverInfo="
        + serverInfo
        + ", unreadInfoItemMap="
        + unreadInfoItemMap
        + '}';
  }
}
