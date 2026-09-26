// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.welcome;

import static com.netease.yunxin.app.im.IMApplication.LOGIN_PARENT_SCOPE;
import static com.netease.yunxin.app.im.IMApplication.LOGIN_SCOPE;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginListener;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncLevel;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginClientChange;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus;
import com.netease.nimlib.sdk.v2.auth.model.V2NIMKickedOfflineDetail;
import com.netease.nimlib.sdk.v2.auth.model.V2NIMLoginClient;
import com.netease.nimlib.sdk.v2.auth.option.V2NIMLoginOption;
import com.netease.yunxin.app.im.AppConfig;
import com.netease.yunxin.app.im.BuildConfig;
import com.netease.yunxin.app.im.IMApplication;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityWelcomeBinding;
import com.netease.yunxin.app.im.main.AccountLoginActivity;
import com.netease.yunxin.app.im.main.MainActivity;
import com.netease.yunxin.app.im.main.mine.setting.ServerConfigActivity;
import com.netease.yunxin.app.im.utils.AppUtils;
import com.netease.yunxin.app.im.utils.Constant;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.List;

/** 启动页面 如果没有登录则展示登录按钮，点击登录按钮进入登录页面 如果已经登录则直接进入主页面 */
public class WelcomeActivity extends BaseLocalActivity {

  private static final String TAG = "WelcomeActivity";
  private static final long NO_LOGIN_REQUEST = 0L;
  private static final long LOGIN_PENDING_LOG_DELAY_MS = 5_000L;
  private static final long SDK_LOGIN_TIMEOUT_MS = 60_000L;
  private static final long LOGIN_WATCHDOG_DELAY_MS = SDK_LOGIN_TIMEOUT_MS + 5_000L;

  private ActivityWelcomeBinding activityWelcomeBinding;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private long loginRequestSequence;
  private long activeLoginRequestId = NO_LOGIN_REQUEST;
  private long loginStartElapsedTime;
  private boolean navigationStarted;
  private Runnable pendingLogTask;
  private Runnable loginWatchdogTask;

  private final V2NIMLoginListener loginListener =
      new V2NIMLoginListener() {
        @Override
        public void onLoginStatus(V2NIMLoginStatus status) {
          runOnUiThread(
              () -> {
                ALog.i(
                    Constant.PROJECT_TAG,
                    TAG,
                    "loginStatusChanged:status=" + status + ",requestId=" + activeLoginRequestId);
                if (status == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED) {
                  handleLoginSuccess(activeLoginRequestId, "statusListener");
                }
              });
        }

        @Override
        public void onLoginFailed(V2NIMError error) {
          runOnUiThread(
              () -> {
                int errorCode = error == null ? -1 : error.getCode();
                String errorMessage =
                    error == null ? getString(R.string.request_fail) : error.getDesc();
                handleLoginError(activeLoginRequestId, errorCode, errorMessage, "statusListener");
              });
        }

        @Override
        public void onKickedOffline(V2NIMKickedOfflineDetail detail) {}

        @Override
        public void onLoginClientChanged(
            V2NIMLoginClientChange change, List<V2NIMLoginClient> clients) {}
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ALog.d(Constant.PROJECT_TAG, TAG, "onCreateView");
    IMApplication.setColdStart(true);
    activityWelcomeBinding = ActivityWelcomeBinding.inflate(getLayoutInflater());
    setContentView(activityWelcomeBinding.getRoot());
    IMKitClient.addLoginListener(loginListener);
    // 判断是否已经登录
    if (IMKitClient.hasLogin()) {
      showMainActivityAndFinish();
    } else {
      startLogin();
    }
  }

  // 进入主页面并结束当前页面
  private void showMainActivityAndFinish() {
    if (navigationStarted || isFinishing() || isDestroyed()) {
      return;
    }
    navigationStarted = true;
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
        String account = AppConfig.account;
        String token = AppConfig.token;
        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            loginIM(account, token);
        } else {
            showLoginView();
        }
    }

  // 展示登录页面
  private void showLoginView() {
    ALog.d(Constant.PROJECT_TAG, TAG, "showLoginView");
    activityWelcomeBinding.getRoot().setVisibility(View.VISIBLE);
    activityWelcomeBinding.appDesc.setVisibility(View.GONE);
    activityWelcomeBinding.loginButton.setVisibility(View.VISIBLE);
    activityWelcomeBinding.loginPendingContainer.setVisibility(View.GONE);
    stopLoginPendingAnimation();
    activityWelcomeBinding.appBottomIcon.setVisibility(View.GONE);
    activityWelcomeBinding.appBottomName.setVisibility(View.GONE);
    activityWelcomeBinding.tvEmailLogin.setVisibility(View.VISIBLE);
    activityWelcomeBinding.tvServerConfig.setVisibility(View.VISIBLE);
    activityWelcomeBinding.vEmailLine.setVisibility(View.VISIBLE);
    activityWelcomeBinding.tvAccountLogin.setVisibility(View.VISIBLE);
    activityWelcomeBinding.vServerLine.setVisibility(View.VISIBLE);
    activityWelcomeBinding.vAccountLoginLine.setVisibility(View.VISIBLE);
    activityWelcomeBinding.tvServerPrivateConfig.setVisibility(View.VISIBLE);
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
    activityWelcomeBinding.tvAccountLogin.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(WelcomeActivity.this, AccountLoginActivity.class);
            startActivity(intent);
          }
        });
    activityWelcomeBinding.tvServerPrivateConfig.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(WelcomeActivity.this, ServerConfigActivity.class);
            startActivity(intent);
          }
        });
  }

    // 启动登录页面
    private void launchLoginPage() {
        ALog.d(Constant.PROJECT_TAG, TAG, "launchLoginPage");
    }

  // 登录IM
    private void loginIM(String account, String token) {
    if (activeLoginRequestId != NO_LOGIN_REQUEST) {
      ALog.w(
          Constant.PROJECT_TAG,
          TAG,
          "loginIM:ignored duplicate request,activeRequestId=" + activeLoginRequestId);
      return;
    }
    long requestId = ++loginRequestSequence;
    activeLoginRequestId = requestId;
    loginStartElapsedTime = SystemClock.elapsedRealtime();
    ALog.i(
        Constant.PROJECT_TAG,
        TAG,
        "loginIM:begin,requestId="
            + requestId
            + ",hasAccount="
            + !TextUtils.isEmpty(account)
            + ",hasToken="
            + !TextUtils.isEmpty(token));
    activityWelcomeBinding.getRoot().setVisibility(View.VISIBLE);
    showLoginPendingView();
    scheduleLoginWatchdogs(requestId);
    V2NIMLoginOption option = new V2NIMLoginOption();
    option.setSyncLevel(V2NIMDataSyncLevel.V2NIM_DATA_SYNC_TYPE_LEVEL_BASIC);
    option.setOfflineMode(false);
    option.setTimeout(SDK_LOGIN_TIMEOUT_MS);
    IMKitClient.login(
        account,
        token,
        option,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @NonNull String errorMsg) {
            runOnUiThread(() -> handleLoginError(requestId, errorCode, errorMsg, "loginCallback"));
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            runOnUiThread(() -> handleLoginSuccess(requestId, "loginCallback"));
          }
        });
  }

  private void showLoginPendingView() {
    activityWelcomeBinding.getRoot().setVisibility(View.VISIBLE);
    activityWelcomeBinding.loginButton.setVisibility(View.GONE);
    activityWelcomeBinding.loginPendingContainer.setVisibility(View.VISIBLE);
    activityWelcomeBinding.loginPendingDots.playAnimation();
    activityWelcomeBinding.tvEmailLogin.setVisibility(View.GONE);
    activityWelcomeBinding.tvServerConfig.setVisibility(View.GONE);
    activityWelcomeBinding.vEmailLine.setVisibility(View.GONE);
    activityWelcomeBinding.tvAccountLogin.setVisibility(View.GONE);
    activityWelcomeBinding.vServerLine.setVisibility(View.GONE);
    activityWelcomeBinding.vAccountLoginLine.setVisibility(View.GONE);
    activityWelcomeBinding.tvServerPrivateConfig.setVisibility(View.GONE);
  }

  private void stopLoginPendingAnimation() {
    activityWelcomeBinding.loginPendingDots.cancelAnimation();
  }

  private void scheduleLoginWatchdogs(long requestId) {
    clearLoginWatchdogs();
    pendingLogTask =
        () -> {
          if (!isActiveLoginRequest(requestId)) {
            return;
          }
          ALog.w(
              Constant.PROJECT_TAG,
              TAG,
              "loginIM:pending,requestId="
                  + requestId
                  + ",elapsedMs="
                  + getLoginElapsedTime()
                  + ",hasLogin="
                  + IMKitClient.hasLogin());
        };
    loginWatchdogTask = () -> handleLoginTimeout(requestId);
    mainHandler.postDelayed(pendingLogTask, LOGIN_PENDING_LOG_DELAY_MS);
    mainHandler.postDelayed(loginWatchdogTask, LOGIN_WATCHDOG_DELAY_MS);
  }

  private void handleLoginSuccess(long requestId, String source) {
    if (!isActiveLoginRequest(requestId)) {
      return;
    }
    ALog.i(
        Constant.PROJECT_TAG,
        TAG,
        "loginIM:success,source="
            + source
            + ",requestId="
            + requestId
            + ",elapsedMs="
            + getLoginElapsedTime());
    finishLoginRequest();
    showMainActivityAndFinish();
  }

  private void handleLoginError(
      long requestId, int errorCode, @Nullable String errorMessage, String source) {
    if (!isActiveLoginRequest(requestId)) {
      return;
    }
    ALog.w(
        Constant.PROJECT_TAG,
        TAG,
        "loginIM:error,source="
            + source
            + ",requestId="
            + requestId
            + ",elapsedMs="
            + getLoginElapsedTime()
            + ",errorCode="
            + errorCode
            + ",errorMessage="
            + errorMessage);
    finishLoginRequest();
    ToastX.showShortToast(
        String.format(getResources().getString(R.string.login_fail), errorMessage));
    showLoginView();
  }

  private void handleLoginTimeout(long requestId) {
    if (!isActiveLoginRequest(requestId)) {
      return;
    }
    ALog.w(
        Constant.PROJECT_TAG,
        TAG,
        "loginIM:timeout,requestId="
            + requestId
            + ",elapsedMs="
            + getLoginElapsedTime()
            + ",hasLogin="
            + IMKitClient.hasLogin());
    if (IMKitClient.hasLogin()) {
      handleLoginSuccess(requestId, "watchdog");
      return;
    }
    finishLoginRequest();
    ToastX.showShortToast(R.string.login_timeout);
    showLoginView();
  }

  private boolean isActiveLoginRequest(long requestId) {
    return requestId != NO_LOGIN_REQUEST && requestId == activeLoginRequestId;
  }

  private long getLoginElapsedTime() {
    return SystemClock.elapsedRealtime() - loginStartElapsedTime;
  }

  private void finishLoginRequest() {
    activeLoginRequestId = NO_LOGIN_REQUEST;
    clearLoginWatchdogs();
  }

  private void clearLoginWatchdogs() {
    if (pendingLogTask != null) {
      mainHandler.removeCallbacks(pendingLogTask);
      pendingLogTask = null;
    }
    if (loginWatchdogTask != null) {
      mainHandler.removeCallbacks(loginWatchdogTask);
      loginWatchdogTask = null;
    }
  }

  @Override
  protected void onDestroy() {
    IMKitClient.removeLoginListener(loginListener);
    if (activityWelcomeBinding != null) {
      stopLoginPendingAnimation();
    }
    finishLoginRequest();
    activityWelcomeBinding = null;
    super.onDestroy();
  }
}
