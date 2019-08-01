package com.netease.nim.uikit.common.adapter;

import android.view.View;

/**
 */
public class ItemClickListerAdapter<T> implements OnItemClickListener<T> {

    @Override
    public void onClick(View v, int pos, T data) {

    }

    @Override
    public boolean onLongClick(View v, int pos, T data) {
        return false;
    }
}
