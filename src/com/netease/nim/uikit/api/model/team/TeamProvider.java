package com.netease.nim.uikit.api.model.team;

import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.List;

/**
 * 群、群成员信息提供者
 */

public interface TeamProvider {
    /**
     * 根据teamId 异步获取群信息
     *
     * @param teamId   群id
     * @param callback 回调
     */
    void fetchTeamById(String teamId, SimpleCallback<Team> callback);

    /**
     * 根据teamId 同步获取群信息
     *
     * @param teamId 群id
     * @return 群信息Team
     */
    Team getTeamById(String teamId);

    /**
     * 获取当前账号所有的群
     *
     * @return 群列表
     */
    List<Team> getAllTeams();

    /**
     * 获取当前账号所有的指定类型的群
     *
     * @return 群列表
     */
    List<Team> getAllTeamsByType(TeamTypeEnum teamTypeEnum);

    /**
     * 根据群id异步获取当前群所有的群成员信息
     *
     * @param teamId   群id
     * @param callback 回调
     */
    void fetchTeamMemberList(String teamId, SimpleCallback<List<TeamMember>> callback);

    /**
     * 根据群id、账号（异步）查询群成员资料
     */
    void fetchTeamMember(String teamId, String account, SimpleCallback<TeamMember> callback);

    /**
     * 根据群id 同步获取当前群所有的群成员信息
     *
     * @param teamId 群id
     * @return 群成员信息列表
     */
    List<TeamMember> getTeamMemberList(String teamId);

    /**
     * 获取群成员资料
     *
     * @param teamId  群id
     * @param account 成员账号
     * @return TeamMember
     */
    TeamMember getTeamMember(String teamId, String account);
}
