/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import com.netease.yunxin.kit.common.ui.databinding.CommonTopPopupWindowLayoutBinding

class TopPopupWindow(context: Context) : PopupWindow(context) {

    private var viewBinding: CommonTopPopupWindowLayoutBinding? = null
    init {
        viewBinding = CommonTopPopupWindowLayoutBinding.inflate(LayoutInflater.from(context))
        contentView = viewBinding?.root
    }
    constructor(context: Context, titleRes: Int, contentRes: Int) : this(context) {
        viewBinding?.commonPopTitle?.setText(titleRes)
        viewBinding?.commonPopContent?.setText(contentRes)
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isTouchable = true
        isOutsideTouchable = true
        setBackgroundDrawable(ColorDrawable(0x00000000))
    }
}
