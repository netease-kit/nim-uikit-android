/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.searchkit.ui.view;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.searchkit.ui.databinding.SearchTitleItemLayoutBinding;
import com.netease.yunxin.kit.searchkit.ui.model.TitleBean;

public class TitleViewHolder extends BaseViewHolder<TitleBean> {

    private SearchTitleItemLayoutBinding viewBinding;

    public TitleViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public TitleViewHolder(@NonNull SearchTitleItemLayoutBinding viewBinding) {
        this(viewBinding.getRoot());
        this.viewBinding = viewBinding;
    }

    @Override
    public void onBindData(TitleBean data, int position) {
        if (TextUtils.isEmpty(data.title)){
            viewBinding.tvTitle.setText(data.titleRes);
        }else {
            viewBinding.tvTitle.setText(data.title);
        }
    }
}