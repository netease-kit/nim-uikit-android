/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout

/**
 * when set click Listener for it's child view
 * this LinearLayout will be available for it's longClick
 * @constructor
 *
 * @param context
 * @param attrs
 * @param defStyleAttr
 */
class LongClickableFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var mLongPressTriggered: Boolean = false

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val mask = ev.actionMasked
        if (mask == MotionEvent.ACTION_DOWN) {
            mLongPressTriggered = false
        }
        val handle = super.dispatchTouchEvent(ev)
        if (mask == MotionEvent.ACTION_DOWN && isLongClickable) {
            scheduleLongPress()
        }
        if (ev.actionMasked == MotionEvent.ACTION_UP ||
            ev.actionMasked == MotionEvent.ACTION_CANCEL
        ) {
            removeLongPress()
        }
        return handle
    }

    private fun scheduleLongPress() {
        postDelayed(longClickRunnable, ViewConfiguration.getLongPressTimeout().toLong())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mLongPressTriggered && event.actionMasked == MotionEvent.ACTION_UP) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    private val longClickRunnable = Runnable {
        mLongPressTriggered = performLongClick()
    }

    private fun removeLongPress() {
        removeCallbacks(longClickRunnable)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (mLongPressTriggered && ev.actionMasked == MotionEvent.ACTION_UP) {
            true
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }
}
