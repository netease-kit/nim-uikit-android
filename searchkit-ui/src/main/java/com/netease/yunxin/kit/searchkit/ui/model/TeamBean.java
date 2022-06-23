/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.searchkit.ui.model;

import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.searchkit.model.TeamSearchInfo;
import com.netease.yunxin.kit.searchkit.ui.commone.SearchConstant;

import java.util.Objects;

public class TeamBean extends BaseBean {
    private static final String TAG = "SearchTeamBean";
    public TeamSearchInfo teamSearchInfo;

    public TeamBean(TeamSearchInfo searchInfo){
        this.teamSearchInfo = searchInfo;
        this.viewType = SearchConstant.ViewType.TEAM;
        this.router = RouterConstant.PATH_CHAT_TEAM_PAGE;
        this.paramKey = RouterConstant.CHAT_KRY;
        this.param = searchInfo.getTeam();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamBean)) return false;
        TeamBean that = (TeamBean) o;
        return Objects.equals(teamSearchInfo.getTeam().getId(), that.teamSearchInfo.getTeam().getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamSearchInfo.getTeam().getId());
    }
}
