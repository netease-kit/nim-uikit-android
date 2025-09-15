/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.common.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.netease.yunxin.kit.common.ui.CommonUIConfig
import com.netease.yunxin.kit.common.ui.R

class BrowseActivity : AppCompatActivity() {
    private var title: String? = null
    private var url: String? = null
    private var webView: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)
        title = intent.getStringExtra(PARAM_KEY_TITLE)
        url = intent.getStringExtra(PARAM_KEY_URL)
        if (TextUtils.isEmpty(url)) {
            finish()
            return
        } else {
            initViews()
        }
    }

    private fun initViews() {
        val close: ImageView = findViewById<View>(R.id.iv_close) as ImageView
        close.setOnClickListener { finish() }
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        tvTitle.text = title
        webView = initWebView()
        val webViewGroup = findViewById<ViewGroup>(R.id.rl_root)
        val layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        layoutParams.addRule(RelativeLayout.BELOW, R.id.title_divide)
        webView!!.layoutParams = layoutParams
        webViewGroup.addView(webView)
        if (!TextUtils.isEmpty(url)) {
            webView!!.loadUrl(url!!)
        }
        if (CommonUIConfig.backIconResId != 0) {
            close.setImageResource(CommonUIConfig.backIconResId)
        }
        if (CommonUIConfig.titleTextColor != 0) {
            tvTitle.setTextColor(CommonUIConfig.titleTextColor)
        }
        if (CommonUIConfig.titleTextSize != 0f) {
            tvTitle.textSize = CommonUIConfig.titleTextSize
        }
    }

    private fun initWebView(): WebView {
        val webView = WebView(applicationContext)
        webView.setOnLongClickListener { true }
        val client: WebViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                val uri = Uri.parse(url)
                val scheme = uri.scheme
                var result =
                    TextUtils.isEmpty(scheme) || scheme != SCHEME_HTTP && scheme != SCHEME_HTTPS
                if (result) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = uri
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        result = false
                    }
                }
                return result
            }
        }
        webView.webViewClient = client
        webView.webChromeClient = WebChromeClient()
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.domStorageEnabled = true
        webView.settings.blockNetworkImage = false
        webView.settings.javaScriptEnabled = true
        return webView
    }

    override fun onBackPressed() {
        if (webView!!.canGoBack()) {
            webView!!.goBack()
            return
        }
        super.onBackPressed()
    }

    companion object {
        const val PARAM_KEY_TITLE = "param_key_title"
        const val PARAM_KEY_URL = "param_key_url"
        const val SCHEME_HTTP = "http"
        const val SCHEME_HTTPS = "https"
        fun launch(context: Context, title: String?, url: String?) {
            val intent = Intent(context, BrowseActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            intent.putExtra(PARAM_KEY_TITLE, title)
            intent.putExtra(PARAM_KEY_URL, url)
            context.startActivity(intent)
        }
    }
}
