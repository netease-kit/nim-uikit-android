/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.indexbar.bean;


import com.netease.yunxin.kit.contactkit.ui.indexbar.suspension.ISuspension;

import java.io.Serializable;

/**
 * base bean for index
 */
public abstract class IndexBean implements ISuspension, Serializable {

    private String indexTag;//tag of index （show in the top）

    public String getIndexTag() {
        return indexTag;
    }

    public IndexBean setIndexTag(String baseIndexTag) {
        this.indexTag = baseIndexTag;
        return this;
    }

    @Override
    public String getTag() {
        return indexTag;
    }

    @Override
    public boolean isShowDivision() {
        return true;
    }
}
