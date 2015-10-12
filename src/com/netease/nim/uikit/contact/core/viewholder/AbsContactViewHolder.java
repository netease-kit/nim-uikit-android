package com.netease.nim.uikit.contact.core.viewholder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.netease.nim.uikit.contact.core.model.ContactDataAdapter;
import com.netease.nim.uikit.contact.core.item.AbsContactItem;

public abstract class AbsContactViewHolder<T extends AbsContactItem> {
    protected View view;

    protected Context context;

    public AbsContactViewHolder() {

    }

    public abstract void refresh(ContactDataAdapter adapter, int position, T item);

    public abstract View inflate(LayoutInflater inflater);

    public final View getView() {
        return view;
    }

    public void create(Context context) {
        this.context = context;
        this.view = inflate(LayoutInflater.from(context));
    }
}