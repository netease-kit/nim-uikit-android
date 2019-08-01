package com.netease.nim.uikit.business.team.viewholder;

import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.model.Announcement;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.util.sys.TimeUtil;

/**
 * Created by hzxuwen on 2015/3/20.
 */
public class TeamAnnounceHolder extends TViewHolder {

    private TextView announceTitle;
    private TextView teamName;
    private TextView announceCreateTime;
    private TextView announceContent;

    @Override
    protected int getResId() {
        return R.layout.nim_advanced_team_announce_list_item;
    }

    @Override
    protected void inflate() {
        announceTitle = (TextView) view.findViewById(R.id.announce_title);
        teamName = (TextView) view.findViewById(R.id.team_name);
        announceCreateTime = (TextView) view.findViewById(R.id.announce_create_time);
        announceContent = (TextView) view.findViewById(R.id.announce_content);
    }

    @Override
    protected void refresh(Object item) {
        Announcement a = (Announcement) item;
        announceTitle.setText(a.getTitle());
        teamName.setText(TeamHelper.getTeamMemberDisplayName(a.getTeamId(), a.getCreator()));
        announceCreateTime.setText(TimeUtil.getTimeShowString((a.getTime() * 1000), false)); // 兼容ios
        announceContent.setText(a.getContent());
    }
}
