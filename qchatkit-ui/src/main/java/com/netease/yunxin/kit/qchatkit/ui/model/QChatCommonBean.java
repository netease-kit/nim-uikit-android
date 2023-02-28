// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.model;

public class QChatCommonBean extends QChatBaseBean {

  public Object param;

  public QChatCommonBean(int type) {
    this.viewType = type;
  }

  public QChatCommonBean(int type, Object param) {
    this.viewType = type;
    this.viewType = QChatViewType.ARROW_VIEW_TYPE;
  }
}
