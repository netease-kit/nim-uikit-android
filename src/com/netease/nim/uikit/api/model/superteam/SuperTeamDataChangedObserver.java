package com.netease.nim.uikit.api.model.superteam;

import com.netease.nimlib.sdk.superteam.SuperTeam;

import java.util.List;

/**
 * UIKit 与 app 群数据变更监听接口
 */

public interface SuperTeamDataChangedObserver {

    /**
     * 群更新
     *
     * @param teams 群列表
     */
    void onUpdateTeams(List<SuperTeam> teams);

    /**
     * 群删除
     *
     * @param team) 群
     */
    void onRemoveTeam(SuperTeam team);
}
