package com.netease.nim.uikit.api.model.superteam;

import android.content.Context;
import android.os.Handler;

import com.netease.nimlib.sdk.superteam.SuperTeam;
import com.netease.nimlib.sdk.superteam.SuperTeamMember;

import java.util.ArrayList;
import java.util.List;

/**
 * 群、群成员变更通知接口
 */

public class SuperTeamChangedObservable {

    private List<SuperTeamDataChangedObserver> teamObservers = new ArrayList<>();

    private List<SuperTeamMemberDataChangedObserver> memberObservers = new ArrayList<>();

    private Handler uiHandler;

    public SuperTeamChangedObservable(Context context) {
        uiHandler = new Handler(context.getMainLooper());
    }

    public synchronized void registerTeamDataChangedObserver(SuperTeamDataChangedObserver o, boolean register) {
        if (register) {
            if (teamObservers.contains(o)) {
                return;
            }
            teamObservers.add(o);
        } else {
            teamObservers.remove(o);
        }
    }

    public synchronized void registerTeamMemberDataChangedObserver(SuperTeamMemberDataChangedObserver o,
                                                                   boolean register) {
        if (register) {
            if (memberObservers.contains(o)) {
                return;
            }
            memberObservers.add(o);
        } else {
            memberObservers.remove(o);
        }
    }

    public synchronized void notifyTeamDataUpdate(final List<SuperTeam> teams) {
        uiHandler.post(new Runnable() {

            @Override
            public void run() {
                for (SuperTeamDataChangedObserver o : teamObservers) {
                    o.onUpdateTeams(teams);
                }
            }
        });
    }

    public synchronized void notifyTeamDataRemove(final SuperTeam team) {
        uiHandler.post(new Runnable() {

            @Override
            public void run() {
                for (SuperTeamDataChangedObserver o : teamObservers) {
                    o.onRemoveTeam(team);
                }
            }
        });

    }

    public synchronized void notifyTeamMemberDataUpdate(final List<SuperTeamMember> members) {
        uiHandler.post(new Runnable() {

            @Override
            public void run() {
                for (SuperTeamMemberDataChangedObserver o : memberObservers) {
                    o.onUpdateTeamMember(members);
                }
            }
        });
    }

    public synchronized void notifyTeamMemberRemove(final List<SuperTeamMember> members) {
        uiHandler.post(new Runnable() {

            @Override
            public void run() {
                for (SuperTeamMemberDataChangedObserver o : memberObservers) {
                    o.onRemoveTeamMember(members);
                }
            }
        });
    }
}
