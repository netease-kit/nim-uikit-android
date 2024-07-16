/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonAdapter
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonData
import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseViewHolder
import com.netease.yunxin.kit.common.ui.databinding.CommonActLayoutBinding

open class CommonListActivity : BaseActivity() {

    private lateinit var viewBinding: CommonActLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = CommonActLayoutBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    open fun updateData(dataList: List<CommonData<*>>) {
        getAdapter().setData(dataList)
    }

    open fun getAdapter(): CommonAdapter {
        return CommonAdapter()
    }

    open fun getLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    open fun <T : BaseViewHolder.Factory<*>> registerViewHolder(viewType: Int, clazz: Class<T>) {
        getAdapter().addViewHolder(viewType, clazz)
    }
}
