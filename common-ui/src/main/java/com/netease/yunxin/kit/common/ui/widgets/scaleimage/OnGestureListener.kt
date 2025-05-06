/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.widgets.scaleimage

interface OnGestureListener {
    fun onDrag(dx: Float, dy: Float)

    fun onFling(
        startX: Float, startY: Float, velocityX: Float,
        velocityY: Float
    )

    fun onScale(scaleFactor: Float, focusX: Float, focusY: Float)

    fun onScale(scaleFactor: Float, focusX: Float, focusY: Float, dx: Float, dy: Float)
}
