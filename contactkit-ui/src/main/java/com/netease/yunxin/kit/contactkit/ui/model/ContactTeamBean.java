// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;

/** Contact data for team */
public class ContactTeamBean extends BaseContactBean {

  public V2NIMTeam data;

  public ContactTeamBean(V2NIMTeam data) {
    this.data = data;
    viewType = IViewTypeConstant.CONTACT_TEAM_LIST;
    this.router = RouterConstant.PATH_CHAT_TEAM_PAGE;
  }

  @Override
  public boolean isShowDivision() {
    return false;
  }

  @Override
  public String getTarget() {
    return null;
  }

  @Override
  public String getAccountId() {
    return data.getTeamId();
  }
}
