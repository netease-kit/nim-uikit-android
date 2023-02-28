// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.model;

public class QChatMoreBean extends QChatBaseBean {
  public String title;
  public int titleRes;
  public String extend;

  public QChatMoreBean(String title) {
    this.title = title;
    this.viewType = QChatViewType.MORE_VIEW_TYPE;
  }

  public QChatMoreBean(int titleRes) {
    this.titleRes = titleRes;
    this.viewType = QChatViewType.MORE_VIEW_TYPE;
  }
}
