/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.interfaces;

import com.netease.yunxin.kit.contactkit.ui.IContactFactory;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import com.netease.yunxin.kit.contactkit.ui.view.adapter.ContactAdapter;


public interface IContactListView {

    //******************interaction*************

    /**
     * set action for Contact
     *
     * @param contactActions actions
     */
    void setContactAction(ContactActions contactActions);

    //******************ui display*************

    /**
     * get adapter in ContactListView
     *
     */
    ContactAdapter getAdapter();


    /**
     * if you want add your owner viewHolder,you should set your owner factory here
     *
     * @param viewHolderFactory your owner ContactViewHolderFactory
     */
    void setViewHolderFactory(IContactFactory viewHolderFactory);

    /**
     * set config for ContactListView
     *
     * @param attrs config
     */
    void setViewConfig(ContactListViewAttrs attrs);
}
