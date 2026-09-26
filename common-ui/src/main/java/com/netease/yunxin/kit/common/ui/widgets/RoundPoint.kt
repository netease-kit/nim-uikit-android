/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.widgets

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.netease.yunxin.kit.common.ui.R
import com.netease.yunxin.kit.common.utils.SizeUtils

class RoundPoint @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val DEFAULT_RING_WIDTH = 1f
    }

    private var mPaint: Paint = Paint()

    private var mRectF: RectF? = null

    private var ringWidth: Float = SizeUtils.dp2px(DEFAULT_RING_WIDTH).toFloat()

    private var centerX: Float = 0f

    private var centerY: Float = 0f

    private var process: Float = 0f

    init {
        mPaint.isAntiAlias = true
        attrs?.let {
            val t: TypedArray = context.theme.obtainStyledAttributes(
                it,
                R.styleable.RoundPoint,
                0,
                0
            )
            ringWidth = t.getDimension(
                R.styleable.RoundPoint_ringWidth,
                SizeUtils.dp2px(DEFAULT_RING_WIDTH).toFloat()
            )
            mPaint.color = t.getColor(R.styleable.RoundPoint_viewColor, Color.BLUE)
        }
    }

    fun setColor(@ColorInt color: Int) {
        mPaint.color = color
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        centerX = (measuredWidth / 2).toFloat()
        centerY = (measuredHeight / 2).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawRing(canvas)

        drawProcess(canvas)
    }

    private fun drawRing(canvas: Canvas?) {
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = ringWidth
        canvas?.drawCircle(centerX, centerY, width / 2 - ringWidth / 2, mPaint)
    }

    private fun drawProcess(canvas: Canvas?) {
        if (mRectF == null) {
            mRectF = RectF(
                ringWidth / 2,
                ringWidth / 2,
                centerX * 2 - ringWidth / 2,
                centerY * 2 - ringWidth / 2
            )
        }
        mPaint.style = Paint.Style.FILL
        canvas?.drawArc(mRectF!!, -90f, 360 * process, true, mPaint)
    }

    fun setProcess(process: Float) {
        this.process = process
        invalidate()
    }
}
