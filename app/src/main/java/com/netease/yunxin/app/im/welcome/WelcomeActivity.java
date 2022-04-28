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
import com.netease.yunxin.app.im.BuildConfig;
import com.netease.yunxin.app.im.databinding.ActivityWelcomeBinding;
import com.netease.yunxin.app.im.main.MainActivity;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.corekit.im.XKitImClient;
import com.netease.yunxin.kit.corekit.im.login.LoginCallback;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.AuthorConfig;
import com.netease.yunxin.kit.login.model.LoginResultCallback;
import com.netease.yunxin.kit.login.model.UserInfo;
import com.netease.yunxin.kit.login.utils.action.FinishAction;


/**
 * Welcome Page is launch page
 */
public class WelcomeActivity extends AppCompatActivity {

    private static final int LOGIN_PARENT_SCOPE = 2;
    private static final int LOGIN_SCOPE = 7;

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
        AuthorConfig authorConfig = new AuthorConfig(DataUtils.readAppKey(this), LOGIN_PARENT_SCOPE, LOGIN_SCOPE, BuildConfig.DEBUG);
        AuthorManager.INSTANCE.initAuthor(this, authorConfig);

        AuthorManager.INSTANCE.autoLogin(new com.netease.yunxin.kit.login.model.LoginCallback<UserInfo>() {
            @Override
            public void onSuccess(@Nullable UserInfo userInfo) {
                assert userInfo != null;
                loginIM(userInfo.getImAccid(),userInfo.getImToken(),null);
            }

            @Override
            public void onError(int i, @NonNull String s) {
                launchLoginPage();
            }
        });
    }

    /**
     * launch login activity
     */
    private void launchLoginPage() {
        AuthorManager.INSTANCE.launchLogin(this, new LoginResultCallback(){

            @Override
            public void onSuccess(@Nullable UserInfo userInfo, @NonNull FinishAction finishAction) {
                if (userInfo != null) {
                    loginIM(userInfo.getImAccid(),userInfo.getImToken(),finishAction);
                }
            }

            @Override
            public void onCancel() {
                super.onCancel();
                finish();
            }
        });
    }

    /**
     * when your own page login success, you should login IM SDK
     *
     */
    private void loginIM(String account,String token, FinishAction finishAction) {

        LoginInfo loginInfo = LoginInfo.LoginInfoBuilder.loginInfoDefault(account, token).withAppKey(DataUtils.readAppKey(this)).build();
        XKitImClient.loginIMWithQChat(loginInfo,new LoginCallback<QChatLoginResult>() {
            @Override
            public void onError(int errorCode, @NonNull String errorMsg) {
                launchLoginPage();
            }

            @Override
            public void onSuccess(@Nullable QChatLoginResult data) {
                showMainActivityAndFinish();
                if (finishAction != null) {
                    finishAction.finish();
                }
            }
        });
    }

}
