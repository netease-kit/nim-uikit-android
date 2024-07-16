/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.activities.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.netease.yunxin.kit.common.ui.databinding.EmptyViewHolderLayoutBinding

class EmptyViewHolder(viewBinding: EmptyViewHolderLayoutBinding) : BaseViewHolder<String>(
    viewBinding
) {

    override fun onBindData(data: String, position: Int) {
        val binding = viewBinding as EmptyViewHolderLayoutBinding
        binding.emptyTextView.text = data
    }

    class Factory : BaseViewHolder.Factory<String>() {
        override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<String> {
            val holderLayoutBinding = EmptyViewHolderLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return EmptyViewHolder(holderLayoutBinding)
        }
    }
}
