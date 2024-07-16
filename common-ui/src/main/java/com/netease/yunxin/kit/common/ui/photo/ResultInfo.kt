/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.photo

/**
 * The result info of uniform
 */
class ResultInfo<T> @JvmOverloads constructor(
    val value: T? = null, // result of value
    val success: Boolean = true, // result of execution, true is successful otherwise failing.
    val msg: ErrorMsg? = null // error message
) {
    override fun toString(): String {
        return "ResultInfo(value=$value, success=$success, msg=$msg)"
    }
}

/**
 * The error info of uniform, was included in [ResultInfo]
 */
class ErrorMsg @JvmOverloads constructor(
    val code: Int, // code of error
    val message: String = "", // message
    val exception: Throwable? = null // exception of result
) {
    override fun toString(): String {
        return "ErrorMsg(code=$code, message='$message', exception=$exception)"
    }
}

/**
 * observer of result
 */
interface ResultObserver<T> {

    /**
     * can call this method to inform user the result.
     *
     * @param result result of execution.
     */
    fun onResult(result: ResultInfo<T>)
}
