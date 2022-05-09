/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.model;

/**
 * constants view type
 * 1~9 for default ViewHolder
 */
public interface IViewTypeConstant {


    /**
     * friend
     */
    int CONTACT_FRIEND = 1;

    /**
     * entrance
     */
    int CONTACT_ACTION_ENTER = 2;

    /**
     * custom view type start with this
     */
    int CUSTOM_START = 10;

    /**
     * blackList
     */
    int CONTACT_BLACK_LIST = 11;

    /**
     * verify info
     */
    int CONTACT_VERIFY_INFO = 12;

    /**
     * group list
     */
    int CONTACT_TEAM_LIST = 13;
}
