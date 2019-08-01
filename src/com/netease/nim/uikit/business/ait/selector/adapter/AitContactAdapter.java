package com.netease.nim.uikit.business.ait.selector.adapter;

import android.support.v7.widget.RecyclerView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.ait.selector.holder.RobotViewHolder;
import com.netease.nim.uikit.business.ait.selector.holder.SimpleLabelViewHolder;
import com.netease.nim.uikit.business.ait.selector.holder.TeamMemberViewHolder;
import com.netease.nim.uikit.business.ait.selector.model.AitContactItem;
import com.netease.nim.uikit.business.ait.selector.model.ItemType;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;

import java.util.List;

/**
 * Created by hzchenkang on 2017/6/21.
 */

public class AitContactAdapter extends BaseMultiItemQuickAdapter<AitContactItem, BaseViewHolder> {

    public AitContactAdapter(RecyclerView recyclerView, List<AitContactItem> data) {
        super(recyclerView, data);
        addItemType(ItemType.SIMPLE_LABEL, R.layout.nim_ait_contact_label_item, SimpleLabelViewHolder.class);
        addItemType(ItemType.ROBOT, R.layout.nim_ait_contact_robot_item, RobotViewHolder.class);
        addItemType(ItemType.TEAM_MEMBER, R.layout.nim_ait_contact_team_member_item, TeamMemberViewHolder.class);
    }

    @Override
    protected int getViewType(AitContactItem item) {
        return item.getViewType();
    }

    @Override
    protected String getItemKey(AitContactItem item) {
        return "" + item.getViewType() + item.hashCode();
    }
}
