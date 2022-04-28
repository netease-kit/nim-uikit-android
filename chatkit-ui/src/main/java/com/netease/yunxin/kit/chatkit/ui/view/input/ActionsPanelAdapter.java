package com.netease.yunxin.kit.chatkit.ui.view.input;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMeesageMoreItemViewBinding;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

public class ActionsPanelAdapter extends RecyclerView.Adapter<ActionsPanelAdapter.GridViewHolder> {
    private static final int ROW_COUNT = 2;
    private static final int COLUMN_COUNT = 4;
    private static final int PAGE_SIZE = ROW_COUNT * COLUMN_COUNT;
    private Context mContext;
    private ArrayList<ActionItem> items;
    private OnActionItemClick onActionItemClick;

    public ActionsPanelAdapter(Context context, ArrayList<ActionItem> items) {
        this.mContext = context;
        this.items = items;
    }

    public void setOnActionItemClick(OnActionItemClick onActionItemClick) {
        this.onActionItemClick = onActionItemClick;
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GridLayout gridLayout = new GridLayout(mContext);
        gridLayout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        gridLayout.setColumnCount(COLUMN_COUNT);
        gridLayout.setRowCount(ROW_COUNT);
        return new GridViewHolder(gridLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        ((GridLayout) holder.itemView).removeAllViews();
        int start = position * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE && start + i < items.size(); ++i) {
            int index = start + i;
            ActionItem item = items.get(index);
            ((GridLayout) holder.itemView).addView(inflateItem(item, i));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        // todo date change
    }

    private View inflateItem(ActionItem item, int i) {
        ChatMeesageMoreItemViewBinding binding = ChatMeesageMoreItemViewBinding.inflate(LayoutInflater.from(mContext));
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        param.width = ScreenUtil.getDisplayWidth() / COLUMN_COUNT;
        param.setGravity(Gravity.CENTER);
        param.columnSpec = GridLayout.spec(i % COLUMN_COUNT);
        param.rowSpec = GridLayout.spec(i / COLUMN_COUNT);
        binding.getRoot().setLayoutParams(param);
        binding.actionIcon.setBackgroundResource(item.getIconResId());
        binding.actionText.setText(item.getTitleResId());
        binding.getRoot().setOnClickListener(view -> {
            item.onClick(view);
            if (onActionItemClick != null) {
                onActionItemClick.onClick(item);
            }
        });
        return binding.getRoot();
    }

    @Override
    public int getItemCount() {
        return items.size() / PAGE_SIZE + 1;
    }

    static class GridViewHolder extends RecyclerView.ViewHolder {

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    interface OnActionItemClick {
        void onClick(ActionItem item);
    }
}
