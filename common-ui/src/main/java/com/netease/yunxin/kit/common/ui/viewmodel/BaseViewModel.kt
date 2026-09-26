/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.viewmodel

import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {

    open fun onDestroy() {
    }
}
