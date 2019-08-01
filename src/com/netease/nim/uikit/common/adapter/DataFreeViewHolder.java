package com.netease.nim.uikit.common.adapter;

import android.view.View;
import android.view.ViewGroup;

/**
 */

public abstract class DataFreeViewHolder<T> extends BaseViewHolder<T> {
    public DataFreeViewHolder(ViewGroup parent, int layoutId) {
        super(parent, layoutId);
    }

    public DataFreeViewHolder(View view) {
        super(view);
    }

    public final void bind(T data) {
        bindViewHolder(data);
    }
}
