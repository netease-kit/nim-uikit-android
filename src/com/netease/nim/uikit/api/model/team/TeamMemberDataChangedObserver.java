package com.netease.nim.uikit.api.model.team;

import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.List;

/**
 * UIKit 与 app 群成员数据变更监听接口
 */

public interface TeamMemberDataChangedObserver {

    /**
     * 成员更新
     *
     * @param members 成员列表
     */
    void onUpdateTeamMember(List<TeamMember> members);

    /**
     * 成员删除
     *
     * @param members 成员列表
     */
    void onRemoveTeamMember(List<TeamMember> members);
}
