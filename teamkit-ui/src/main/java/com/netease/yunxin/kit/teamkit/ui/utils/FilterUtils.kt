/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.teamkit.ui.utils

// 过滤器工具类
object FilterUtils {

    /**
     * 过滤列表
     *
     * @param sourceList 源列表
     * @param predicate 过滤条件
     * @return 过滤后的列表
     */
    @JvmStatic
    fun <T> filter(sourceList: List<T>, predicate: (T) -> Boolean): List<T> {
        return sourceList.filter {
            predicate(it)
        }
    }
}
