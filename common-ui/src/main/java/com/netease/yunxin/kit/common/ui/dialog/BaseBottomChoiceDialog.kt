/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.netease.yunxin.kit.common.ui.R
import com.netease.yunxin.kit.common.ui.action.ActionItem
import com.netease.yunxin.kit.common.utils.SizeUtils

abstract class BaseBottomChoiceDialog : Dialog {

    protected var cancelTv: TextView? = null
    var containerView: ViewGroup? = null
    var rootView: View? = null
    var onChoiceListener: OnChoiceListener? = null
    var mActionList: ArrayList<ActionItem>? = null

    constructor(context: Context, list: ArrayList<ActionItem>) : super(
        context,
        R.style.TransBottomDialogTheme
    ) {
        mActionList = list
        this.rootView = initViewAndGetRootView()
    }

    protected abstract fun initViewAndGetRootView(): View?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(rootView!!)
        window?.let {
            it.setBackgroundDrawableResource(R.drawable.trans_corner_bottom_dialog_bg)
            val params = it.attributes
            params.gravity = Gravity.BOTTOM
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = params
        }
        setCanceledOnTouchOutside(true)
        initViews()
    }

    private fun initViews() {
        handleContainer()
        cancelTv?.setOnClickListener {
            dismiss()
            onChoiceListener?.onCancel()
        }
    }

    private fun handleContainer() {
        containerView?.removeAllViews()
        mActionList?.forEachIndexed { index, it ->
            addItemView(index, it)
        }
    }

    fun addItemView(index: Int, actionItem: ActionItem) {
        if (index != 0) {
            val divider = View(context)
            divider.setBackgroundColor(ContextCompat.getColor(context, R.color.color_dedede))
            containerView?.addView(
                divider,
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            )
        }
        val tvItem = TextView(context)
        tvItem.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                SizeUtils.dp2px(42f)
            )
        tvItem.gravity = Gravity.CENTER
        tvItem.text = context.getString(actionItem.titleResId)
        tvItem.setTextColor(ContextCompat.getColor(context, actionItem.titleColorResId))
        tvItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        tvItem.setOnClickListener { v ->
            dismiss()
            actionItem.onClick(v)
            onChoiceListener?.onChoice(actionItem.action)
        }
        containerView?.addView(tvItem)
    }

    fun showCancelButton(show: Boolean): BaseBottomChoiceDialog {
        cancelTv?.visibility = if (show) View.VISIBLE else View.GONE
        return this
    }

    interface OnChoiceListener {
        fun onChoice(type: String)
        fun onCancel()
    }
}
