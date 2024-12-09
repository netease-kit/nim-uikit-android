// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityServerConfigBinding;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.dialog.CommonConfirmDialog;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerConfigActivity extends BaseLocalActivity {

  // 服务器Link地址
  private final String KEY_LINK = "link";
  // lbs服务器地址
  private final String KEY_LBS = "lbs";
  private final String KEY_APP_KEY = "appkey";
  // nos 上传LBS地址
  private final String KEY_NOS_LBS = "nos_lbs";
  // nos 上传默认link地址
  private final String KEY_NOS_UPLOADER = "nos_uploader";
  // nos 拼接下载地址
  private final String KEY_NOS_DOWNLOADER = "nos_downloader";
  // nos 上传服务主机
  private final String KEY_NOS_UPLOADER_HOST = "nos_uploader_host";
  //连接云信服务器加密数据通道的公钥参数1 rsaModulus
  private final String KEY_MODULE = "module";

  private ActivityServerConfigBinding viewBinding;
  private ServerConfigViewModel viewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityServerConfigBinding.inflate(getLayoutInflater());
    viewModel = new ViewModelProvider(this).get(ServerConfigViewModel.class);
    setContentView(viewBinding.getRoot());
    initView();
    loadData();
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadData();
  }

  private void initView() {
    viewBinding.settingTitleBar.setOnBackIconClickListener(v -> onBackPressed());

    viewBinding.configTypeLayout.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            startActivity(new Intent(ServerConfigActivity.this, ServerConfigParseActivity.class));
          }
        });

    viewBinding.serverConfigSwitchLayout.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            viewBinding.serverConfigSC.setChecked(!viewBinding.serverConfigSC.isChecked());
          }
        });
    viewBinding.tvSaveConfig.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            boolean configSwitch = viewBinding.serverConfigSC.isChecked();
            String defaultLink = viewBinding.linkEt.getText().toString();
            String appKey = viewBinding.appkeyEt.getText().toString();
            String lbs = viewBinding.lbsEt.getText().toString();
            String nosLbs = viewBinding.nosLbsEt.getText().toString();
            String nosDefaultLink = viewBinding.nosLbsDefaultEt.getText().toString();
            String nosUpload = viewBinding.nosUploadEt.getText().toString();
            String nosFormatDownload = viewBinding.nosDownloadEt.getText().toString();
            String serverModule = viewBinding.serverModuleEt.getText().toString();

            JSONObject dataJson = new JSONObject();
            try {
              dataJson.put(KEY_LINK, defaultLink);
              dataJson.put(KEY_APP_KEY, appKey);
              dataJson.put(KEY_LBS, lbs);
              dataJson.put(KEY_NOS_LBS, nosLbs);
              dataJson.put(KEY_NOS_UPLOADER, nosDefaultLink);
              dataJson.put(KEY_NOS_UPLOADER_HOST, nosUpload);
              dataJson.put(KEY_NOS_DOWNLOADER, nosFormatDownload);
              dataJson.put(KEY_MODULE, serverModule);
            } catch (JSONException e) {
              throw new RuntimeException(e);
            }

            CommonConfirmDialog.Companion.show(
                ServerConfigActivity.this,
                getString(R.string.server_config_dialog_title),
                getString(R.string.server_config_dialog_content),
                getString(R.string.server_config_dialog_cancel),
                getString(R.string.server_config_dialog_positive),
                true,
                true,
                positive -> {
                  if (positive) {
                    String dataStr = dataJson.toString();
                    viewModel.saveServerConfig(dataStr, configSwitch);
                    IMKitClient.logout(
                        new FetchCallback<Void>() {
                          @Override
                          public void onSuccess(@Nullable Void unused) {

                            Intent intent =
                                getBaseContext()
                                    .getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            android.os.Process.killProcess(android.os.Process.myPid());
                          }

                          @Override
                          public void onError(int i, @NonNull String s) {}
                        });
                  }
                });
          }
        });
    viewBinding.lbsClearIv.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            viewBinding.lbsEt.setText("");
          }
        });

    viewBinding.nosLbsClearIv.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            viewBinding.nosLbsEt.setText("");
          }
        });

    viewBinding.nosLbsDefaultClearIv.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            viewBinding.nosLbsDefaultEt.setText("");
          }
        });

    viewBinding.nosDownloadClearIv.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            viewBinding.nosDownloadEt.setText("");
          }
        });

    viewBinding.nosUploadDefaultClearIv.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            viewBinding.nosUploadEt.setText("");
          }
        });

    viewBinding.nosDownloadClearIv.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            viewBinding.nosDownloadEt.setText("");
          }
        });

    viewBinding.serverModuleClearIv.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            viewBinding.serverModuleEt.setText("");
          }
        });
  }

  private void loadData() {
    JSONObject jsonData = viewModel.getServerConfig();
    boolean configSwitch = viewModel.getServiceConfigSwitch();
    viewBinding.serverConfigSC.setChecked(configSwitch);
    if (jsonData != null) {
      viewBinding.appkeyEt.setText(jsonData.optString(KEY_APP_KEY));
      viewBinding.linkEt.setText(jsonData.optString(KEY_LINK));
      viewBinding.lbsEt.setText(jsonData.optString(KEY_LBS));
      viewBinding.nosLbsEt.setText(jsonData.optString(KEY_NOS_LBS));
      viewBinding.nosLbsDefaultEt.setText(jsonData.optString(KEY_NOS_UPLOADER));
      viewBinding.nosUploadEt.setText(jsonData.optString(KEY_NOS_UPLOADER_HOST));
      viewBinding.nosDownloadEt.setText(jsonData.optString(KEY_NOS_DOWNLOADER));
      viewBinding.serverModuleEt.setText(jsonData.optString(KEY_MODULE));
    }
  }
}
