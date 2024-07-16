/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.utils

import androidx.annotation.StringRes
import com.netease.yunxin.kit.common.utils.XKitUtils

object ToastX {

    @JvmStatic
    fun showShortToast(content: String) {
        XKitUtils.getApplicationContext().let { ToastUtils.showShortToast(it, content) }
    }

    @JvmStatic
    fun showLongToast(content: String) {
        XKitUtils.getApplicationContext().let { ToastUtils.showLongToast(it, content) }
    }

    @JvmStatic
    fun showShortToast(@StringRes res: Int) {
        XKitUtils.getApplicationContext().let {
            ToastUtils.showShortToast(
                it,
                XKitUtils.getApplicationContext().resources.getString(res)
            )
        }
    }

    @JvmStatic
    fun showShortToast(@StringRes res: Int, vararg args: Any?) {
        XKitUtils.getApplicationContext().let {
            ToastUtils.showShortToast(
                it,
                XKitUtils.getApplicationContext().resources.getString(res),
                *args
            )
        }
    }

    @JvmStatic
    fun showShortToast(format: String?, vararg args: Any?) {
        XKitUtils.getApplicationContext().let {
            ToastUtils.showShortToast(
                it,
                format,
                args
            )
        }
    }

    @JvmStatic
    fun showLongToast(@StringRes res: Int) {
        XKitUtils.getApplicationContext().let {
            ToastUtils.showLongToast(it, XKitUtils.getApplicationContext().resources.getString(res))
        }
    }

    @JvmStatic
    fun showErrorToast(code: Int) {
        XKitUtils.getApplicationContext().let { ToastUtils.showErrorToast(it, code) }
    }
}
