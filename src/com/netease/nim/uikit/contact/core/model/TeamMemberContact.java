package com.netease.nim.uikit.contact.core.model;

import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nimlib.sdk.team.model.TeamMember;

/**
 * Created by huangjun on 2015/5/5.
 */
public class TeamMemberContact extends AbsContact {

    private TeamMember teamMember;

    public TeamMemberContact(TeamMember teamMember) {
        this.teamMember = teamMember;
    }

    @Override
    public String getContactId() {
        return teamMember.getAccount();
    }

    @Override
    public int getContactType() {
        return Type.TeamMember;
    }

    @Override
    public String getDisplayName() {
        return TeamDataCache.getInstance().getTeamMemberDisplayName(teamMember.getTid(), teamMember.getAccount());
    }
}
