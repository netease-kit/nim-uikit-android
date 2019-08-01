package com.netease.nim.uikit.api.model.team;

import android.content.Context;
import android.os.Handler;

import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.ArrayList;
import java.util.List;

/**
 * 群、群成员变更通知接口
 */

public class TeamChangedObservable {

    private List<TeamDataChangedObserver> teamObservers = new ArrayList<>();
    private List<TeamMemberDataChangedObserver> memberObservers = new ArrayList<>();

    private Handler uiHandler;

    public TeamChangedObservable(Context context) {
        uiHandler = new Handler(context.getMainLooper());
    }

    public synchronized void registerTeamDataChangedObserver(TeamDataChangedObserver o, boolean register) {
        if (register) {
            if (teamObservers.contains(o)) {
                return;
            }
            teamObservers.add(o);
        } else {
            teamObservers.remove(o);
        }
    }

    public synchronized void registerTeamMemberDataChangedObserver(TeamMemberDataChangedObserver o, boolean register) {
        if (register) {
            if (memberObservers.contains(o)) {
                return;
            }
            memberObservers.add(o);
        } else {
            memberObservers.remove(o);
        }
    }

    public synchronized void notifyTeamDataUpdate(final List<Team> teams) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (TeamDataChangedObserver o : teamObservers) {
                    o.onUpdateTeams(teams);
                }
            }
        });
    }

    public synchronized void notifyTeamDataRemove(final Team team) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (TeamDataChangedObserver o : teamObservers) {
                    o.onRemoveTeam(team);
                }
            }
        });

    }

    public synchronized void notifyTeamMemberDataUpdate(final List<TeamMember> members) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (TeamMemberDataChangedObserver o : memberObservers) {
                    o.onUpdateTeamMember(members);
                }
            }
        });
    }

    public synchronized void notifyTeamMemberRemove(final List<TeamMember> members) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (TeamMemberDataChangedObserver o : memberObservers) {
                    o.onRemoveTeamMember(members);
                }
            }
        });
    }
}
