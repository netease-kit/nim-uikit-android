package com.netease.nim.uikit.business.session.fragment;

import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;

/**
 * Created by zhoujianghua on 2015/9/10.
 */
public class TeamMessageFragment extends MessageFragment {

    private Team team;

    @Override
    public boolean isAllowSendMessage(IMMessage message) {
        if (team == null) {
            team = NimUIKit.getTeamProvider().getTeamById(sessionId);
        }

        if (team == null || !team.isMyTeam()) {
            Toast.makeText(getActivity(), R.string.team_send_message_not_allow, Toast.LENGTH_SHORT).show();
            return false;
        }

        return super.isAllowSendMessage(message);
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}