package com.netease.nim.uikit.common.adapter;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public abstract class LifecycleViewHolder<T> extends BaseViewHolder<T>
        implements RecyclerView.RecyclerListener, LifecycleOwner {

    public LifecycleViewHolder(ViewGroup parent, int layoutId, LifecycleOwner outerLifecycleOwner) {
        super(parent, layoutId);

        outerLifecycleOwner.getLifecycle().addObserver(new GenericLifecycleObserver() {
            @Override
            public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    if (mLifecycle != null) {
                        mLifecycle.markState(Lifecycle.State.DESTROYED);
                    }
                }
            }
        });
    }

    @Override
    protected void onBindViewHolder(T data) {
        mLifecycle = new LifecycleRegistry(this);
        mLifecycle.markState(Lifecycle.State.RESUMED);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        mLifecycle.markState(Lifecycle.State.DESTROYED);
    }

    protected LifecycleRegistry mLifecycle;

    @Override
    public Lifecycle getLifecycle() {
        return mLifecycle;
    }
}
