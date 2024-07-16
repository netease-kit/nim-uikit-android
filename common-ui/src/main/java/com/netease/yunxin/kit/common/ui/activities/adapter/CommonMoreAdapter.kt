/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.activities.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseMoreViewHolder
import kotlin.collections.ArrayList

const val DEFAULT_SHOW_SIZE = 5

abstract class CommonMoreAdapter<T, VB : ViewBinding> :
    RecyclerView.Adapter<BaseMoreViewHolder<T, VB>>() {

    open var defaultShow = DEFAULT_SHOW_SIZE

    var itemClickListener: ItemClickListener<T>? = null

    val dataList by lazy {
        ArrayList<T>()
    }

    var showSub: Boolean = false

    var showAll: Boolean = true

    override fun onBindViewHolder(holder: BaseMoreViewHolder<T, VB>, position: Int) {
        val item = dataList[position]
        holder.bind(item)
        itemClickListener?.let {
            holder.itemView.setOnClickListener { v -> it.onItemClick(item, position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseMoreViewHolder<T, VB> {
        return getViewHolder(parent, viewType)
    }

    abstract fun getViewHolder(parent: ViewGroup, viewType: Int): BaseMoreViewHolder<T, VB>

    override fun getItemCount(): Int {
        return if (showSub) {
            dataList.size.coerceAtMost(defaultShow)
        } else {
            dataList.size
        }
    }

    fun deleteItem(item: T) {
        val pos = dataList.indexOf(item)
        dataList.remove(item)
        notifyItemRemoved(pos)
    }

    fun clear() {
        dataList.clear()
        notifyDataSetChanged()
    }

    fun append(data: List<T>) {
        val lastSize = dataList.size
        dataList.addAll(data.filter { data.contains(it) })
        notifyItemRangeInserted(lastSize, data.size)
    }

    fun append(data: T) {
        val lastSize = dataList.size
        dataList.add(data)
        notifyItemInserted(lastSize)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh(data: List<T>) {
        dataList.clear()
        dataList.addAll(data)
        notifyDataSetChanged()
    }

    fun update(data: T) {
        val index = dataList.indexOf(data)
        if (index >= 0) {
            dataList[index] = data
        }
        notifyItemChanged(index)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun packUp() {
        showAll = true
        showSub = false
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showSub() {
        showAll = false
        showSub = true
        notifyDataSetChanged()
    }

    fun getItemData(position: Int): T {
        return dataList[position]
    }

    interface ItemClickListener<T> {
        fun onItemClick(item: T, position: Int)
    }

    fun <X> refreshDataAndNotify(t: T?, payload: X? = null) {
        t?.let {
            for (i in dataList.indices) {
                val element = dataList[i]
                if (element == t) {
                    dataList[i] = t
                    notifyItemChanged(i, payload)
                    break
                }
            }
        }
    }
}
