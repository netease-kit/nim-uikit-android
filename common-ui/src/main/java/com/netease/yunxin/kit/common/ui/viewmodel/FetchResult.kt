/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.viewmodel

import android.content.Context
import android.text.TextUtils

/**
 * Result contain status and type ,used to interact with activity or fragment
 *
 */
class FetchResult<T>(var loadStatus: LoadStatus?) {
    var type = FetchType.Init
    var typeIndex = 0
    var data: T? = null
    var error: ErrorMsg? = null
    var extraInfo: Any? = null

    constructor(loadStatus: LoadStatus?, data: T?) : this(loadStatus) {
        this.data = data
    }
    constructor(type: FetchType) : this(LoadStatus.Success) {
        this.type = type
    }

    constructor(data: T?) : this(LoadStatus.Success) {
        this.data = data
    }

    constructor(type: FetchType, data: T?) : this(LoadStatus.Success) {
        this.type = type
        this.data = data
    }

    constructor(code: Int, msg: String?) : this(LoadStatus.Error) {
        error = ErrorMsg(code, msg)
    }

    fun setError(code: Int, msg: String?) {
        loadStatus = LoadStatus.Error
        error = ErrorMsg(code, msg)
    }

    fun setError(code: Int, msgRes: Int) {
        loadStatus = LoadStatus.Error
        error = ErrorMsg(code, msgRes)
    }

    fun setFetchType(type: FetchType) {
        this.type = type
    }

    fun isSuccess(): Boolean {
        return loadStatus == LoadStatus.Success
    }

    fun setStatus(loadStatus: LoadStatus?) {
        this.loadStatus = loadStatus
    }

    fun errorMsg(): ErrorMsg? {
        return error
    }

    fun getErrorMsg(context: Context): String? {
        if (error != null) {
            if (!TextUtils.isEmpty(error?.msg)) {
                return error?.msg
            }
            if (error?.res != null) {
                return context.resources.getString(error!!.res)
            }
        }
        return null
    }

    enum class FetchType {
        Init, Add, Update, Remove
    }

    class ErrorMsg {
        var code: Int
        var res = 0
        var msg: String? = null

        constructor(code: Int, errorRes: Int) {
            this.code = code
            res = errorRes
        }

        constructor(code: Int, msg: String?) {
            this.code = code
            this.msg = msg
        }
    }
}
