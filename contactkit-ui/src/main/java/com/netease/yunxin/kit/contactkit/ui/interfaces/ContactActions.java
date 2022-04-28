/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.interfaces;

import android.util.SparseArray;

import com.netease.yunxin.kit.contactkit.ui.view.ContactListView;

/**
 * Actions for default ViewHolder in {@link ContactListView}
 */
public class ContactActions {

    private final SparseArray<IContactClickListener> itemClickListeners;

    private final SparseArray<IContactSelectorListener> itemSelectorListeners;

    public ContactActions() {
        itemClickListeners = new SparseArray<>();
        itemSelectorListeners = new SparseArray<>();
    }

    public void addSelectorListener(int viewType, IContactSelectorListener selectorListener) {
        itemSelectorListeners.put(viewType, selectorListener);
    }

    public void addContactListener(int viewType, IContactClickListener contactListener) {
        itemClickListeners.put(viewType, contactListener);
    }

    public IContactClickListener getContactListener(int viewType) {
        return itemClickListeners.get(viewType);
    }

    public IContactSelectorListener getSelectorListener(int viewType) {
        return itemSelectorListeners.get(viewType);
    }
}
