package com.netease.nim.uikit.business.contact.core.viewholder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.netease.nim.uikit.business.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.business.contact.core.model.ContactDataAdapter;

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