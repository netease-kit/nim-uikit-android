package com.netease.nim.uikit.session.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.recent.AitHelper;
import com.netease.nim.uikit.session.module.input.MessageEditWatcher;
import com.netease.nim.uikit.team.activity.TeamMemberListActivity;
import com.netease.nim.uikit.team.model.TeamExtras;
import com.netease.nim.uikit.team.model.TeamRequestCode;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MemberPushOption;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.ArrayList;
import java.util.HashMap;
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

    private Map<String, TeamMember> selectedMembers;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inputPanel.setWatcher(watcher);
    }

    @Override
    public boolean isAllowSendMessage(IMMessage message) {
        if (team == null || !team.isMyTeam()) {
            Toast.makeText(getActivity(), R.string.team_send_message_not_allow, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public boolean sendMessage(IMMessage message) {

        if (selectedMembers != null && !selectedMembers.isEmpty()) {
            // 从消息中构造 option 字段
            MemberPushOption option = createMemPushOption(selectedMembers, message);
            message.setMemberPushOption(option);
            selectedMembers = null;
        }
        return super.sendMessage(message);
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        selectedMembers = null;
    }

    // inputPanel 文本输入框监听
    MessageEditWatcher watcher = new MessageEditWatcher() {
        @Override
        public void afterTextChanged(Editable editable, int start, int count) {
            if (count <= 0 || editable.length() < start + count)
                return;
            CharSequence s = editable.subSequence(start, start + count);
            if (s != null && s.toString().equals("@")) {
                // 选择联系人
                TeamMemberListActivity.start(getActivity(), team.getId());
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == TeamRequestCode.REQUEST_TEAM_AIT_MEMBER) {
            TeamMember member = (TeamMember) data.getSerializableExtra(TeamExtras.RESULT_EXTRA_DATA);
            if (selectedMembers == null) {
                selectedMembers = new HashMap<>();
            }
            selectedMembers.put(member.getAccount(), member);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private MemberPushOption createMemPushOption(Map<String, TeamMember> selectedMembers, IMMessage message) {
        if (message == null || selectedMembers == null) {
            return null;
        }

        List<String> pushList = new ArrayList<>();

        String text = message.getContent();

        // remove invalid account
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

        // replace
        keys = selectedMembers.keySet().iterator();
        while (keys.hasNext()) {
            String account = keys.next();
            String aitName = AitHelper.getAitName(selectedMembers.get(account));
            text = text.replaceAll("(@" + account + " )", "@" + aitName + " ");

            pushList.add(account);
        }
        message.setContent(text);

        if (pushList.isEmpty()) {
            return null;
        }

        MemberPushOption memberPushOption = new MemberPushOption();
        memberPushOption.setForcePush(true);
        memberPushOption.setForcePushContent(message.getContent());
        memberPushOption.setForcePushList(pushList);
        return memberPushOption;
    }
}