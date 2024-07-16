/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.activities.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseViewHolder
import com.netease.yunxin.kit.common.ui.activities.viewholder.EmptyViewHolder

class CommonAdapter : RecyclerView.Adapter<BaseViewHolder<CommonData<*>>>() {

    private var listData: List<CommonData<*>> = mutableListOf()
    private var factory: Factory = Factory()

    fun setData(data: List<CommonData<*>>) {
        listData = data
    }

    override fun getItemViewType(position: Int): Int {
        val data = listData[position]
        return data.type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<CommonData<*>> {
        return factory.createViewHolder(parent, viewType) as BaseViewHolder<CommonData<*>>
    }

    override fun onBindViewHolder(holder: BaseViewHolder<CommonData<*>>, position: Int) {
        val data = listData[position]
        holder.onBindData(data, position)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    fun <T : BaseViewHolder.Factory<*>> addViewHolder(type: Int, clazz: Class<T>) {
        factory.addViewHolder(type, clazz)
    }

    class Factory {
        private val holderMap = mutableMapOf<Int, Class<out BaseViewHolder.Factory<*>>>()

        fun <T : BaseViewHolder.Factory<*>> addViewHolder(type: Int, holder: Class<T>) {
            holderMap[type] = holder
        }

        fun createViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
            val holderClass = holderMap[viewType]
            return if (holderClass != null) {
                val holder = holderClass.newInstance()
                holder.createViewHolder(parent, viewType)
            } else {
                EmptyViewHolder.Factory().createViewHolder(parent, viewType)
            }
        }
    }
}
