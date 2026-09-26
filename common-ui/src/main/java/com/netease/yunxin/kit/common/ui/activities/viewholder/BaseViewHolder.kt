
/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.activities.viewholder

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseViewHolder<T>(var viewBinding: ViewBinding) : RecyclerView.ViewHolder(
    viewBinding.root
) {

    abstract fun onBindData(data: T, position: Int)

    abstract class Factory<T> {
        abstract fun createViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T>
    }
}
