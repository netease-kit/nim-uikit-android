// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityAccountLoginBinding;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;

public class AccountLoginActivity extends BaseLocalActivity {

  private ActivityAccountLoginBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewBinding = ActivityAccountLoginBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    viewBinding.settingTitleBar.setOnBackIconClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            finish();
          }
        });
    viewBinding.loginBtn.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            String account = viewBinding.accountLoginEt.getText().toString();
            String token = viewBinding.accountLoginTokenEt.getText().toString();
            loginIM(account, token);
          }
        });
  }

  // 登录IM
  private void loginIM(String account, String token) {

    IMKitClient.login(
        account,
        token,
        null,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @NonNull String errorMsg) {
            ToastX.showShortToast(
                String.format(getResources().getString(R.string.login_fail), errorCode));
          }

          @Override
          public void onSuccess(@Nullable Void data) {

            showMainActivityAndFinish();
          }
        });
  }

  // 进入主页面并结束当前页面
  private void showMainActivityAndFinish() {
    Intent intent = new Intent();
    intent.setClass(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    this.startActivity(intent);
    finish();
  }
}
