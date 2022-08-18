// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.model;

public class QChatTitleBean extends QChatBaseBean {

  public String title;
  public int titleRes;

  public QChatTitleBean(String title) {
    this.title = title;
    this.viewType = QChatViewType.TITLE_VIEW_TYPE;
  }

  public QChatTitleBean(int titleRes) {
    this.titleRes = titleRes;
    this.viewType = QChatViewType.TITLE_VIEW_TYPE;
  }
}
