package com.netease.nim.uikit.api.model.superteam;

import com.netease.nimlib.sdk.superteam.SuperTeamMember;

import java.util.List;

/**
 * UIKit 与 app 群成员数据变更监听接口
 */

public interface SuperTeamMemberDataChangedObserver {

    /**
     * 成员更新
     *
     * @param members 成员列表
     */
    void onUpdateTeamMember(List<SuperTeamMember> members);

    /**
     * 成员删除
     *
     * @param members 成员列表
     */
    void onRemoveTeamMember(List<SuperTeamMember> members);
}
