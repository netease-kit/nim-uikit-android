package com.netease.nim.uikit.common.adapter;

import java.util.ArrayList;
import java.util.Iterator;

/**

 */

public class AdvancedAdapter extends BaseAdapter<BaseViewHolderData> {

    public AdvancedAdapter() {
        super(new ArrayList<BaseViewHolderData>());
    }

    public AdvancedAdapter(OnItemClickListener listener) {
        super(new ArrayList<BaseViewHolderData>(), listener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.bindViewHolder(dataList.get(position).getData());
        listenClick(holder);
    }

    public void clear() {
        this.dataList.clear();
    }

    public void add(int type, Object data) {
        this.dataList.add(new BaseViewHolderData(type, data));
    }

    public void add(int type, Object data, int index) {
        this.dataList.add(index, new BaseViewHolderData(type, data));
    }

    @Override
    public final int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public final int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void removeByTypeAll(int type) {
        remove(type, false);
    }

    public void removeByType(int type) {
        remove(type, true);
    }

    private void remove(int type, boolean onlyOne) {
        Iterator<BaseViewHolderData> iterator = this.dataList.iterator();
        while (iterator.hasNext()) {
            BaseViewHolderData data = iterator.next();
            if (data.getViewType() == type) {
                iterator.remove();
                if (onlyOne) {
                    break;
                }
            }
        }
    }
}
