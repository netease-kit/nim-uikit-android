package com.netease.nim.uikit.contact.ait;

import com.netease.nimlib.sdk.robot.model.NimRobotInfo;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hzchenkang on 2017/6/23.
 */

public class AitedContacts {

    private Map<String, TeamMember> selectedMembers = new HashMap<>();

    private Map<String, NimRobotInfo> selectedRobots = new HashMap<>();

    private AitContactsDataChangeListener aitContactsDataChangeListener;

    private AitedContacts() {
    }

    public void aitTeamMember(TeamMember member) {
        selectedMembers.put(member.getAccount(), member);
        if (aitContactsDataChangeListener != null) {
            aitContactsDataChangeListener.onAitTeamMemberAdded(member);
        }
    }

    public void aitRobot(NimRobotInfo robotInfo) {
        selectedRobots.put(robotInfo.getAccount(), robotInfo);
        if (aitContactsDataChangeListener != null) {
            aitContactsDataChangeListener.onAitRobotAdded(robotInfo, false);
        }
    }

    public void aitRobotForce(NimRobotInfo info) {
        if (info == null) {
            return;
        }
        selectedRobots.put(info.getAccount(), info);
        if (aitContactsDataChangeListener != null) {
            aitContactsDataChangeListener.onAitRobotAdded(info, true);
        }
    }

    public void clearAitContact() {
        selectedMembers.clear();
        selectedRobots.clear();
    }

    public Map<String, NimRobotInfo> getSelectedRobots() {
        return selectedRobots;
    }

    public Map<String, TeamMember> getSelectedMembers() {
        return selectedMembers;
    }

    public void setAitContactsDataChangeListener(AitContactsDataChangeListener listener) {
        this.aitContactsDataChangeListener = listener;
    }

    public void removeAitContactsDataChangeListener(AitContactsDataChangeListener listener) {
        if (aitContactsDataChangeListener == listener) {
            aitContactsDataChangeListener = null;
        }
    }

    // 单例
    public static AitedContacts getInstance() {
        return InstanceHolder.aitedContacts;
    }

    private static class InstanceHolder {
        private static AitedContacts aitedContacts = new AitedContacts();
    }

}
