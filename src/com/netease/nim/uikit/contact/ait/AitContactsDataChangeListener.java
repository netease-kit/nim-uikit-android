package com.netease.nim.uikit.contact.ait;

import com.netease.nimlib.sdk.robot.model.NimRobotInfo;
import com.netease.nimlib.sdk.team.model.TeamMember;

/**
 * Created by hzchenkang on 2017/7/4.
 */

public interface AitContactsDataChangeListener {
    void onAitTeamMemberAdded(TeamMember member);

    void onAitRobotAdded(NimRobotInfo robotInfo, boolean force);
}
