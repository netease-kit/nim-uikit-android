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
import com.netease.yunxin.kit.common.ui.databinding.ChoiceDialogLayoutBinding

class CommonChoiceDialog : BaseDialog() {

    var binding: ChoiceDialogLayoutBinding? = null

    var listener: ChoiceListener? = null

    var title: String? = null

    var content: String? = null

    var positive: String? = null

    var negative: String? = null

    companion object {
        const val TAG = "CommonChoiceDialog"
    }

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = ChoiceDialogLayoutBinding.inflate(inflater, container, false)
        binding?.tvDialogNegative?.setOnClickListener {
            dismiss()
            listener?.onNegative()
        }
        binding?.tvDialogPositive?.setOnClickListener {
            dismiss()
            listener?.onPositive()
        }
        binding?.tvDialogTitle?.text = title
        binding?.tvDialogContent?.text = content
        binding?.tvDialogPositive?.text = positive
        binding?.tvDialogNegative?.text = negative
        return binding?.root
    }

    fun setTitleStr(title: String): CommonChoiceDialog {
        this.title = title
        binding?.tvDialogTitle?.text = title
        return this
    }

    fun setContentStr(content: String): CommonChoiceDialog {
        this.content = content
        binding?.tvDialogContent?.text = content
        return this
    }

    fun setPositiveStr(str: String): CommonChoiceDialog {
        positive = str
        binding?.tvDialogPositive?.text = positive
        return this
    }

    fun setNegativeStr(str: String): CommonChoiceDialog {
        negative = str
        binding?.tvDialogNegative?.text = negative
        return this
    }

    fun setConfirmListener(confirmListener: ChoiceListener): CommonChoiceDialog {
        listener = confirmListener
        return this
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }
}

interface ChoiceListener {
    fun onPositive()

    fun onNegative()
}
