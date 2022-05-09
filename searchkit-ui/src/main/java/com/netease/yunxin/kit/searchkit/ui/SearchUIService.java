/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.searchkit.ui;

import android.content.Context;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.searchkit.SearchKitService;
import com.netease.yunxin.kit.searchkit.ui.page.GlobalSearchActivity;

/**
 * launch service
 * when app start the SearchUIService will be created
 * it need to config in manifest
 */
public class SearchUIService extends SearchKitService {

    @NonNull
    @Override
    public String getServiceName() {
        return "SearchUIService";
    }

    @NonNull
    @Override
    public SearchKitService create(@NonNull Context context) {
        XKitRouter.registerRouter(RouterConstant.PATH_GLOBAL_SEARCH, GlobalSearchActivity.class);
        return this;
    }
}
