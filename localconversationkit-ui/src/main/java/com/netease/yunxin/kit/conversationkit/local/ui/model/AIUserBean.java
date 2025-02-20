// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.model;

import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;

public class AIUserBean extends BaseBean {

  protected V2NIMAIUser aiUser;

  public AIUserBean(V2NIMAIUser aiUser) {
    this.aiUser = aiUser;
  }

  public AIUserBean(V2NIMAIUser aiUser, String router, String routerParamKey, String routerParam) {
    this.aiUser = aiUser;
    this.router = router;
    this.paramKey = routerParamKey;
    this.param = routerParam;
  }

  public V2NIMAIUser getAiUser() {
    return aiUser;
  }

  public String getAccountId() {
    return aiUser.getAccountId();
  }

  public String getName() {
    return aiUser.getName();
  }

  public String getAvatar() {
    return aiUser.getAvatar();
  }
}
