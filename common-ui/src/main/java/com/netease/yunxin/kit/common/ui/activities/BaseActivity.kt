/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.activities

import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.netease.yunxin.kit.common.ui.dialog.LoadingDialog

open class BaseActivity : AppCompatActivity() {

    private var alertDialog: LoadingDialog? = null

    open fun changeStatusBarColor(@ColorRes colorResId: Int) {
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, colorResId)
    }

    open fun showLoading() {
        if (alertDialog == null) {
            alertDialog = LoadingDialog(this)
        }
        alertDialog?.show()
    }

    open fun dismissLoading() {
        if (null != alertDialog && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
    }
}
