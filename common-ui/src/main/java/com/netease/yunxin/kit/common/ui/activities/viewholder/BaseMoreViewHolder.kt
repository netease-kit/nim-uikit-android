/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.activities.viewholder

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Base more view holder
 *
 * @param T
 * @param VB
 * @property binding
 * @constructor Create empty Base more view holder
 */
abstract class BaseMoreViewHolder<T, VB : ViewBinding>(protected val binding: VB) :
    RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(item: T)
    open fun bind(item: T, payloads: List<Any>) {}
}
