package com.netease.nim.uikit.api.model.team;

import com.netease.nimlib.sdk.team.model.Team;

import java.util.List;

/**
 * UIKit 与 app 群数据变更监听接口
 */

public interface TeamDataChangedObserver {

    /**
     * 群更新
     *
     * @param teams 群列表
     */
    void onUpdateTeams(List<Team> teams);

    /**
     * 群删除
     *
     * @param team) 群
     */
    void onRemoveTeam(Team team);
}
