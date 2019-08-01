package com.netease.nim.uikit.business.contact.core.model;

import android.text.TextUtils;

import com.netease.nimlib.sdk.team.model.Team;

public class TeamContact extends AbsContact {

    private Team team;

    public TeamContact(Team team) {
        this.team = team;
    }

    @Override
    public String getContactId() {
        return team == null ? "" : team.getId();
    }

    @Override
    public int getContactType() {
        return IContact.Type.Team;
    }

    @Override
    public String getDisplayName() {
        String name = team.getName();

        return TextUtils.isEmpty(name) ? team.getId() : name;
    }

    public Team getTeam() {
        return team;
    }
}
