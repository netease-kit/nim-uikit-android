/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.searchkit.ui.model;

import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.searchkit.ui.commone.SearchConstant;

public class TitleBean extends BaseBean {

    public String title;
    public int titleRes;

    public TitleBean(String tt){
        this.title = tt;
        this.viewType = SearchConstant.ViewType.TITLE;
    }

    public TitleBean(int res){
        this.titleRes = res;
        this.viewType = SearchConstant.ViewType.TITLE;
    }
}
