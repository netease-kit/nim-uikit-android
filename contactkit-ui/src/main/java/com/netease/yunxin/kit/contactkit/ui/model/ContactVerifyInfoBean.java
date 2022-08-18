// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfo;

/** Contact data for verify data */
public class ContactVerifyInfoBean extends BaseContactBean {

  public SystemMessageInfo data;

  public ContactVerifyInfoBean(SystemMessageInfo data) {
    this.data = data;
    viewType = IViewTypeConstant.CONTACT_VERIFY_INFO;
  }

  @Override
  public boolean isShowDivision() {
    return false;
  }

  @Override
  public String getTarget() {
    return null;
  }
}
