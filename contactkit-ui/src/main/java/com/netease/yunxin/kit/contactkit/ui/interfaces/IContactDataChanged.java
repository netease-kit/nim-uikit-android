/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.interfaces;

import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListView;

import java.util.List;

/**
 * when data have changed，you can user this interface post data to {@link ContactListView}
 */
public interface IContactDataChanged {

    /**
     * Contacts friend data have changed
     */
    void onFriendDataSourceChanged(List<ContactFriendBean> contactItemBeanList);

    /**
     * add one friend data
     *
     * @param friend friend data,this data will been sort
     */
    void addFriendData(List<ContactFriendBean> friend);

    /**
     * remove one friend data
     *
     * @param friend friend data,data will been sort
     */
    void removeFriendData(List<ContactFriendBean> friend);

    /**
     * update one friend data
     *
     * @param friend friend data,data will been sort
     */
    void updateFriendData(List<ContactFriendBean> friend);

    /**
     * update data which have been added
     *
     * @param data any data instanceof BaseContactBean except Friend
     */
    void updateContactData(BaseContactBean data);

    /**
     * update data as list(remove all this type data and add new data)
     *
     * @param viewType the viewType been update
     * @param data     any data instanceof BaseContactBean except Friends
     */
    void updateContactData(int viewType, List<? extends BaseContactBean> data);

    /**
     * add a custom data
     */
    void addContactData(BaseContactBean contactData);

    /**
     * add custom data as list
     */
    void addContactData(List<? extends BaseContactBean> contactData);

    /**
     * remove custom data
     *
     */
    void removeContactData(BaseContactBean contactData);

    /**
     * remove custom as list
     *
     */
    void removeContactData(List<? extends BaseContactBean> contactData);

    /**
     * clear data
     */
    void clearContactData();

}
