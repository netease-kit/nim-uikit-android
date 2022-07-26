/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui;

import android.util.SparseArray;
import android.view.View;

import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactClickListener;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactSelectorListener;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.model.MenuBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import com.netease.yunxin.kit.contactkit.ui.view.ContactViewHolderFactory;

import java.util.List;

public class ContactUIConfig {

    public static int INT_DEFAULT_NULL = -1;

    //title config
    public String title;
    public int titleColor = INT_DEFAULT_NULL;
    public boolean showTitleBar = true;
    public boolean showTitleBarRight2Icon = true;
    public int titleBarRight2Res = INT_DEFAULT_NULL;
    public View.OnClickListener titleBarRight2Click;
    public boolean showTitleBarRightIcon = true;
    public int titleBarRightRes = INT_DEFAULT_NULL;
    public View.OnClickListener titleBarRightClick;
    public List<MenuBean> titleBarRightMenu;

    //page
    public boolean showHeader = true;
    public List<ContactEntranceBean> headerData;
    public ContactListViewAttrs contactAttrs;

    public SparseArray<IContactClickListener> itemClickListeners = new SparseArray<>();
    public SparseArray<IContactSelectorListener> itemSelectorListeners = new SparseArray<>();

    public IContactFactory viewHolderFactory;

}
