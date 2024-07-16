/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.utils

interface CommonCallback<T> {
    /**
     * success call
     * @param param result info
     */
    fun onSuccess(param: T?)

    /**
     * failed call
     * @param code error code。
     */
    fun onFailed(code: Int)

    /**
     * exception call
     * @param exception exception message
     */
    fun onException(exception: Throwable?)
}
