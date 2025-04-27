// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.v2.auth.option.V2NIMLoginOption;
import com.netease.yunxin.app.im.IMApplication;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityWelcomeBinding;
import com.netease.yunxin.app.im.main.MainActivity;
import com.netease.yunxin.app.im.utils.Constant;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;

/**
 * 启动页面 如果没有登录则展示登录按钮，点击登录按钮进入登录页面 如果已经登录则直接进入主页面
 */
public class WelcomeActivity extends BaseActivity {

    private static final String TAG = "WelcomeActivity";
    private ActivityWelcomeBinding activityWelcomeBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ALog.d(Constant.PROJECT_TAG, TAG, "onCreateView");
        IMApplication.setColdStart(true);
        activityWelcomeBinding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(activityWelcomeBinding.getRoot());
        // 判断是否已经登录
        if (IMKitClient.hasLogin()) {
            showMainActivityAndFinish();
        } else {
            startLogin();
        }
    }

    // 进入主页面并结束当前页面
    private void showMainActivityAndFinish() {
        ALog.d(Constant.PROJECT_TAG, TAG, "showMainActivityAndFinish");
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        this.startActivity(intent);
        finish();
    }

    // 开始登录
    private void startLogin() {
        ALog.d(Constant.PROJECT_TAG, TAG, "startLogin");

        //填入你的 account and token
        String account = "";
        String token = "";
        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            loginIM(account, token);
        } else {
            showLoginView();
        }
    }

    // 展示登录页面
    private void showLoginView() {
        ALog.d(Constant.PROJECT_TAG, TAG, "showLoginView");
        activityWelcomeBinding.appDesc.setVisibility(View.GONE);
        activityWelcomeBinding.loginButton.setVisibility(View.VISIBLE);
        activityWelcomeBinding.appBottomIcon.setVisibility(View.GONE);
        activityWelcomeBinding.appBottomName.setVisibility(View.GONE);
        activityWelcomeBinding.tvEmailLogin.setVisibility(View.VISIBLE);
        activityWelcomeBinding.tvServerConfig.setVisibility(View.VISIBLE);
        activityWelcomeBinding.vEmailLine.setVisibility(View.VISIBLE);
        activityWelcomeBinding.loginButton.setOnClickListener(
                view -> {

                    launchLoginPage();
                });
        activityWelcomeBinding.tvEmailLogin.setOnClickListener(
                view -> {

                    launchLoginPage();
                });
        activityWelcomeBinding.tvServerConfig.setOnClickListener(
                view -> {
                    Intent intent = new Intent(WelcomeActivity.this, ServerActivity.class);
                    startActivity(intent);
                });
    }

    // 启动登录页面
    private void launchLoginPage() {
        ALog.d(Constant.PROJECT_TAG, TAG, "launchLoginPage");

    }

    // 登录IM
    private void loginIM(String account, String token) {
        ALog.d(Constant.PROJECT_TAG, TAG, "loginIM");
        activityWelcomeBinding.getRoot().setVisibility(View.GONE);
        V2NIMLoginOption option = new V2NIMLoginOption();
        IMKitClient.login(
                account,
                token,
                option,
                new FetchCallback<Void>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        ToastX.showShortToast(
                                String.format(
                                        getResources().getString(R.string.login_fail),
                                        errorCode
                                ));
                        launchLoginPage();
                    }

                    @Override
                    public void onSuccess(@Nullable Void data) {
                        showMainActivityAndFinish();
                    }
                }
        );
    }
}
