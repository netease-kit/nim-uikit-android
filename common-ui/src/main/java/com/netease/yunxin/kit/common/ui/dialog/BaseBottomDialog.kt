/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.dialog

import android.view.Gravity
import android.view.ViewGroup
import com.netease.yunxin.kit.common.ui.R

abstract class BaseBottomDialog : BaseDialog() {

    override fun setStyle() {
        setStyle(STYLE_NORMAL, R.style.TransBottomDialogTheme)
    }

    override fun initParams() {
        val window = dialog?.window
        window?.let {
            it.setBackgroundDrawableResource(R.drawable.trans_corner_bottom_dialog_bg)
            val params = it.attributes
            params.gravity = Gravity.BOTTOM
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = params
        }
        isCancelable = true
    }
}
