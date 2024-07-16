/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
@file:JvmName("TransHelper")

package com.netease.yunxin.kit.common.ui.photo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.core.app.ActivityCompat

private val transferMap = HashMap<Int, TransferHelperParam?>()

const val KEY_PERMISSION_RESULT_GRANTED = "permissions_result_granted"
const val KEY_PERMISSION_RESULT_DENIED = "permissions_result_denied"
const val KEY_PERMISSION_RESULT_DENIED_FOREVER = "permissions_result_denied_forever"

fun launchTask(
    context: Context,
    requestId: Int,
    action: (Activity, Int) -> Unit,
    result: (ResultInfo<Intent>) -> Unit
) {
    transferMap[requestId] = TransferHelperParam(action, result)
    TransferHelperActivity.launch(context, requestId)
}

fun removeTransferKey(requestId: Int) {
    transferMap.remove(requestId)
}

internal class TransferHelperParam(
    val action: ((Activity, Int) -> Unit)? = null,
    val result: ((ResultInfo<Intent>) -> Unit)? = null
)

class TransferHelperActivity : Activity() {
    private val requestId: Int by lazy {
        intent.getIntExtra(PARAM_KEY_FOR_REQUEST_ID, -1)
    }

    companion object {
        const val PARAM_KEY_FOR_REQUEST_ID = "transfer_request_id"

        fun launch(context: Context, requestId: Int) {
            Intent(context, TransferHelperActivity::class.java).apply {
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                putExtra(PARAM_KEY_FOR_REQUEST_ID, requestId)
                context.startActivity(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        val param = transferMap[requestId]
        if (param?.action == null) {
            finish()
        } else {
            param.action.invoke(this, requestId)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            val param = transferMap.remove(requestId)
            param?.result?.invoke(
                ResultInfo(data, resultCode == RESULT_OK, msg = ErrorMsg(resultCode))
            )
        } finally {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            val param = transferMap.remove(requestId)
            val callbackResult = param?.result ?: return
            permissions.toList()
            val size = permissions.size
            val grantedList = ArrayList<String>()
            val deniedList = ArrayList<String>()
            val deniedForeverList = ArrayList<String>()
            for (index in 0 until size) {
                val permission = permissions[index]
                when (grantResults[index]) {
                    PERMISSION_GRANTED -> grantedList.add(permission)
                    PERMISSION_DENIED -> {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permission
                            )
                        ) {
                            deniedForeverList.add(permission)
                        } else {
                            deniedList.add(permission)
                        }
                    }
                }
            }

            callbackResult.invoke(
                ResultInfo(
                    Intent()
                        .apply {
                            putStringArrayListExtra(KEY_PERMISSION_RESULT_GRANTED, grantedList)
                            putStringArrayListExtra(KEY_PERMISSION_RESULT_DENIED, deniedList)
                            putStringArrayListExtra(
                                KEY_PERMISSION_RESULT_DENIED_FOREVER,
                                deniedForeverList
                            )
                        },
                    true
                )
            )
        } finally {
            finish()
        }
    }
}
