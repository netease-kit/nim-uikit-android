package com.netease.nim.uikit.api.model.superteam;

import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nimlib.sdk.superteam.SuperTeam;
import com.netease.nimlib.sdk.superteam.SuperTeamMember;

import java.util.List;

/**
 * 群、群成员信息提供者
 */

public interface SuperTeamProvider {

    /**
     * 根据teamId 异步获取群信息
     *
     * @param teamId   群id
     * @param callback 回调
     */
    void fetchTeamById(String teamId, SimpleCallback<SuperTeam> callback);

    /**
     * 根据teamId 同步获取群信息
     *
     * @param teamId 群id
     * @return 群信息Team
     */
    SuperTeam getTeamById(String teamId);

    /**
     * 获取当前账号所有的群
     *
     * @return 群列表
     */
    List<SuperTeam> getAllTeams();

    /**
     * 根据群id异步获取当前群所有的群成员信息
     *
     * @param teamId   群id
     * @param callback 回调
     */
    void fetchTeamMemberList(String teamId, SimpleCallback<List<SuperTeamMember>> callback);

    /**
     * 根据群id、账号（异步）查询群成员资料
     */
    void fetchTeamMember(String teamId, String account, SimpleCallback<SuperTeamMember> callback);

    /**
     * 根据群id 同步获取当前群所有的群成员信息
     *
     * @param teamId 群id
     * @return 群成员信息列表
     */
    List<SuperTeamMember> getTeamMemberList(String teamId);

    /**
     * 获取群成员资料
     *
     * @param teamId  群id
     * @param account 成员账号
     * @return TeamMember
     */
    SuperTeamMember getTeamMember(String teamId, String account);
}
