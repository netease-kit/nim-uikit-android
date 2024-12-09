// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.model;

import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import java.util.ArrayList;
import java.util.List;

public class ConversationHeaderBean extends ConversationBean {

  private List<AIUserBean> userList = new ArrayList<>();

  public ConversationHeaderBean(List<AIUserBean> userBeans) {
    super(null, null, ConversationConstant.ViewType.HORIZON_VIEW);
    this.userList.addAll(userBeans);
  }

  public void addAIUser(List<AIUserBean> userBeans) {
    this.userList.addAll(userBeans);
  }

  public List<AIUserBean> getUserList() {
    return this.userList;
  }

  public void clear() {
    this.userList.clear();
  }
}
