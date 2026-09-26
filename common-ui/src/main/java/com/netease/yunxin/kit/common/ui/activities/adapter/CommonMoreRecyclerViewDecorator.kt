/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.netease.yunxin.kit.common.ui.activities.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CommonMoreRecyclerViewDecorator<T>(
    private val innerView: RecyclerView,
    val layoutManager: LinearLayoutManager,
    val adapter: CommonMoreAdapter<T, *>
) {
    var loadMoreListener: LoadMoreListener<T>? = null

    private var lastDataAnchor: T? = null

    var hasMore: Boolean = false

    var nextTimeTag = 0L

    private fun prepareForDecorator() {
        innerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            var dyP = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                dyP = dy
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE || dyP <= DY_VALUE) {
                    return
                }
                val position = layoutManager.findLastVisibleItemPosition()
                val total = adapter.itemCount
                if (loadMoreListener != null && total < position + LOAD_MORE_LIMIT) {
                    val data = adapter.getItemData(total - 1)
                    if (data != lastDataAnchor || lastDataAnchor == null) {
                        loadMoreListener!!.onLoadMore(data)
                        lastDataAnchor = data
                    }
                }
            }
        })
    }

    /**
     * load more listener
     */
    interface LoadMoreListener<T> {
        /**
         * @param data last data anchor
         */
        fun onLoadMore(data: T?)
    }

    companion object {
        const val LOAD_MORE_LIMIT = 5

        const val DY_VALUE = 0
    }

    init {
        prepareForDecorator()
    }
}
