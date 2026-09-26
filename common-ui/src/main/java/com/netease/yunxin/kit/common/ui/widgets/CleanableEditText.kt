/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.widgets

import android.content.Context
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.core.widget.addTextChangedListener
import com.netease.yunxin.kit.common.ui.R
import com.netease.yunxin.kit.common.ui.databinding.ClearableEditLayoutBinding

class CleanableEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : RoundFrameLayout(context, attrs, defStyleAttr) {

    var enableClear = true

    val binding: ClearableEditLayoutBinding by lazy {
        ClearableEditLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true)
    }

    init {

        if (attrs != null) {
            val at = getContext().theme.obtainStyledAttributes(
                attrs,
                R.styleable.ClearableEditText,
                0,
                0
            )
            val hintText = at.getText(R.styleable.ClearableEditText_hintText)
            binding.editText.hint = hintText
            binding.editText.filters =
                arrayOf(
                    InputFilter.LengthFilter(
                        at.getInt(R.styleable.ClearableEditText_android_maxLength, 20)
                    )
                )
            binding.editText.setLines(at.getInt(R.styleable.ClearableEditText_android_lines, 1))
            at.recycle()
        }

        binding.editText.addTextChangedListener(
            afterTextChanged = {
                if (TextUtils.isEmpty(it?.trim()) || !enableClear) {
                    binding.ivClear.visibility = GONE
                } else {
                    binding.ivClear.visibility = VISIBLE
                }
            }
        )

        binding.ivClear.setOnClickListener { binding.editText.text = null; }
    }

    fun setText(text: String?) {
        binding.editText.setText(text)
    }

    fun getText(): String? {
        return binding.editText.text?.toString()?.trim()
    }

    fun setEditable(enable: Boolean) {
        binding.editText.isEnabled = enable
    }

    fun setHint(@StringRes str: Int) {
        binding.editText.setHint(str)
    }

    fun addTextChangedListener(watcher: TextWatcher) {
        binding.editText.addTextChangedListener(watcher)
    }

    fun setMaxLength(length: Int) {
        binding.editText.filters =
            arrayOf(InputFilter.LengthFilter(length))
    }

    fun setInputType(inputType: Int) {
        binding.editText.inputType = inputType
    }

    fun setFilter(filters: Array<InputFilter>) {
        binding.editText.filters = filters
    }
}
