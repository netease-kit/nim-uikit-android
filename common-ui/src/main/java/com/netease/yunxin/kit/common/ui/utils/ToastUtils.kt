/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.utils

import android.content.Context
import android.widget.Toast
import com.netease.yunxin.kit.common.ui.R

object ToastUtils {
    var toast: Toast? = null

    private const val ERROR_CODE_OVER_TIME = 408

    const val ERROR_CODE_NO_PERMISSION = 403

    fun showShortToast(context: Context, content: String?) {
        if (content == null) return
        MainTask.getInstance().runOnUIThread {
            toast?.cancel()
            toast = null
            toast = Toast.makeText(context.applicationContext, content, Toast.LENGTH_SHORT)
            toast?.show()
        }
    }

    fun showShortToast(context: Context, format: String?, vararg args: Any?) {
        if (format == null) return
        MainTask.getInstance().runOnUIThread {
            toast?.cancel()
            toast = null
            toast = Toast.makeText(
                context.applicationContext,
                String.format(format, *args),
                Toast.LENGTH_SHORT
            )
            toast?.show()
        }
    }

    fun showLongToast(context: Context, content: String?) {
        if (content == null) return
        MainTask.getInstance().runOnUIThread {
            toast?.cancel()
            toast = null
            toast = Toast.makeText(context.applicationContext, content, Toast.LENGTH_LONG)
            toast?.show()
        }
    }

    fun showErrorToast(context: Context, errorCode: Int) {
        MainTask.getInstance().runOnUIThread {
            toast?.cancel()
            toast = null
            toast = when (errorCode) {
                ERROR_CODE_OVER_TIME -> Toast.makeText(
                    context.applicationContext,
                    context.getText(R.string.error_over_time),
                    Toast.LENGTH_LONG
                )
                ERROR_CODE_NO_PERMISSION -> Toast.makeText(
                    context.applicationContext,
                    context.getText(R.string.error_no_permission),
                    Toast.LENGTH_LONG
                )
                else -> Toast.makeText(
                    context.applicationContext,
                    context.getText(R.string.error_default),
                    Toast.LENGTH_LONG
                )
            }
            toast?.show()
        }
    }
}
