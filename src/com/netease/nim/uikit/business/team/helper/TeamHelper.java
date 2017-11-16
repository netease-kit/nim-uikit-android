package com.netease.nim.uikit.business.team.helper;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nimlib.sdk.team.constant.TeamBeInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.constant.TeamUpdateModeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hzxuwen on 2015/3/25.
 */
public class TeamHelper {
    public static VerifyTypeEnum getVerifyTypeEnum(String name) {
        VerifyTypeEnum type = null;

        if (name.equals(NimUIKit.getContext().getString(R.string.team_allow_anyone_join))) {
            type = VerifyTypeEnum.Free;
        } else if (name.equals(NimUIKit.getContext().getString(R.string.team_need_authentication))) {
            type = VerifyTypeEnum.Apply;
        } else if (name.equals(NimUIKit.getContext().getString(R.string.team_not_allow_anyone_join))) {
            type = VerifyTypeEnum.Private;
        }

        return type;
    }

    public static List<String> createAuthenMenuStrings() {
        int[] res = new int[]{R.string.team_allow_anyone_join, R.string.team_need_authentication,
                R.string.team_not_allow_anyone_join, R.string.cancel};
        List<String> btnNames = new ArrayList<>();
        for (int r : res) {
            btnNames.add(NimUIKit.getContext().getString(r));
        }
        return btnNames;
    }

    public static int getVerifyString(VerifyTypeEnum type) {
        if (type == VerifyTypeEnum.Free) {
            return R.string.team_allow_anyone_join;
        } else if (type == VerifyTypeEnum.Apply) {
            return R.string.team_need_authentication;
        } else {
            return R.string.team_not_allow_anyone_join;
        }
    }

    // 群消息提醒类型菜单项名称
    public static List<String> createNotifyMenuStrings() {
        int[] res = new int[]{
                R.string.team_notify_all,
                R.string.team_notify_manager,
                R.string.team_notify_mute,
                R.string.cancel
        };
        List<String> btnNames = new ArrayList<>();
        for (int r : res) {
            btnNames.add(NimUIKit.getContext().getString(r));
        }
        return btnNames;
    }

    // 获取提醒类型
    public static TeamMessageNotifyTypeEnum getNotifyType(String name) {
        TeamMessageNotifyTypeEnum type = null;
        if (name.equals(NimUIKit.getContext().getString(R.string.team_notify_all))) {
            type = TeamMessageNotifyTypeEnum.All;
        } else if (name.equals(NimUIKit.getContext().getString(R.string.team_notify_manager))) {
            type = TeamMessageNotifyTypeEnum.Manager;
        } else if (name.equals(NimUIKit.getContext().getString(R.string.team_notify_mute))) {
            type = TeamMessageNotifyTypeEnum.Mute;
        }
        return type;
    }

    // 邀请他人菜单项名称
    public static List<String> createInviteMenuStrings() {
        int[] res = new int[]{R.string.team_admin_invite, R.string.team_everyone_invite,
                R.string.cancel};
        List<String> btnNames = new ArrayList<>();
        for (int r : res) {
            btnNames.add(NimUIKit.getContext().getString(r));
        }
        return btnNames;
    }

    // 获取邀请他人类型
    public static TeamInviteModeEnum getInviteModeEnum(String name) {
        TeamInviteModeEnum type = null;

        if (name.equals(NimUIKit.getContext().getString(R.string.team_admin_invite))) {
            type = TeamInviteModeEnum.Manager;
        } else if (name.equals(NimUIKit.getContext().getString(R.string.team_everyone_invite))) {
            type = TeamInviteModeEnum.All;
        }

        return type;
    }

    // 获取邀请他人文本描述
    public static int getInviteModeString(TeamInviteModeEnum type) {
        if (type == TeamInviteModeEnum.Manager) {
            return R.string.team_admin_invite;
        } else {
            return R.string.team_everyone_invite;
        }
    }

    // 群资料修改权限菜单名称
    public static List<String> createTeamInfoUpdateMenuStrings() {
        int[] res = new int[]{R.string.team_admin_update, R.string.team_everyone_update,
                R.string.cancel};
        List<String> btnNames = new ArrayList<>();
        for (int r : res) {
            btnNames.add(NimUIKit.getContext().getString(r));
        }
        return btnNames;
    }

    // 获取群资料修改类型
    public static TeamUpdateModeEnum getUpdateModeEnum(String name) {
        TeamUpdateModeEnum type = null;

        if (name.equals(NimUIKit.getContext().getString(R.string.team_admin_update))) {
            type = TeamUpdateModeEnum.Manager;
        } else if (name.equals(NimUIKit.getContext().getString(R.string.team_everyone_update))) {
            type = TeamUpdateModeEnum.All;
        }

        return type;
    }

    // 获取群资料修改类型文本描述
    public static int getInfoUpdateModeString(TeamUpdateModeEnum type) {
        if (type == TeamUpdateModeEnum.Manager) {
            return R.string.team_admin_update;
        } else {
            return R.string.team_everyone_update;
        }
    }

    // 被邀请人身份验证名称
    public static List<String> createTeamInviteeAuthenMenuStrings() {
        int[] res = new int[]{R.string.team_invitee_need_authen, R.string.team_invitee_not_need_authen,
                R.string.cancel};
        List<String> btnNames = new ArrayList<>();
        for (int r : res) {
            btnNames.add(NimUIKit.getContext().getString(r));
        }
        return btnNames;
    }

    // 获取被邀请人身份类型
    public static TeamBeInviteModeEnum getBeInvitedModeEnum(String name) {
        TeamBeInviteModeEnum type = null;

        if (name.equals(NimUIKit.getContext().getString(R.string.team_invitee_need_authen))) {
            type = TeamBeInviteModeEnum.NeedAuth;
        } else if (name.equals(NimUIKit.getContext().getString(R.string.team_invitee_not_need_authen))) {
            type = TeamBeInviteModeEnum.NoAuth;
        }

        return type;
    }

    // 获取被邀请人类型文本描述
    public static int getBeInvitedModeString(TeamBeInviteModeEnum type) {
        if (type == TeamBeInviteModeEnum.NeedAuth) {
            return R.string.team_invitee_need_authen;
        } else {
            return R.string.team_invitee_not_need_authen;
        }
    }

    /**
     * 获取创建群通讯录选择器option
     *
     * @return
     */
    public static ContactSelectActivity.Option getCreateContactSelectOption(ArrayList<String> memberAccounts, int teamCapacity) {
        // 限制群成员数量在群容量范围内
        int capacity = teamCapacity - (memberAccounts == null ? 0 : memberAccounts.size());
        ContactSelectActivity.Option option = TeamHelper.getContactSelectOption(memberAccounts);
        option.maxSelectNum = capacity;
        option.maxSelectedTip = NimUIKit.getContext().getString(R.string.reach_team_member_capacity, teamCapacity);
        option.allowSelectEmpty = true;
        option.alreadySelectedAccounts = memberAccounts;
        return option;
    }

    /**
     * 获取通讯录选择器option
     *
     * @param memberAccounts
     * @return
     */
    public static ContactSelectActivity.Option getContactSelectOption(List<String> memberAccounts) {
        ContactSelectActivity.Option option = new ContactSelectActivity.Option();
        option.title = NimUIKit.getContext().getString(R.string.invite_member);
        if (memberAccounts != null) {
            ArrayList<String> disableAccounts = new ArrayList<>();
            disableAccounts.addAll(memberAccounts);
            option.itemDisableFilter = new ContactIdFilter(disableAccounts);
        }
        return option;
    }

    private static Map<TeamMemberType, Integer> teamMemberLevelMap = new HashMap<>(4);

    static {
        teamMemberLevelMap.put(TeamMemberType.Owner, 0);
        teamMemberLevelMap.put(TeamMemberType.Manager, 1);
        teamMemberLevelMap.put(TeamMemberType.Normal, 2);
        teamMemberLevelMap.put(TeamMemberType.Apply, 3);
    }

    /**
     * 邀请的成员，所在群数量已经超限
     *
     * @param failedAccounts 超限的账号列表
     * @param context        context
     */
    public static void onMemberTeamNumOverrun(List<String> failedAccounts, Context context) {
        if (failedAccounts != null && !failedAccounts.isEmpty()) {
            StringBuilder tipContent = new StringBuilder("好友：");
            for (int i = 0; i < failedAccounts.size(); i++) {
                String name = UserInfoHelper.getUserDisplayName(failedAccounts.get(i));
                tipContent.append(name);
                if (i != failedAccounts.size() - 1) {
                    tipContent.append("、");
                }
            }
            tipContent.append("所在群组数量达到上限，邀请失败");
            Toast.makeText(context, tipContent.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public static Comparator<TeamMember> teamMemberComparator = new Comparator<TeamMember>() {
        @Override
        public int compare(TeamMember l, TeamMember r) {
            if (l == null) {
                return 1;
            }

            if (r == null) {
                return -1;
            }

            return teamMemberLevelMap.get(l.getType()) - teamMemberLevelMap.get(r.getType());
        }
    };


    public static String getTeamName(String teamId) {
        Team team = NimUIKit.getTeamProvider().getTeamById(teamId);
        return team == null ? teamId : TextUtils.isEmpty(team.getName()) ? team.getId() : team
                .getName();
    }

    /**
     * 获取显示名称。用户本人显示“我”
     *
     * @param tid
     * @param account
     * @return
     */
    public static String getTeamMemberDisplayName(String tid, String account) {
        if (account.equals(NimUIKit.getAccount())) {
            return "我";
        }

        return getDisplayNameWithoutMe(tid, account);
    }

    /**
     * 获取显示名称。用户本人显示“你”
     *
     * @param tid
     * @param account
     * @return
     */
    public static String getTeamMemberDisplayNameYou(String tid, String account) {
        if (account.equals(NimUIKit.getAccount())) {
            return "你";
        }

        return getDisplayNameWithoutMe(tid, account);
    }

    /**
     * 获取显示名称。用户本人也显示昵称
     * 备注>群昵称>昵称
     */
    public static String getDisplayNameWithoutMe(String tid, String account) {

        String alias = NimUIKit.getContactProvider().getAlias(account);
        if (!TextUtils.isEmpty(alias)) {
            return alias;
        }

        String memberNick = getTeamNick(tid, account);
        if (!TextUtils.isEmpty(memberNick)) {
            return memberNick;
        }

        return UserInfoHelper.getUserName(account);
    }

    public static String getTeamNick(String tid, String account) {
        Team team = NimUIKit.getTeamProvider().getTeamById(tid);
        if (team != null && team.getType() == TeamTypeEnum.Advanced) {
            TeamMember member = NimUIKit.getTeamProvider().getTeamMember(tid, account);
            if (member != null && !TextUtils.isEmpty(member.getTeamNick())) {
                return member.getTeamNick();
            }
        }
        return null;
    }
}
