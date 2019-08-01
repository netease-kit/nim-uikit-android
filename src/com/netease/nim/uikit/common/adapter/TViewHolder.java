package com.netease.nim.uikit.common.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;

public abstract class TViewHolder implements IScrollStateListener {
    /**
     * context
     */
    protected Context context;

    /**
     * fragment
     */
    protected Fragment fragment;

    /**
     * list item view
     */
    protected View view;

    /**
     * adapter providing data
     */
    protected TAdapter adapter;

    /**
     * index of item
     */
    protected int position;

    public TViewHolder() {

    }

    protected void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    protected void setContext(Context context) {

        this.context = context;
    }

    protected void setAdapter(TAdapter adapter) {
        this.adapter = adapter;
    }

    protected TAdapter getAdapter() {
        return this.adapter;
    }

    protected void setPosition(int position) {
        this.position = position;
    }

    public View getView(LayoutInflater inflater) {
        int resId = getResId();
        view = inflater.inflate(resId, null);
        inflate();
        return view;
    }

    public boolean isFirstItem() {
        return position == 0;
    }

    public boolean isLastItem() {
        return position == adapter.getCount() - 1;
    }

    protected abstract int getResId();

    protected abstract void inflate();

    protected abstract void refresh(Object item);

    @Override
    public void reclaim() {
    }

    @Override
    public void onImmutable() {
    }

    protected boolean mutable() {
        return adapter.isMutable();
    }

    public void destory() {

    }

    protected <T extends View> T findView(int resId) {
        return (T) (view.findViewById(resId));
    }
}