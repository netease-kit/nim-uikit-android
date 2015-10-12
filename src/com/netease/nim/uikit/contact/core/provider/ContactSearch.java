package com.netease.nim.uikit.contact.core.provider;

import com.netease.nim.uikit.contact.core.model.IContact;
import com.netease.nim.uikit.contact.core.provider.ContactSearch.HitInfo.Type;
import com.netease.nim.uikit.contact.core.query.TextQuery;
import com.netease.nim.uikit.contact.core.query.TextSearcher;
import com.netease.nim.uikit.team.TeamDataCache;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;

public class ContactSearch {
    public static final boolean hitUser(UserInfoProvider.UserInfo contact, TextQuery query) {
        String name = contact.getName();

        return TextSearcher.contains(query.t9, name, query.text);
    }

    public static final boolean hitTeam(Team contact, TextQuery query) {
        String name = contact.getName();
        String id = contact.getId();

        return TextSearcher.contains(query.t9, name, query.text) || TextSearcher.contains(query.t9, id, query.text);
    }

    public static final boolean hitTeamMember(TeamMember teamMember, TextQuery query) {
        String name = TeamDataCache.getInstance().getTeamMemberDisplayName(teamMember.getTid(), teamMember.getAccount());

        return TextSearcher.contains(query.t9, name, query.text);
    }

    public static final class HitInfo {
        public enum Type {
            Account, Name,
        }

        public final Type type;

        public final String text;

        public final int[] range;

        public HitInfo(Type type, String text, int[] range) {
            this.type = type;
            this.text = text;
            this.range = range;
        }
    }

    public static final HitInfo hitInfoFriend(IContact contact, TextQuery query) {
        String account = contact.getContactId();
        String name = contact.getDisplayName();

        int[] range = TextSearcher.indexOf(query.t9, name, query.text);

        if (range != null) {
            return new HitInfo(Type.Name, name, range);
        }

        range = TextSearcher.indexOf(query.t9, account, query.text);

        if (range != null) {
            return new HitInfo(Type.Account, account, range);
        }

        return null;
    }

    public static final HitInfo hitInfoTeamContact(IContact contact, TextQuery query) {
        String name = contact.getDisplayName();

        int[] range = TextSearcher.indexOf(query.t9, name, query.text);

        if (range != null) {
            return new HitInfo(Type.Name, name, range);
        }

        return null;
    }

    public static final HitInfo hitInfoContact(IContact contact, TextQuery query) {
        String name = contact.getDisplayName();

        int[] range = TextSearcher.indexOf(query.t9, name, query.text);

        if (range != null) {
            return new HitInfo(Type.Name, name, range);
        }

        return null;
    }

    public static final HitInfo hitInfo(IContact contact, TextQuery query) {
        if (contact.getContactType() == IContact.Type.Friend) {
            return hitInfoFriend(contact, query);
        } else if (contact.getContactType() == IContact.Type.Team) {
            return hitInfoTeamContact(contact, query);
        }

        return hitInfoContact(contact, query);
    }
}