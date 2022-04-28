/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui;

import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.View;

import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactClickListener;
import com.netease.yunxin.kit.contactkit.ui.interfaces.IContactSelectorListener;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.model.MenuBean;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.ContactViewHolderFactory;

import java.util.List;

public class ContactConfig {

    //title config
    public String title;
    public int titleColor = -1;
    public boolean showTitleBar = true;
    public boolean showTitleSearchIcon = true;
    public Drawable titleSearchIcon;
    public View.OnClickListener titleSearchClick;
    public boolean showTitleMoreIcon = true;
    public Drawable titleMoreIcon;
    public View.OnClickListener titleMoreClick;
    public List<MenuBean> moreMenu;

    //page
    public boolean showIndexBar = true;
    public boolean showHeader = true;
    public int itemTextColor = -1;
    public ContactViewHolderFactory viewHolderFactory;
    public ContactActions contactActions;
    public List<ContactEntranceBean> headerData;
    public SparseArray<IContactClickListener> itemClickListeners = new SparseArray<>();;
    public SparseArray<IContactSelectorListener> itemSelectorListeners = new SparseArray<>();;

}
