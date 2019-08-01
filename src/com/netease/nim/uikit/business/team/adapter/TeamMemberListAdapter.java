package com.netease.nim.uikit.business.team.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.team.viewholder.TeamMemberListHolder;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.List;

/**
 * Created by hzchenkang on 2016/12/2.
 */

public class TeamMemberListAdapter extends RecyclerView.Adapter<TeamMemberListHolder>
        implements View.OnClickListener {

    public interface ItemClickListener {
        void onItemClick(TeamMember member);
    }

    private Context context;

    private List<TeamMember> members;

    private ItemClickListener listener;

    public TeamMemberListAdapter(Context context) {
        this.context = context;
    }

    public void updateData(List<TeamMember> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    public void setListener(ItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public TeamMemberListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) {
            return null;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.nim_ait_contact_team_member_item, parent, false);
        v.setOnClickListener(this);
        return new TeamMemberListHolder(v);
    }

    @Override
    public void onBindViewHolder(TeamMemberListHolder holder, int position) {
        if (members == null || members.size() <= position) {
            return;
        }

        TeamMember member = members.get(position);

        holder.refresh(member);
    }

    @Override
    public int getItemCount() {
        return members == null ? 0 : members.size();
    }

    @Override
    public void onClick(View v) {
        TeamMember member = (TeamMember) v.getTag();
        if (listener != null) {
            listener.onItemClick(member);
        }
    }
}
