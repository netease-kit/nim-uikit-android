package com.netease.nim.uikit.team.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.team.viewholder.TeamMemberHolder;

import java.util.List;

public class TeamMemberAdapter extends TAdapter {

    /**
     * 当前GridView显示模式：显示讨论组成员，正在移除讨论组成员
     */
    public static enum Mode {
        NORMAL,
        DELETE
    }

    /**
     * 每个Item的类型：讨论组成员，添加成员，移除成员
     */
    public static enum TeamMemberItemTag {
        NORMAL,
        ADD,
        DELETE
    }

    /**
     * GridView数据项
     */
    public static class TeamMemberItem {
        private TeamMemberItemTag tag;
        private String tid;
        private String account;
        private String desc;

        public TeamMemberItem(TeamMemberItemTag tag, String tid, String account, String desc) {
            this.tag = tag;
            this.tid = tid;
            this.account = account;
            this.desc = desc;
        }

        public TeamMemberItemTag getTag() {
            return tag;
        }

        public String getTid() {
            return tid;
        }

        public String getDesc() {
            return desc;
        }

        public String getAccount() {
            return account;
        }
    }

    /**
     * 群成员移除回调函数
     */
    public static interface RemoveMemberCallback {
        public void onRemoveMember(String account);
    }

    public static interface AddMemberCallback {
        public void onAddMember();
    }

    private Context context;

    private Mode mode = Mode.NORMAL;

    private RemoveMemberCallback removeMemberCallback;

    private AddMemberCallback addMemberCallback;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public boolean switchMode() {
        if (getMode() == Mode.DELETE) {
            setMode(Mode.NORMAL);
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    public RemoveMemberCallback getRemoveMemberCallback() {
        return removeMemberCallback;
    }

    public AddMemberCallback getAddMemberCallback() {
        return addMemberCallback;
    }

    public TeamMemberAdapter(Context context, List<?> items, TAdapterDelegate delegate,
                             RemoveMemberCallback removeMemberCallback, AddMemberCallback addMemberCallback) {
        super(context, items, delegate);
        this.context = context;
        this.removeMemberCallback = removeMemberCallback;
        this.addMemberCallback = addMemberCallback;
    }

    private TeamMemberHolder.TeamMemberHolderEventListener teamMemberHolderEventListener;

    public void setEventListener(TeamMemberHolder.TeamMemberHolderEventListener eventListener) {
        this.teamMemberHolderEventListener = eventListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (teamMemberHolderEventListener != null) {
            ((TeamMemberHolder) view.getTag()).setEventListener(teamMemberHolderEventListener);
        }

        return view;
    }
}
