/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.netease.yunxin.kit.contactkit.ui.databinding.BaseListActivityLayoutBinding;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;


public abstract class BaseListActivity extends BaseActivity {

    protected BaseListActivityLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = BaseListActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.title.setOnBackIconClickListener(v -> onBackPressed());
        initView();
        initData();
    }

    protected abstract void initView();

    protected abstract void initData();


}
