// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import com.netease.nimlib.coexist.sdk.v2.ai.model.V2NIMAIUser;

/** AI数字人数据模块 */
public class AIUserInfoBean extends BaseContactBean {
  public V2NIMAIUser data;

  public boolean isSelected;

  public AIUserInfoBean(V2NIMAIUser user) {
    this.data = user;
    this.viewType = IViewTypeConstant.CONTACT_AI_USER;
  }

  public String getName() {
    return data.getName() != null ? data.getName() : data.getAccountId();
  }

  public String getAvatar() {
    return data.getAvatar();
  }

  public String getAccountId() {
    return data.getAccountId();
  }

  @Override
  public boolean isShowDivision() {
    return false;
  }

  @Override
  public String getTarget() {
    return data.getName();
  }

  @Override
  public int hashCode() {
    return data.getAccountId().hashCode();
  }
}
