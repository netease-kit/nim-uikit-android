package com.netease.nim.uikit.business.recent.holder;

import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;

public class SuperTeamRecentViewHolder extends TeamRecentViewHolder {

    public SuperTeamRecentViewHolder(BaseQuickAdapter adapter) {
        super(adapter);
    }

    @Override
    public String getTeamUserDisplayName(String tid, String account) {
        return TeamHelper.getSuperTeamMemberDisplayName(tid, account);
    }
}
