package com.netease.nim.uikit.business.contact.core.provider;

import android.text.TextUtils;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.item.ContactItem;
import com.netease.nim.uikit.business.contact.core.item.ItemTypes;
import com.netease.nim.uikit.business.contact.core.model.ContactGroupStrategy;
import com.netease.nim.uikit.business.contact.core.model.IContact;
import com.netease.nim.uikit.business.contact.core.model.TeamMemberContact;
import com.netease.nim.uikit.business.contact.core.query.TextComparator;
import com.netease.nim.uikit.business.contact.core.query.TextQuery;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.ArrayList;
import java.util.List;

/**
 * 群成员数据源提供者
 * <p/>
 * Created by huangjun on 2015/5/4.
 */
public class TeamMemberDataProvider {
    public static final List<AbsContactItem> provide(TextQuery query, String tid) {
        List<TeamMemberContact> sources = query(query, tid);
        List<AbsContactItem> items = new ArrayList<>(sources.size());
        for (TeamMemberContact t : sources) {
            items.add(createTeamMemberItem(t));
        }

        return items;
    }

    private static AbsContactItem createTeamMemberItem(TeamMemberContact teamMember) {
        return new ContactItem(teamMember, ItemTypes.TEAM_MEMBER) {
            @Override
            public int compareTo(ContactItem item) {
                return compareTeamMember((TeamMemberContact) getContact(), (TeamMemberContact) (item.getContact()));
            }

            @Override
            public String belongsGroup() {
                String group = TextComparator.getLeadingUp(getCompare());
                return !TextUtils.isEmpty(group) ? group : ContactGroupStrategy.GROUP_TEAM;
            }

            private String getCompare() {
                IContact contact = getContact();
                return contact != null ? contact.getDisplayName() : null;
            }
        };
    }

    private static int compareTeamMember(TeamMemberContact lhs, TeamMemberContact rhs) {
        return TextComparator.compareIgnoreCase(lhs.getDisplayName(), rhs.getDisplayName());
    }

    /**
     * * 数据查询
     */
    private static final List<TeamMemberContact> query(TextQuery query, String tid) {
        List<TeamMember> teamMembers = NimUIKit.getTeamProvider().getTeamMemberList(tid);

        List<TeamMemberContact> contacts = new ArrayList<>();
        for (TeamMember t : teamMembers) {
            if (t != null && (query == null || ContactSearch.hitTeamMember(t, query))) {
                contacts.add(new TeamMemberContact(t));
            }
        }

        return contacts;
    }

    /**
     * 发起异步任务load群成员进入缓存
     *
     * @param tid
     * @param callback
     */
    public static void loadTeamMemberDataAsync(String tid, final LoadTeamMemberCallback callback) {
        NimUIKit.getTeamProvider().fetchTeamMemberList(tid, new SimpleCallback<List<TeamMember>>() {
            @Override
            public void onResult(boolean success, List<TeamMember> result, int code) {
                if (callback != null) {
                    callback.onResult(success);
                }
            }
        });
    }

    public interface LoadTeamMemberCallback {
        void onResult(boolean success);
    }
}
