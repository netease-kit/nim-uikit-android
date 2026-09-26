/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.dialog

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.netease.yunxin.kit.common.ui.R
import com.netease.yunxin.kit.common.ui.action.ActionItem
import com.netease.yunxin.kit.common.ui.databinding.ListAlertDialogItemBinding
import com.netease.yunxin.kit.common.ui.databinding.ListAlertDialogLayoutBinding

class ListAlertDialog : BaseDialog(), View.OnClickListener {

    val TAG = "ListAlertDialog"
    var viewBinding: ListAlertDialogLayoutBinding? = null
    var title: String? = null
    var showTitle: Int = View.VISIBLE
    var itemList = arrayListOf<ActionItem>()
    var itemClickListener: AlertItemClickListener? = null
    var dialogWidth: Float = -1f

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        viewBinding = ListAlertDialogLayoutBinding.inflate(inflater, container, false)
        viewBinding?.tvDialogTitle?.text = title
        viewBinding?.tvDialogTitle?.visibility = showTitle
        loadContent(inflater)
        return viewBinding?.root
    }

    fun setTitleStr(title: String): ListAlertDialog {
        this.title = title
        viewBinding?.tvDialogTitle?.text = title
        return this
    }

    fun setTitleVisibility(visible: Int): ListAlertDialog {
        this.showTitle = visible
        viewBinding?.tvDialogTitle?.visibility = visible
        return this
    }

    fun setWidth(width: Float) {
        dialogWidth = width
    }

    fun setContent(items: List<ActionItem>) {
        this.itemList.clear()
        this.itemList.addAll(items)
    }

    private fun loadContent(inflater: LayoutInflater) {
        viewBinding?.dialogContentLl?.removeAllViews()
        for (index in 0 until itemList.size) {
            val item = itemList[index]
            val itemView = ListAlertDialogItemBinding.inflate(
                inflater,
                viewBinding?.dialogContentLl,
                false
            )
            itemView.root.setOnClickListener(this)
            itemView.listDialogContentTv.text = getText(item.titleResId)
            itemView.root.tag = item.action
            if (index == (itemList.size - 1)) {
                itemView.lineBottom.visibility = View.GONE
            }
            viewBinding?.dialogContentLl?.addView(itemView.root)
        }
    }

    override fun initParams() {
        val window = dialog?.window
        window?.let {
            it.setBackgroundDrawableResource(R.drawable.trans_corner_bottom_dialog_bg)
            val params = it.attributes
            params.gravity = Gravity.CENTER
            if (dialogWidth > 0) {
                params.width = dialogWidth.toInt()
            } else {
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = params
        }
        isCancelable = true
    }

    override fun onClick(v: View?) {
        if (v != null && v.tag is String) {
            itemClickListener?.onClick(v.tag.toString())
        }
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, TAG)
    }
}

interface AlertItemClickListener {
    fun onClick(action: String)
}
