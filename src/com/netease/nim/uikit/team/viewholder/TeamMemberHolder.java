package com.netease.nim.uikit.team.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.team.adapter.TeamMemberAdapter;

public class TeamMemberHolder extends TViewHolder {

    public interface TeamMemberHolderEventListener {
        void onHeadImageViewClick(String account);
    }

    protected TeamMemberHolderEventListener teamMemberHolderEventListener;

    public void setEventListener(TeamMemberHolderEventListener eventListener) {
        this.teamMemberHolderEventListener = eventListener;
    }

    private HeadImageView headImageView;

    private ImageView ownerImageView;

    private ImageView adminImageView;

    private ImageView deleteImageView;

    private TextView nameTextView;

    private TeamMemberAdapter.TeamMemberItem memberItem;

    public final static String OWNER = "owner";
    public final static String ADMIN = "admin";

    protected TeamMemberAdapter getAdapter() {
        return (TeamMemberAdapter) super.getAdapter();
    }

    @Override
    protected int getResId() {
        return R.layout.nim_team_member_item;
    }

    @Override
    protected void inflate() {
        headImageView = (HeadImageView) view.findViewById(R.id.imageViewHeader);
        nameTextView = (TextView) view.findViewById(R.id.textViewName);
        ownerImageView = (ImageView) view.findViewById(R.id.imageViewOwner);
        adminImageView = (ImageView) view.findViewById(R.id.imageViewAdmin);
        deleteImageView = (ImageView) view.findViewById(R.id.imageViewDeleteTag);
    }

    @Override
    protected void refresh(Object item) {
        memberItem = (TeamMemberAdapter.TeamMemberItem) item;
        headImageView.resetImageView();
        ownerImageView.setVisibility(View.GONE);
        adminImageView.setVisibility(View.GONE);
        deleteImageView.setVisibility(View.GONE);

        if (getAdapter().getMode() == TeamMemberAdapter.Mode.NORMAL) {
            view.setVisibility(View.VISIBLE);
            if (memberItem.getTag() == TeamMemberAdapter.TeamMemberItemTag.ADD) {
                // add team member
                headImageView.setBackgroundResource(R.drawable.nim_team_member_add_selector);
                nameTextView.setText(context.getString(R.string.add));
                headImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getAdapter().getAddMemberCallback().onAddMember();
                    }
                });
            } else if (memberItem.getTag() == TeamMemberAdapter.TeamMemberItemTag.DELETE) {
                // delete team member
                headImageView.setBackgroundResource(R.drawable.nim_team_member_delete_selector);
                nameTextView.setText(context.getString(R.string.remove));
                headImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getAdapter().setMode(TeamMemberAdapter.Mode.DELETE);
                        getAdapter().notifyDataSetChanged();
                    }
                });
            } else {
                // show team member
                refreshTeamMember(memberItem, false);
            }
        } else if (getAdapter().getMode() == TeamMemberAdapter.Mode.DELETE) {
            if (memberItem.getTag() == TeamMemberAdapter.TeamMemberItemTag.NORMAL) {
                refreshTeamMember(memberItem, true);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }

    private void refreshTeamMember(final TeamMemberAdapter.TeamMemberItem item, boolean deleteMode) {
        nameTextView.setText(TeamDataCache.getInstance().getTeamMemberDisplayName(item.getTid(), item.getAccount()));
        headImageView.loadBuddyAvatar(item.getAccount());
        headImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (teamMemberHolderEventListener != null) {
                    teamMemberHolderEventListener.onHeadImageViewClick(item.getAccount());
                }
            }
        });

        if (item.getDesc() != null) {
            if (item.getDesc().equals(OWNER)) {
                ownerImageView.setVisibility(View.VISIBLE);
            } else if (item.getDesc().equals(ADMIN)) {
                adminImageView.setVisibility(View.VISIBLE);
            }
        }

        final String account = item.getAccount();
        if (deleteMode && !isSelf(account)) {
            deleteImageView.setVisibility(View.VISIBLE);
            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getAdapter().getRemoveMemberCallback().onRemoveMember(account);
                }
            });
        } else {
            deleteImageView.setVisibility(View.GONE);
        }
    }

    private boolean isSelf(String account) {
        return account.equals(NimUIKit.getAccount());
    }
}
