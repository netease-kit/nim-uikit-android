package com.netease.nim.uikit.impl.provider;

import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.team.TeamProvider;
import com.netease.nim.uikit.impl.cache.TeamDataCache;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.List;

/**
 * Created by hzchenkang on 2017/11/1.
 */

public class DefaultTeamProvider implements TeamProvider {
    @Override
    public void fetchTeamById(String teamId, SimpleCallback<Team> callback) {
        TeamDataCache.getInstance().fetchTeamById(teamId, callback);
    }

    @Override
    public Team getTeamById(String teamId) {
        return TeamDataCache.getInstance().getTeamById(teamId);
    }

    @Override
    public List<Team> getAllTeams() {
        return TeamDataCache.getInstance().getAllTeams();
    }

    @Override
    public List<Team> getAllTeamsByType(TeamTypeEnum teamTypeEnum) {
        if (teamTypeEnum == TeamTypeEnum.Advanced) {
            return TeamDataCache.getInstance().getAllAdvancedTeams();
        } else if (teamTypeEnum == TeamTypeEnum.Normal) {
            return TeamDataCache.getInstance().getAllNormalTeams();
        } else {
            return null;
        }
    }

    @Override
    public void fetchTeamMemberList(String teamId, SimpleCallback<List<TeamMember>> callback) {
        TeamDataCache.getInstance().fetchTeamMemberList(teamId, callback);
    }

    @Override
    public void fetchTeamMember(String teamId, String account, SimpleCallback<TeamMember> callback) {
        TeamDataCache.getInstance().fetchTeamMember(teamId, account, callback);
    }

    @Override
    public List<TeamMember> getTeamMemberList(String teamId) {
        return TeamDataCache.getInstance().getTeamMemberList(teamId);
    }

    @Override
    public TeamMember getTeamMember(String teamId, String account) {
        return TeamDataCache.getInstance().getTeamMember(teamId, account);
    }
}
