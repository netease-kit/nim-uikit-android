// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.robot;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.RobotInfoBean;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;

/**
 * 查看机器人配置串页面基类
 *
 * <p>子类在 {@link #initViews()} 中完成布局 inflate、setContentView，并给以下字段赋值。 Base 负责拉取机器人信息、展示配置串、复制到剪切板。
 */
public abstract class BaseRobotViewConfigActivity extends BaseLocalActivity {

  // ---------- 子类必须在 initViews() 中赋值的 View 字段 ----------
  protected BackTitleBar titleBar;
  protected TextView tvConfigContent;
  protected TextView btnCopy;
  // ---------------------------------------------------------------

  protected RobotInfoViewModel viewModel;
  private String configString = "";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initViews();
    configTitle(titleBar);
    loadData();
    bindEvents();
  }

  /** 子类实现：inflate 布局、setContentView，并给所有 View 字段赋值 */
  protected abstract void initViews();

  /** 子类可覆写定制标题栏（Fun 版修改字体/背景） */
  protected void configTitle(BackTitleBar bar) {
    bar.setOnBackIconClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
  }

  private void loadData() {
    viewModel = new ViewModelProvider(this).get(RobotInfoViewModel.class);

    String accountId = getIntent().getStringExtra(RouterConstant.KEY_ACCOUNT_ID_KEY);
    if (TextUtils.isEmpty(accountId)) {
      finish();
      return;
    }

    viewModel
        .getRobotInfoLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success && result.getData() != null) {
                RobotInfoBean bean = result.getData();
                configString = buildConfigString(bean);
                tvConfigContent.setText(maskConfigString(configString));
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                Toast.makeText(this, R.string.contact_operate_error_tip, Toast.LENGTH_SHORT).show();
              }
            });

    viewModel.loadRobotInfo(accountId);
  }

  private void bindEvents() {
    btnCopy.setOnClickListener(
        v -> {
          if (TextUtils.isEmpty(configString)) return;
          ClipboardManager clipboard =
              (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
          ClipData clip =
              ClipData.newPlainText(
                  getString(R.string.contact_robot_view_config_label), configString);
          if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.contact_robot_view_config_copied, Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  /**
   * 脱敏展示配置串：前 1/3 明文，其余用「...」代替。
   *
   * @param raw 原始配置串
   * @return 脱敏后的字符串
   */
  private String maskConfigString(String raw) {
    if (TextUtils.isEmpty(raw)) return raw;
    int visibleLen = Math.max(1, raw.length() / 3);
    return raw.substring(0, visibleLen) + "...";
  }

  /** 根据机器人信息拼接配置串，子类可覆写。 默认格式：{@code Appkey:Accid:Token} */
  protected String buildConfigString(RobotInfoBean bean) {
    if (bean != null && bean.getAIBot() != null) {
      return IMKitClient.getOptions().appKey
          + ":"
          + bean.getAccountId()
          + ":"
          + bean.getAIBot().getToken();
    }
    return "";
  }
}
