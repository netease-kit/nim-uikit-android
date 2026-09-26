/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences utils
 * 应用基本设置，和用户无关
 *
 * @constructor Create empty S p utils
 */
object SPUtils {

    private const val preferenceName = "yunxin_app_im"

    fun getBoolean(key: String, value: Boolean, context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(key, value)
    }

    fun saveBoolean(key: String, value: Boolean, context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun saveInt(key: String, value: Int, context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String, value: Int, context: Context): Int {
        return getSharedPreferences(context).getInt(key, value)
    }

    fun saveLong(key: String, value: Long, context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String, value: Long, context: Context): Long {
        return getSharedPreferences(context).getLong(key, value)
    }

    fun getString(key: String, value: String, context: Context): String? {
        return getSharedPreferences(context).getString(key, value)
    }

    fun saveString(key: String, value: String, context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context
            .getSharedPreferences(
                preferenceName,
                Context.MODE_PRIVATE
            )
    }
}
