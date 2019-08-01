package com.netease.nim.uikit.common.ui.recyclerview.listener;

import android.view.View;

import com.netease.nim.uikit.common.ui.recyclerview.adapter.IRecyclerView;


/**
 * A convenience class to extend when you only want to OnItemClickListener for a subset
 * of all the SimpleClickListener. This implements all methods in the
 * {@link SimpleClickListener}
 */
public abstract class OnItemClickListener<T extends IRecyclerView> extends SimpleClickListener<T> {

    @Override
    public void onItemLongClick(T adapter, View view, int position) {

    }

    @Override
    public void onItemChildClick(T adapter, View view, int position) {

    }

    @Override
    public void onItemChildLongClick(T adapter, View view, int position) {

    }
}
