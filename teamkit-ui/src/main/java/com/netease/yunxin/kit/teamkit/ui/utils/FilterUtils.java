// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FilterUtils {

    /**
     * 过滤列表
     * @param sourceList 源列表
     * @param predicate 过滤条件
     * @return 过滤后的列表
     */
    public static <T> List<T> filter(List<T> sourceList, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T item : sourceList) {
            if (predicate.test(item)) {
                result.add(item);
            }
        }
        return result;
    }
}
