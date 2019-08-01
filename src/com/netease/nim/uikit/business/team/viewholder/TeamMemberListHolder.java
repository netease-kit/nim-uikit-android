package com.netease.nim.uikit.business.team.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nimlib.sdk.team.model.TeamMember;

/**
 * Created by hzchenkang on 2016/12/2.
 */

public class TeamMemberListHolder extends RecyclerView.ViewHolder {

    private HeadImageView headImageView;

    private TextView nameTextView;

    private View container;


    public TeamMemberListHolder(View itemView) {
        super(itemView);
        headImageView = (HeadImageView) itemView.findViewById(R.id.imageViewHeader);
        nameTextView = (TextView) itemView.findViewById(R.id.textViewName);
        container = itemView;
    }

    public void refresh(TeamMember member) {
        headImageView.resetImageView();
        nameTextView.setText(TeamHelper.getTeamMemberDisplayName(member.getTid(), member.getAccount()));
        headImageView.loadBuddyAvatar(member.getAccount());
        container.setTag(member);
    }

}
