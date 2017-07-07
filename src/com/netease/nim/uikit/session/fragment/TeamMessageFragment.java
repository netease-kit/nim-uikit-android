package com.netease.nim.uikit.session.fragment;

import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.contact.ait.AitContactSelectorActivity;
import com.netease.nim.uikit.contact.ait.AitedContacts;
import com.netease.nim.uikit.recent.TeamMemberAitHelper;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MemberPushOption;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhoujianghua on 2015/9/10.
 */
public class TeamMessageFragment extends MessageFragment {

    private Team team;

    @Override
    public boolean isAllowSendMessage(IMMessage message) {
        if (team == null || !team.isMyTeam()) {
            Toast.makeText(getActivity(), R.string.team_send_message_not_allow, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void startAitContactActivity() {
        AitContactSelectorActivity.start(getActivity(), team == null ? null : team.getId(), true);
    }

    @Override
    public boolean sendMessage(IMMessage message) {
        Map<String, TeamMember> aitedTeamMembers = AitedContacts.getInstance().getSelectedMembers();
        if (!aitedTeamMembers.isEmpty()) {
            // 移走后面又删掉的账号
            removeInvalidAccount(aitedTeamMembers, message);
            // 从消息中构造 option 字段
            setMemPushOption(aitedTeamMembers, message);
            // 替换文本中的账号为昵称
            replaceNickName(aitedTeamMembers, message);
        }
        return super.sendMessage(message);
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    private void setMemPushOption(Map<String, TeamMember> selectedMembers, IMMessage message) {
        if (message == null || selectedMembers == null) {
            return;
        }

        List<String> pushList = new ArrayList<>();

        // remove invalid account
        Iterator<String> keys = selectedMembers.keySet().iterator();
        while (keys.hasNext()) {
            String account = keys.next();
            pushList.add(account);
        }
        if (pushList.isEmpty()) {
            return;
        }
        MemberPushOption memberPushOption = new MemberPushOption();
        memberPushOption.setForcePush(true);
        memberPushOption.setForcePushContent(message.getContent());
        memberPushOption.setForcePushList(pushList);
        message.setMemberPushOption(memberPushOption);
    }

    private void removeInvalidAccount(Map<String, TeamMember> selectedMembers, IMMessage message) {
        if (message == null || selectedMembers == null) {
            return;
        }
        String text = message.getContent();
        Iterator<String> keys = selectedMembers.keySet().iterator();
        while (keys.hasNext()) {
            String account = keys.next();
            Pattern p = Pattern.compile("(@" + account + " )");
            Matcher matcher = p.matcher(text);
            if (matcher.find()) {
                continue;
            }
            keys.remove();
        }
    }

    private void replaceNickName(Map<String, TeamMember> selectedMembers, IMMessage message) {
        if (message == null || selectedMembers == null) {
            return;
        }
        String text = message.getContent();
        Iterator<String> keys = selectedMembers.keySet().iterator();
        while (keys.hasNext()) {
            String account = keys.next();
            String aitName = TeamMemberAitHelper.getAitName(selectedMembers.get(account));
            text = text.replaceAll("(@" + account + " )", "@" + aitName + " ");
        }
        message.setContent(text);
    }
}