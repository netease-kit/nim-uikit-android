/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.widgets

import android.graphics.drawable.GradientDrawable

class ShapeDrawable : GradientDrawable() {

    class Builder {
        private val shapeDrawable = ShapeDrawable()

        fun setSolid(color: Int): Builder {
            shapeDrawable.setColor(color)
            return this
        }

        fun setStroke(width: Int, color: Int): Builder {
            shapeDrawable.setStroke(width, color)
            return this
        }

        fun setStrokeDash(width: Int, color: Int, dashWidth: Float, dashGap: Float): Builder {
            shapeDrawable.setStroke(width, color, dashWidth, dashGap)
            return this
        }

        fun setRadii(radii: FloatArray): Builder {
            shapeDrawable.cornerRadii = radii
            return this
        }

        fun setRadius(radius: Float): Builder {
            shapeDrawable.cornerRadius = radius
            return this
        }

        fun build(): ShapeDrawable = shapeDrawable
    }
}
