package com.netease.nim.uikit.business.contact.core.provider;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.contact.core.model.IContact;
import com.netease.nim.uikit.business.contact.core.provider.ContactSearch.HitInfo.Type;
import com.netease.nim.uikit.business.contact.core.query.TextQuery;
import com.netease.nim.uikit.business.contact.core.query.TextSearcher;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;

public class ContactSearch {
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

    /**
     * 判断是否击中
     */

    static boolean hitUser(UserInfo contact, TextQuery query) {
        String account = contact.getAccount();
        String name = UserInfoHelper.getUserName(account);

        return TextSearcher.contains(query.t9, name, query.text) || TextSearcher.contains(query.t9, account, query.text);
    }

    static boolean hitFriend(UserInfo contact, TextQuery query) {
        String account = contact.getAccount();
        String alias = NimUIKit.getContactProvider().getAlias(account);

        return TextSearcher.contains(query.t9, account, query.text) || TextSearcher.contains(query.t9, alias, query.text);
    }

    static boolean hitTeam(Team contact, TextQuery query) {
        String name = contact.getName();
        String teamId = contact.getId();

        return TextSearcher.contains(query.t9, name, query.text) || TextSearcher.contains(query.t9, teamId, query.text);
    }

    public static final boolean hitTeamMember(TeamMember teamMember, TextQuery query) {
        String name = TeamHelper.getTeamMemberDisplayName(teamMember.getTid(), teamMember.getAccount());

        return TextSearcher.contains(query.t9, name, query.text);
    }

    /**
     * 返回击中信息（可进行击中文本高亮显示）
     */

    public static final HitInfo hitInfo(IContact contact, TextQuery query) {
        if (contact.getContactType() == IContact.Type.Friend) {
            return hitInfoFriend(contact, query);
        } else if (contact.getContactType() == IContact.Type.Team) {
            return hitInfoTeamContact(contact, query);
        }

        return hitInfoContact(contact, query);
    }

    public static final HitInfo hitInfoFriend(IContact contact, TextQuery query) {
        String name = contact.getDisplayName();
        String account = contact.getContactId();

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
}