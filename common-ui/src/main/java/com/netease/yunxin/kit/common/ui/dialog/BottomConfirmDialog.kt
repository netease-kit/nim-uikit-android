/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.netease.yunxin.kit.common.ui.databinding.ConfirmDialogLayoutBinding

class BottomConfirmDialog : BaseBottomDialog() {

    var binding: ConfirmDialogLayoutBinding? = null

    var listener: ConfirmListener? = null

    var title: String? = null

    var positive: String? = null

    var negative: String? = null

    var viewRender: ((ConfirmDialogLayoutBinding) -> Unit)? = null

    companion object {
        const val TAG = "CommonConfirmDialog"
    }

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = ConfirmDialogLayoutBinding.inflate(inflater, container, false)
        binding?.tvNegative?.setOnClickListener {
            dismiss()
            listener?.onNegative()
        }
        binding?.tvPositive?.setOnClickListener {
            listener?.onPositive()
        }
        binding?.tvTitle?.text = title
        binding?.tvPositive?.text = positive
        binding?.tvNegative?.text = negative
        binding?.run {
            viewRender?.invoke(this)
        }
        return binding?.root
    }

    fun setTitleStr(title: String): BottomConfirmDialog {
        this.title = title
        binding?.tvTitle?.text = title
        return this
    }

    fun setPositiveStr(str: String): BottomConfirmDialog {
        positive = str
        binding?.tvPositive?.text = str
        return this
    }

    fun setNegativeStr(str: String): BottomConfirmDialog {
        negative = str
        binding?.tvNegative?.text = str
        return this
    }

    fun setConfirmListener(confirmListener: ConfirmListener): BottomConfirmDialog {
        listener = confirmListener
        return this
    }

    fun configViewRender(render: (ConfirmDialogLayoutBinding) -> Unit) {
        this.viewRender = render
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }
}

interface ConfirmListener {
    fun onPositive()

    fun onNegative()
}
