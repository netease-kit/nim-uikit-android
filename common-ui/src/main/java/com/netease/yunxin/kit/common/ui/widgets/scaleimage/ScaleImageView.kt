/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.widgets.scaleimage

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector
import androidx.appcompat.widget.AppCompatImageView

class ScaleImageView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private lateinit var attache: ScaleAttache
    private var pendingScaleType: ScaleType? = null
    init {
        if (attrs != null) {
            attache = ScaleAttache(this)
            // We always pose as a Matrix scale type, though we can change to another scale type
            // via the attacher
            super.setScaleType(ScaleType.MATRIX)
            // apply the previously applied scale type
            if (pendingScaleType != null) {
                setScaleType(pendingScaleType!!)
                pendingScaleType = null
            }
        }
    }

    /**
     * Get the current [PhotoViewAttacher] for this view. Be wary of holding on to references
     * to this attacher, as it has a reference to this view, which, if a reference is held in the
     * wrong place, can cause memory leaks.
     *
     * @return the attacher.
     */
    fun getAttache(): ScaleAttache {
        return attache
    }

    override fun getScaleType(): ScaleType {
        return attache.getScaleType()
    }

    override fun getImageMatrix(): Matrix {
        return attache.getImageMatrix()
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        attache.setOnLongClickListener(l)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        attache.setOnClickListener(l)
    }

    override fun setScaleType(scaleType: ScaleType) {
        if (attache == null) {
            pendingScaleType = scaleType
        } else {
            attache.setScaleType(scaleType)
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        // setImageBitmap calls through to this method
        if (attache != null) {
            attache.update()
        }
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        if (attache != null) {
            attache.update()
        }
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        if (attache != null) {
            attache.update()
        }
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        if (changed) {
            attache.update()
        }
        return changed
    }

    fun setRotationTo(rotationDegree: Float) {
        attache.setRotationTo(rotationDegree)
    }

    fun setRotationBy(rotationDegree: Float) {
        attache.setRotationBy(rotationDegree)
    }

    fun isZoomable(): Boolean {
        return attache.isZoomable()
    }

    fun setZoomable(zoomable: Boolean) {
        attache.setZoomable(zoomable)
    }

    fun getDisplayRect(): RectF? {
        return attache.getDisplayRect()
    }

    fun getDisplayMatrix(matrix: Matrix) {
        attache.getDisplayMatrix(matrix)
    }

    fun setDisplayMatrix(finalRectangle: Matrix): Boolean {
        return attache.setDisplayMatrix(finalRectangle)
    }

    fun getSuppMatrix(matrix: Matrix) {
        attache.getSuppMatrix(matrix)
    }

    fun setSuppMatrix(matrix: Matrix?): Boolean {
        return attache.setDisplayMatrix(matrix)
    }

    fun getMinimumScale(): Float {
        return attache.getMinimumScale()
    }

    fun getMediumScale(): Float {
        return attache.getMediumScale()
    }

    fun getMaximumScale(): Float {
        return attache.getMaximumScale()
    }

    fun getScale(): Float {
        return attache.getScale()
    }

    fun setAllowParentInterceptOnEdge(allow: Boolean) {
        attache.setAllowParentInterceptOnEdge(allow)
    }

    fun setMinimumScale(minimumScale: Float) {
        attache.setMinimumScale(minimumScale)
    }

    fun setMediumScale(mediumScale: Float) {
        attache.setMediumScale(mediumScale)
    }

    fun setMaximumScale(maximumScale: Float) {
        attache.setMaximumScale(maximumScale)
    }

    fun setScaleLevels(minimumScale: Float, mediumScale: Float, maximumScale: Float) {
        attache.setScaleLevels(minimumScale, mediumScale, maximumScale)
    }

    fun setOnMatrixChangeListener(listener: OnMatrixChangedListener?) {
        attache.setOnMatrixChangeListener(listener)
    }

    fun setOnPhotoTapListener(listener: OnPhotoTapListener?) {
        attache.setOnPhotoTapListener(listener)
    }

    fun setOnOutsidePhotoTapListener(listener: OnOutsidePhotoTapListener?) {
        attache.setOnOutsidePhotoTapListener(listener)
    }

    fun setOnViewTapListener(listener: OnViewTapListener?) {
        attache.setOnViewTapListener(listener)
    }

    fun setOnViewDragListener(listener: OnViewDragListener?) {
        attache.setOnViewDragListener(listener)
    }

    fun setScale(scale: Float) {
        attache.setScale(scale)
    }

    fun setScale(scale: Float, animate: Boolean) {
        attache.setScale(scale, animate)
    }

    fun setScale(scale: Float, focalX: Float, focalY: Float, animate: Boolean) {
        attache.setScale(scale, focalX, focalY, animate)
    }

    fun setZoomTransitionDuration(milliseconds: Int) {
        attache.setZoomTransitionDuration(milliseconds)
    }

    fun setOnDoubleTapListener(onDoubleTapListener: GestureDetector.OnDoubleTapListener?) {
        attache.setOnDoubleTapListener(onDoubleTapListener)
    }

    fun setOnScaleChangeListener(onScaleChangedListener: OnScaleChangedListener?) {
        attache.setOnScaleChangeListener(onScaleChangedListener)
    }

    fun setOnSingleFlingListener(onSingleFlingListener: OnSingleFlingListener?) {
        attache.setOnSingleFlingListener(onSingleFlingListener)
    }
}
