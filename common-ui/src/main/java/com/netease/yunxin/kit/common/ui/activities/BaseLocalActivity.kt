/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.activities

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.text.TextUtils
import com.netease.yunxin.kit.common.ui.utils.AppLanguageConfig
import java.util.Locale

/**
 * 支持语言设置的Activity
 *
 * @constructor Create empty Base local activity
 */
open class BaseLocalActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language = AppLanguageConfig.getInstance().getAppLanguage(this)
        changeLanguage(this, language)
    }

    /**
     * 修改应用内语言设置
     *
     * @param language 语言
     * @param area 地区
     */
    private fun changeLanguage(context: Context?, language: String, area: String? = null) {
        if (context == null || TextUtils.isEmpty(language)) {
            return
        }
        // 修改app语言，持久化语言选项信息
        val newLocale = Locale(language, area ?: "")
        setAppLanguage(context, newLocale)
    }

    /**
     * 更新应用语言
     *
     * @param context 上下
     * @param locale 语言
     */
    private fun setAppLanguage(context: Context, locale: Locale) {
        val resources = context.resources
        val metrics = resources.displayMetrics
        val configuration = resources.configuration
        // Android 7.0以上的方法
        if (Build.VERSION.SDK_INT >= 24) {
            configuration.setLocale(locale)
            configuration.setLocales(LocaleList(locale))
            context.createConfigurationContext(configuration)
            // 实测，updateConfiguration这个方法虽然很多博主说是版本不适用
            // 但是我的生产环境androidX+Android Q环境下，必须加上这一句，才可以通过重启App来切换语言
            resources.updateConfiguration(configuration, metrics)
        } else {
            // Android 4.1 以上方法
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, metrics)
        }
    }
}
