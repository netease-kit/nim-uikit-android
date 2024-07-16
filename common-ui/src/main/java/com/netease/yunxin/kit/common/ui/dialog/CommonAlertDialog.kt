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
import androidx.fragment.app.FragmentTransaction
import com.netease.yunxin.kit.common.ui.databinding.AlertDialogLayoutBinding

class CommonAlertDialog : BaseDialog() {

    var binding: AlertDialogLayoutBinding? = null

    var listener: AlertListener? = null

    var title: String? = null

    var content: String? = null

    var positive: String? = null

    companion object {
        const val TAG = "CommonAlertDialog"
    }

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        binding = AlertDialogLayoutBinding.inflate(inflater, container, false)
        binding?.tvDialogPositive?.setOnClickListener {
            dismiss()
            listener?.onPositive()
        }
        binding?.tvDialogTitle?.text = title
        binding?.tvDialogContent?.text = content
        binding?.tvDialogPositive?.text = positive
        return binding?.root
    }

    fun setTitleStr(title: String): CommonAlertDialog {
        this.title = title
        binding?.tvDialogTitle?.text = title
        return this
    }

    fun setContentStr(content: String): CommonAlertDialog {
        this.content = content
        binding?.tvDialogContent?.text = content
        return this
    }

    fun setPositiveStr(str: String): CommonAlertDialog {
        positive = str
        binding?.tvDialogPositive?.text = positive
        return this
    }

    fun setConfirmListener(confirmListener: AlertListener): CommonAlertDialog {
        listener = confirmListener
        return this
    }

    fun show(manager: FragmentManager) {
        // https://cloud.tencent.com/developer/article/1883592
        try {
            val temp: FragmentManager? = null
            // super 里有变量需要赋值
            super.show(temp!!, tag)
        } catch (e: Exception) {
        }

        val ft: FragmentTransaction = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }
}

interface AlertListener {
    fun onPositive()
}
