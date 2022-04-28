/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.interfaces;

import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;

/**
 * Action listener
 */
public interface IContactClickListener {

        /**
         * on item click
         */
        void onClick(int position, BaseContactBean data);

}
