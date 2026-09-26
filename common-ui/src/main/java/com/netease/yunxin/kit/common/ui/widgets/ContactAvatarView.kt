/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.netease.yunxin.kit.common.ui.widgets

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.netease.yunxin.kit.common.ui.R
import com.netease.yunxin.kit.common.ui.databinding.AvatarViewLayoutBinding
import java.util.Random
import kotlin.math.abs

class ContactAvatarView : FrameLayout {
    private var viewBinding: AvatarViewLayoutBinding? = null
    private var cornersRadius: Float = 0f

    constructor(context: Context) : super(context) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(attrs)
    }

    private fun initView(attrs: AttributeSet?) {
        val layoutInflater = LayoutInflater.from(context)
        viewBinding = AvatarViewLayoutBinding.inflate(layoutInflater, this, true)
        if (attrs != null) {
            val t: TypedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ContactAvatarView,
                0,
                0
            )
            val textSize = t.getDimension(
                R.styleable.ContactAvatarView_avatarTextSize,
                context.resources.getDimension(R.dimen.text_size_14)
            )
            val corners = t.getDimension(
                R.styleable.ContactAvatarView_avatarCorner,
                context.resources.getDimension(R.dimen.dimen_30_dp)
            )
            setCornerRadius(corners)
            viewBinding!!.tvAvatar.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            t.recycle()
        }
    }

    fun setTextSize(textSize: Int) {
        viewBinding!!.tvAvatar.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
    }

    fun setImageScaleType(scaleType: ImageView.ScaleType) {
        viewBinding!!.ivAvatar.scaleType = scaleType
    }

    @JvmOverloads
    fun setData(avatar: String?, name: String, hashCode: Int = 0) {
        if (context == null) {
            return
        }
        if (!TextUtils.isEmpty(avatar)) {
            viewBinding!!.flAvatar.visibility = GONE
            var requestBuilder = Glide.with(context.applicationContext)
                .load(avatar).addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        loadTvAvatar(name, hashCode)
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
            if (cornersRadius > 0) {
                requestBuilder = requestBuilder.transform(RoundedCorners(cornersRadius.toInt()))
            }
            requestBuilder.into(viewBinding!!.ivAvatar)
        } else {
            loadTvAvatar(name, hashCode)
        }
    }

    @JvmOverloads
    fun setData(avatarResId: Int, name: String, hashCode: Int = 0) {
        if (context == null) {
            return
        }
        viewBinding!!.flAvatar.visibility = GONE
        Glide.with(context)
            .load(avatarResId).addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    loadTvAvatar(name, hashCode)
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(viewBinding!!.ivAvatar)
    }

    private fun loadTvAvatar(name: String, hashCode: Int) {
        viewBinding!!.flAvatar.visibility = VISIBLE
        val pos = if (hashCode == 0) {
            val random = Random()
            random.nextInt(SIZE)
        } else {
            abs(hashCode) % SIZE
        }
        viewBinding!!.flAvatar.setBackgroundResource(bgRes[abs(pos)])
        if (name.length <= AVATAR_NAME_LEN) {
            viewBinding!!.tvAvatar.text = name
        } else {
            viewBinding!!.tvAvatar.text =
                name.substring(name.length - AVATAR_NAME_LEN)
        }
    }

    fun setCertainAvatar(@DrawableRes drawableInt: Int) {
        if (context == null) {
            return
        }
        var requestBuilder = Glide.with(context).load(drawableInt)
        if (cornersRadius > 0) {
            requestBuilder = requestBuilder.transform(RoundedCorners(cornersRadius.toInt()))
        }
        requestBuilder.into(viewBinding!!.ivAvatar)
    }

    fun setCornerRadius(radius: Float) {
        cornersRadius = radius
        viewBinding!!.flAvatar.setRadius(radius)
        viewBinding!!.flIvAvatar.setRadius(radius)
    }

    companion object {
        private const val SIZE = 7
        private val bgRes = intArrayOf(
            R.drawable.default_avatar_bg_0,
            R.drawable.default_avatar_bg_1,
            R.drawable.default_avatar_bg_2,
            R.drawable.default_avatar_bg_3,
            R.drawable.default_avatar_bg_4,
            R.drawable.default_avatar_bg_5,
            R.drawable.default_avatar_bg_6
        )

        /**
         * default avatar show sub-name length
         */
        private const val AVATAR_NAME_LEN = 2
    }
}
