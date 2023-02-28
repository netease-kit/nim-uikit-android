/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.teamkit.ui.utils

object FilterUtils {

    @JvmStatic
    fun <T> filter(sourceList: List<T>, predicate: (T) -> Boolean): List<T> {
        return sourceList.filter {
            predicate(it)
        }
    }
}
