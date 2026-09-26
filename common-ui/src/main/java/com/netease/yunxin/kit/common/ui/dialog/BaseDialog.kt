/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.netease.yunxin.kit.common.ui.R

abstract class BaseDialog() : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle()
    }

    protected open fun setStyle() {
        setStyle(STYLE_NORMAL, R.style.TransCommonDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return getRootView(inflater, container)
    }

    override fun onStart() {
        super.onStart()
        initParams()
        initData()
    }

    protected abstract fun getRootView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): View?

    protected open fun initData() {}
    protected open fun initParams() {
        val window = dialog?.window
        window?.let {
            it.setBackgroundDrawableResource(R.drawable.trans_corner_bottom_dialog_bg)
            val params = it.attributes
            params.gravity = Gravity.CENTER
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = params
        }
        isCancelable = true
    }
}
