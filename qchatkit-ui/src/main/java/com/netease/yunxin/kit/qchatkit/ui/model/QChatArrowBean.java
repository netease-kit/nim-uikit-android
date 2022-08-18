// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.model;

public class QChatArrowBean extends QChatBaseBean {

  public String title;
  public float topRadius;
  public float bottomRadius;
  public Object param;

  public QChatArrowBean(String title, int topRadius, int bottomRadius) {
    this.title = title;
    this.topRadius = topRadius;
    this.bottomRadius = bottomRadius;
    this.viewType = QChatViewType.ARROW_VIEW_TYPE;
  }

  public QChatArrowBean(String title, int topRadius, int bottomRadius, Object param) {
    this.title = title;
    this.topRadius = topRadius;
    this.bottomRadius = bottomRadius;
    this.viewType = QChatViewType.ARROW_VIEW_TYPE;
  }
}
