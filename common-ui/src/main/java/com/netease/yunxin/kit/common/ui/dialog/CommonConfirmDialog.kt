/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.dialog

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.netease.yunxin.kit.common.ui.R

/**
 *
 */
open class CommonConfirmDialog protected constructor(
    context: Context,
    themeResId: Int = R.style.TransCommonDialogTheme
) : AlertDialog(
    context,
    themeResId
) {
    interface Callback {
        fun result(boolean: Boolean?)
    }

    private var titleView: TextView? = null
    private var messageView: TextView? = null
    private var cancelView: TextView? = null
    private var okView: TextView? = null
    private var title: CharSequence? = null
    private var message: CharSequence? = null
    private var cancel: CharSequence? = null
    private var ok: CharSequence? = null
    private var callback: Callback? = null
    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        loadLayout()
        initView()
        super.onCreate(savedInstanceState)
    }

    /**
     * load layout
     */
    private fun loadLayout() {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view = findViews(inflater)
        setView(view)
        setViewsListener()
    }

    private fun findViews(inflater: LayoutInflater): View {
        val view: View = inflater.inflate(getLayout(), null)
        titleView = view.findViewById(R.id.title)
        messageView = view.findViewById(R.id.message)
        cancelView = view.findViewById(R.id.cancel)
        okView = view.findViewById(R.id.ok)
        return view
    }

    open fun getLayout(): Int {
        return R.layout.common_confirm_dialog_layout
    }

    private fun setViewsListener() {
        okView?.setOnClickListener {
            if (callback != null) {
                callback!!.result(true)
            }
            dismiss()
        }
        cancelView?.setOnClickListener {
            if (callback != null) {
                callback!!.result(false)
            }
            dismiss()
        }
        messageView?.movementMethod = LinkMovementMethod.getInstance()
    }

    /**
     * init view state
     */
    private fun initView() {
        setTitle(title)
        setMessage(message!!)
        setCancel(cancel)
        setOk(ok)
    }

    fun setCancel(cancel: CharSequence?) {
        if (cancelView != null) {
            cancelView!!.text = if (TextUtils.isEmpty(cancel)) {
                context.getString(
                    R.string.dialog_disagree
                )
            } else {
                cancel
            }
        } else {
            this.cancel = cancel
        }
    }

    fun hideCancel() {
        if (cancelView != null) {
            cancelView!!.visibility = View.GONE
        }
    }

    fun setOk(ok: CharSequence?) {
        if (okView != null) {
            okView!!.text = if (TextUtils.isEmpty(ok)) context.getString(R.string.dialog_read_agree) else ok
        } else {
            this.ok = ok
        }
    }

    override fun setTitle(title: CharSequence?) {
        if (titleView != null) {
            titleView!!.text = title
            titleView!!.visibility = if (TextUtils.isEmpty(title)) View.GONE else View.VISIBLE
        } else {
            this.title = title
        }
    }

    override fun setMessage(message: CharSequence?) {
        if (messageView != null) {
            messageView!!.text = message
            messageView!!.visibility = if (TextUtils.isEmpty(message)) View.GONE else View.VISIBLE
        } else {
            this.message = message
        }
    }

    companion object {
        fun show(
            context: Context,
            title: String?,
            message: CharSequence,
            cancelable: Boolean,
            cancelOnTouchOutside: Boolean,
            callback: Callback?
        ): CommonConfirmDialog {
            return show(
                context,
                title,
                message,
                null,
                null,
                cancelable,
                cancelOnTouchOutside,
                callback
            )
        }

        fun show(
            context: Context,
            title: String?,
            message: CharSequence,
            ok: CharSequence?,
            cancelable: Boolean,
            cancelOnTouchOutside: Boolean,
            callback: Callback?
        ): CommonConfirmDialog {
            val dialog = CommonConfirmDialog(context)
            dialog.setTitle(title)
            dialog.setMessage(message)
            dialog.setCancelable(cancelable)
            dialog.setOk(ok)
            dialog.setCanceledOnTouchOutside(cancelOnTouchOutside)
            dialog.setCallback(callback)
            dialog.show()
            return dialog
        }

        fun show(
            context: Context,
            title: String?,
            message: CharSequence,
            cancel: CharSequence?,
            ok: CharSequence?,
            cancelable: Boolean,
            cancelOnTouchOutside: Boolean,
            callback: Callback?
        ): CommonConfirmDialog {
            val dialog = CommonConfirmDialog(context)
            dialog.setTitle(title)
            dialog.setMessage(message)
            dialog.setCancelable(cancelable)
            dialog.setCancel(cancel)
            dialog.setOk(ok)
            dialog.setCanceledOnTouchOutside(cancelOnTouchOutside)
            dialog.setCallback(callback)
            dialog.show()
            return dialog
        }
    }
}
