/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.activities

import android.os.Bundle
import android.view.View

abstract class CommonActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentView)
        initViewModel()
        initView()
        initData()
    }

    protected abstract fun initViewModel()

    protected abstract val contentView: View?
    protected abstract fun initView()
    protected abstract fun initData()
}
