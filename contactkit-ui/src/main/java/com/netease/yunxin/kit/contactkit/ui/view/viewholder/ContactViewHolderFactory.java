/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.view.viewholder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;

/**
 * viewHolder factory
 */
public abstract class ContactViewHolderFactory {

    public BaseContactViewHolder getViewHolder(ViewGroup view, int viewType) {
        switch (viewType) {
            case IViewTypeConstant.CONTACT_FRIEND:
                return new ContactViewHolder(view);
            case IViewTypeConstant.CONTACT_ACTION_ENTER:
                return new EntranceViewHolder(view);
        }
        if (viewType >= IViewTypeConstant.CUSTOM_START) {
            return getCustomViewHolder(view, viewType);
        }
        return new BaseContactViewHolder(view) {
            @Override
            public void initViewBinding(LayoutInflater layoutInflater, ViewGroup container) {
            }

            @Override
            public void onBind(BaseContactBean bean, int position, ContactListViewAttrs attrs) {
            }
        };
    }

    protected abstract BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType);

}
