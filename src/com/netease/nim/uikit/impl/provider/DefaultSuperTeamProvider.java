package com.netease.nim.uikit.impl.provider;

import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.superteam.SuperTeamProvider;
import com.netease.nim.uikit.impl.cache.SuperTeamDataCache;
import com.netease.nimlib.sdk.superteam.SuperTeam;
import com.netease.nimlib.sdk.superteam.SuperTeamMember;

import java.util.List;

/**
 * Created by hzchenkang on 2017/11/1.
 */

public class DefaultSuperTeamProvider implements SuperTeamProvider {

    @Override
    public void fetchTeamById(String teamId, SimpleCallback<SuperTeam> callback) {
        SuperTeamDataCache.getInstance().fetchTeamById(teamId, callback);
    }

    @Override
    public SuperTeam getTeamById(String teamId) {
        return SuperTeamDataCache.getInstance().getTeamById(teamId);
    }

    @Override
    public List<SuperTeam> getAllTeams() {
        return SuperTeamDataCache.getInstance().getAllTeams();
    }

    @Override
    public void fetchTeamMemberList(String teamId, SimpleCallback<List<SuperTeamMember>> callback) {
        SuperTeamDataCache.getInstance().fetchTeamMemberList(teamId, callback);
    }

    @Override
    public void fetchTeamMember(String teamId, String account, SimpleCallback<SuperTeamMember> callback) {
        SuperTeamDataCache.getInstance().fetchTeamMember(teamId, account, callback);
    }

    @Override
    public List<SuperTeamMember> getTeamMemberList(String teamId) {
        return SuperTeamDataCache.getInstance().getTeamMemberList(teamId);
    }

    @Override
    public SuperTeamMember getTeamMember(String teamId, String account) {
        return SuperTeamDataCache.getInstance().getTeamMember(teamId, account);
    }
}
