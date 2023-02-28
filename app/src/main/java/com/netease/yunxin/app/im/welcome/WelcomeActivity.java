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
import androidx.appcompat.app.AppCompatActivity;

import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.yunxin.app.im.IMApplication;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityWelcomeBinding;
import com.netease.yunxin.app.im.main.MainActivity;
import com.netease.yunxin.app.im.utils.Constant;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.login.LoginCallback;

/** Welcome Page is launch page */
public class WelcomeActivity extends AppCompatActivity {

  private static final String TAG = "WelcomeActivity";
  private ActivityWelcomeBinding activityWelcomeBinding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ALog.d(Constant.PROJECT_TAG, TAG, "onCreateView");
    IMApplication.setColdStart(true);
    activityWelcomeBinding = ActivityWelcomeBinding.inflate(getLayoutInflater());
    setContentView(activityWelcomeBinding.getRoot());
    startLogin();
  }

  private void showMainActivityAndFinish() {
    ALog.d(Constant.PROJECT_TAG, TAG, "showMainActivityAndFinish");
    Intent intent = new Intent();
    intent.setClass(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    this.startActivity(intent);
    finish();
  }

  /** start login page, you can use to launch your own login */
  private void startLogin() {
    ALog.d(Constant.PROJECT_TAG, TAG, "startLogin");

      //填入你的 account and token
      String account = "332103880306944";
      String token = "2d9770b3-1b02-4b67-bbe1-851b522294e8";
      LoginInfo loginInfo = LoginInfo.LoginInfoBuilder.loginInfoDefault(account,token).build();

      if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
          loginIM(loginInfo);
      } else {
          activityWelcomeBinding.appDesc.setVisibility(View.GONE);
          activityWelcomeBinding.loginButton.setVisibility(View.VISIBLE);
          activityWelcomeBinding.loginButton.setOnClickListener(view -> launchLoginPage());
      }

  }

  /** launch login activity */
  private void launchLoginPage() {
    ALog.d(Constant.PROJECT_TAG, TAG, "launchLoginPage");
    ToastX.showShortToast("请在WelcomeActivity类startLogin方法添加账号信息即可进入");
  }

  /** when your own page login success, you should login IM SDK */
  private void loginIM(LoginInfo loginInfo) {
    ALog.d(Constant.PROJECT_TAG, TAG, "loginIM");
    activityWelcomeBinding.getRoot().setVisibility(View.GONE);
    IMKitClient.loginIM(
        loginInfo,
        new LoginCallback<LoginInfo>() {
          @Override
          public void onError(int errorCode, @NonNull String errorMsg) {
            ToastX.showShortToast(
                String.format(getResources().getString(R.string.login_fail), errorCode));
            launchLoginPage();
          }

          @Override
          public void onSuccess(@Nullable LoginInfo data) {
            showMainActivityAndFinish();
          }
        });
  }
}
