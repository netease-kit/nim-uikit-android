// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityConfigInfoBinding;
import com.netease.yunxin.app.im.welcome.WelcomeActivity;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.dialog.CommonConfirmDialog;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;

/**
 * OpenClaw配置信息页面
 * 允许用户动态配置AppKey、Account、Token、OpenClaw Account等参数
 */
public class ConfigInfoActivity extends BaseLocalActivity {

    private ActivityConfigInfoBinding viewBinding;
    private ConfigInfoViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusBarColor(R.color.color_e9eff5);
        
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_config_info);
        viewModel = new ViewModelProvider(this).get(ConfigInfoViewModel.class);
        viewBinding.setViewModel(viewModel);
        viewBinding.setLifecycleOwner(this);
        
        initView();
        observeViewModel();
    }

    private void initView() {
        // 设置标题栏返回按钮
        viewBinding.configTitleBar.setOnBackIconClickListener(v -> onBackPressed());
        
        // 设置输入框监听器
        setupInputWatchers();
        
        // 保存按钮点击事件
        viewBinding.btnSaveConfig.setOnClickListener(v -> {
            if (viewModel.validateAndSaveConfig()) {
                showRestartConfirmDialog();
            }
        });
        
        // 重置按钮点击事件
        viewBinding.tvResetConfig.setOnClickListener(v -> {
            viewModel.resetConfig();
            Toast.makeText(this, getString(R.string.config_reset_success), Toast.LENGTH_SHORT).show();
        });
        
        // 加载现有配置
        viewModel.loadExistingConfig();
    }

    private void setupInputWatchers() {
        // AppKey输入监听
        viewBinding.etAppKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setAppKey(s.toString());
                validateAppKey(s.toString());
            }
        });
        
        // Account输入监听
        viewBinding.etAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setAccount(s.toString());
            }
        });
        
        // Token输入监听
        viewBinding.etToken.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setToken(s.toString());
            }
        });
        
        // OpenClaw Account输入监听
        viewBinding.etOpenclawAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setOpenClawAccount(s.toString());
            }
        });
    }

    private void validateAppKey(String appKey) {
        if (!viewModel.isValidAppKey(appKey)) {
            viewBinding.tvAppKeyError.setText(getString(R.string.config_app_key_error));
            viewBinding.tvAppKeyError.setVisibility(View.VISIBLE);
        } else {
            viewBinding.tvAppKeyError.setVisibility(View.GONE);
        }
    }

    /**
     * 显示重启应用确认对话框
     */
    private void showRestartConfirmDialog() {

        CommonConfirmDialog.Companion.show(
                this,
                getString(R.string.server_config_agent_title),
                getString(R.string.server_config_agent_dialog_content),
                getString(R.string.server_config_dialog_cancel),
                getString(R.string.server_config_dialog_positive),
                true,
                false,
                positive -> {
                    if (positive) {
                        restartApp();
                    }else {
                        finish();
                    }
                });
    }

    /**
     * 重启应用
     */
    private void restartApp() {
        // 清除所有Activity栈，重新启动Welcome页面
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        // 杀死当前进程，确保完全重启
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void observeViewModel() {
        // 观察保存结果
        viewModel.getSaveResult().observe(this, result -> {
            if (result != null && !result.isSuccess()) {
                Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        
        // 观察验证错误
        viewModel.getValidationError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewBinding != null) {
            viewBinding.unbind();
        }
    }
}