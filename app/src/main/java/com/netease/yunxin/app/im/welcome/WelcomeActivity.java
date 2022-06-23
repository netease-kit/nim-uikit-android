/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.qchat.result.QChatLoginResult;
import com.netease.yunxin.app.im.BuildConfig;
import com.netease.yunxin.app.im.databinding.ActivityWelcomeBinding;
import com.netease.yunxin.app.im.main.MainActivity;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.login.LoginCallback;


/**
 * Welcome Page is launch page
 */
public class WelcomeActivity extends AppCompatActivity {

    private static final int LOGIN_PARENT_SCOPE = 2;
    private static final int LOGIN_SCOPE = 7;
    private ActivityWelcomeBinding activityWelcomeBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityWelcomeBinding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(activityWelcomeBinding.getRoot());
        startLogin();
    }

    private void showMainActivityAndFinish() {
        finish();
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        this.startActivity(intent);

    }

    /**
     * start login page, you can use to launch your own login
     */
    private void startLogin() {
        //start you login account and token
        String account = "";
        String token = "";
        LoginInfo loginInfo = LoginInfo.LoginInfoBuilder.loginInfoDefault(account,token).build();

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            loginIM(loginInfo);
        } else {
            activityWelcomeBinding.appDesc.setVisibility(View.GONE);
            activityWelcomeBinding.loginButton.setVisibility(View.VISIBLE);
            activityWelcomeBinding.loginButton.setOnClickListener(view -> launchLoginPage());
        }
    }

    /**
     * launch login activity
     */
    private void launchLoginPage() {
        // jump to your own LoginPage here
        ToastX.showShortToast("请在WelcomeActivity类startLogin方法添加账号信息即可进入");
    }

    /**
     * when your own page login success, you should login IM SDK
     *
     */
    private void loginIM(LoginInfo loginInfo) {

        IMKitClient.loginIMWithQChat(loginInfo,new LoginCallback<QChatLoginResult>() {
            @Override
            public void onError(int errorCode, @NonNull String errorMsg) {
                launchLoginPage();
            }

            @Override
            public void onSuccess(@Nullable QChatLoginResult data) {
                showMainActivityAndFinish();
            }
        });
    }

}
