/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.netease.yunxin.kit.common.ui.action.ActionItem
import com.netease.yunxin.kit.common.ui.databinding.BottomHeaderChoiceDialogLayoutBinding

class BottomHeaderChoiceDialog(context: Context, list: ArrayList<ActionItem>) :
    BaseBottomChoiceDialog(context, list) {

    private lateinit var tvTitle: TextView
    override fun initViewAndGetRootView(): View? {
        var binding: BottomHeaderChoiceDialogLayoutBinding =
            BottomHeaderChoiceDialogLayoutBinding.inflate(LayoutInflater.from(context), null, false)
        cancelTv = binding.tvCancel
        containerView = binding.actionContainer
        tvTitle = binding.tvTitle
        return binding.root
    }

    fun setTitle(title: String) {
        tvTitle.text = title
    }
}
