/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.netease.yunxin.kit.common.ui.R

/**
 * sample
 * var loadingDialog = LoadingDialog(this)
 * loadingDialog.show()
 * loadingDialog.dismiss()
 */

class LoadingDialog(context: Context) : AlertDialog(context) {
    private var loadingText: CharSequence = ""
    override fun show() {
        super.show()
        this.window!!.setContentView(R.layout.loading_dialog_layout)
        this.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window!!.setDimAmount(0f)
        this.setCancelable(false)
        this.setCanceledOnTouchOutside(false)
        val loadingTextView = findViewById<TextView>(R.id.tv_desc)
        if (!TextUtils.isEmpty(loadingText)) {
            loadingTextView.text = loadingText
            loadingTextView.visibility = View.VISIBLE
        } else {
            loadingTextView.visibility = View.GONE
        }
    }

    fun setLoadingText(loadingText: CharSequence) {
        this.loadingText = loadingText
    }
}
