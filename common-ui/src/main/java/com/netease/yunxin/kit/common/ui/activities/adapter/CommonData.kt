/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.activities.adapter

class CommonData<T>(
    var type: Int = -1,
    var data: T? = null,
    var event: Event? = null
) {

    class Event(var eventType: String, var url: String)
}
