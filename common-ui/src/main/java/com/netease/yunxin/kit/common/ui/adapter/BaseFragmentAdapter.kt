/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.netease.yunxin.kit.common.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Fragment Adapter
 * used in MainActivity
 */
class BaseFragmentAdapter : FragmentStateAdapter {
    private var fragmentList: List<Fragment>? = null

    constructor(fragmentActivity: FragmentActivity) : super(fragmentActivity) {}
    constructor(fragment: Fragment) : super(fragment) {}
    constructor(fragmentManager: FragmentManager, lifecycle: Lifecycle) : super(
        fragmentManager,
        lifecycle
    ) {
    }

    fun setFragmentList(fragmentList: List<Fragment>?) {
        this.fragmentList = fragmentList
    }

    override fun createFragment(position: Int): Fragment {
        return if (fragmentList == null || fragmentList!!.size <= position) {
            Fragment()
        } else {
            fragmentList!![position]
        }
    }

    override fun getItemCount(): Int {
        return if (fragmentList == null) 0 else fragmentList!!.size
    }

    companion object {
        private const val TAG = "BaseFragmentAdapter"
    }
}
