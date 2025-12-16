// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import com.netease.yunxin.kit.chatkit.model.TeamSearchInfo;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.contactkit.ui.ContactConstant;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import java.util.Objects;

public class SearchTeamBean extends BaseBean {
  private static final String TAG = "SearchTeamBean";
  public TeamSearchInfo teamSearchInfo;

  public SearchTeamBean(TeamSearchInfo searchInfo, String router) {
    this.teamSearchInfo = searchInfo;
    this.viewType = ContactConstant.SearchViewType.TEAM;
    this.router = router;
    this.paramKey = RouterConstant.CHAT_KRY;
    this.param = searchInfo.getTeam();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SearchTeamBean)) return false;
    SearchTeamBean that = (SearchTeamBean) o;
    return Objects.equals(
        teamSearchInfo.getTeam().getTeamId(), that.teamSearchInfo.getTeam().getTeamId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(teamSearchInfo.getTeam().getTeamId());
  }
}
