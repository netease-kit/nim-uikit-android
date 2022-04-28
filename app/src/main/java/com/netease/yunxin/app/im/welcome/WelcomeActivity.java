/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im.welcome;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.qchat.result.QChatLoginResult;
import com.netease.yunxin.app.im.databinding.ActivityWelcomeBinding;
import com.netease.yunxin.app.im.main.MainActivity;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.corekit.im.XKitImClient;
import com.netease.yunxin.kit.corekit.im.login.LoginCallback;

/**
 * Welcome Page is launch page
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWelcomeBinding activityWelcomeBinding = ActivityWelcomeBinding.inflate(getLayoutInflater());
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
        //start you login account and token , or with loginIm
        LoginInfo loginInfo = LoginInfo.LoginInfoBuilder.loginInfoDefault("account","token").withAppKey(DataUtils.readAppKey(this)).build();
        loginIM(loginInfo);
    }


    /**
     * when your own page login success, you should login IM SDK
     *
     */
    private void loginIM(LoginInfo loginInfo) {

        XKitImClient.loginIMWithQChat(loginInfo,new LoginCallback<QChatLoginResult>() {
            @Override
            public void onError(int errorCode, @NonNull String errorMsg) {
                // login error
            }

            @Override
            public void onSuccess(@Nullable QChatLoginResult data) {
                showMainActivityAndFinish();
            }
        });
    }

}
