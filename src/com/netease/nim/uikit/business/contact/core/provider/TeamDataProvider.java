package com.netease.nim.uikit.business.contact.core.provider;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.team.TeamProvider;
import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.item.ContactItem;
import com.netease.nim.uikit.business.contact.core.item.ItemTypes;
import com.netease.nim.uikit.business.contact.core.model.ContactGroupStrategy;
import com.netease.nim.uikit.business.contact.core.model.TeamContact;
import com.netease.nim.uikit.business.contact.core.query.TextComparator;
import com.netease.nim.uikit.business.contact.core.query.TextQuery;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * 群数据源提供者
 * <p/>
 * Created by huangjun on 2015/3/1.
 */
public class TeamDataProvider {
    public static final List<AbsContactItem> provide(TextQuery query, int itemType) {
        List<TeamContact> sources = query(query, itemType);
        List<AbsContactItem> items = new ArrayList<>(sources.size());
        for (TeamContact t : sources) {
            items.add(createTeamItem(t));
        }

        return items;
    }

    private static AbsContactItem createTeamItem(TeamContact team) {
        return new ContactItem(team, ItemTypes.TEAM) {
            @Override
            public int compareTo(ContactItem item) {
                return compareTeam((TeamContact) getContact(), (TeamContact) (item.getContact()));
            }

            @Override
            public String belongsGroup() {
                return ContactGroupStrategy.GROUP_TEAM;
            }
        };
    }

    private static int compareTeam(TeamContact lhs, TeamContact rhs) {
        return TextComparator.compareIgnoreCase(lhs.getDisplayName(), rhs.getDisplayName());
    }

    /**
     * * 数据查询
     */
    private static final List<TeamContact> query(TextQuery query, int itemType) {
        List<Team> teams;
        TeamProvider provider = NimUIKit.getTeamProvider();
        if (itemType == ItemTypes.TEAMS.ADVANCED_TEAM) {
            teams = provider.getAllTeamsByType(TeamTypeEnum.Advanced);
        } else if (itemType == ItemTypes.TEAMS.NORMAL_TEAM) {
            teams = provider.getAllTeamsByType(TeamTypeEnum.Normal);
        } else {
            teams = provider.getAllTeams();
        }

        List<TeamContact> contacts = new ArrayList<>();
        for (Team t : teams) {
            if (query == null || ContactSearch.hitTeam(t, query)) {
                contacts.add(new TeamContact(t));
            }
        }

        return contacts;
    }
}
