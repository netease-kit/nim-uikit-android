// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.robot;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.RobotInfoBean;
import com.netease.yunxin.kit.contactkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/**
 * 机器人信息页面基类
 *
 * <p>子类在 {@link #initViews()} 中完成布局 inflate、setContentView，并给以下字段赋值。 Base 负责所有通用数据加载和交互逻辑。
 */
public abstract class BaseRobotInfoActivity extends BaseLocalActivity {

  // ---------- 子类必须在 initViews() 中赋值的 View 字段 ----------
  protected BackTitleBar titleBar;
  protected ContactAvatarView avatarView;
  protected TextView tvRobotName;
  /** 编辑入口行（Normal 版显示，Fun 版隐藏） */
  protected View rlyEdit;
  /** 头像+名称整体区域（Fun 版点击进入编辑） */
  protected View rlyAvatarName;

  protected View rlyViewConfig;
  protected View rlyRefreshToken;
  protected TextView tvChat;
  protected TextView tvDelete;
  // ---------------------------------------------------------------

  protected RobotInfoViewModel viewModel;
  protected RobotInfoBean robotBean;
  protected String accountId;

  /** 启动编辑页并接收返回的修改结果 */
  private final ActivityResultLauncher<Intent> editLauncher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
              String newAvatar =
                  result.getData().getStringExtra(BaseRobotEditActivity.KEY_RESULT_AVATAR);
              String newName =
                  result.getData().getStringExtra(BaseRobotEditActivity.KEY_RESULT_NAME);
              boolean changed = false;
              if (!TextUtils.isEmpty(newAvatar)) {
                robotBean =
                    new RobotInfoBean(robotBean.getAccountId(), robotBean.getName(), newAvatar);
                changed = true;
              }
              if (!TextUtils.isEmpty(newName)) {
                robotBean =
                    new RobotInfoBean(robotBean.getAccountId(), newName, robotBean.getAvatar());
                changed = true;
              }
              if (changed) {
                // 重新从服务端拉取最新数据，刷新整个页面
                viewModel.loadRobotInfo(accountId);
              }
            }
          });

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 1. 子类 inflate 布局、setContentView，并给所有 View 字段赋值
    initViews();
    // 2. 通用标题栏配置
    configTitle(titleBar);
    // 3. 数据 & 视图初始化
    loadData();
    bindEvents();
  }

  /** 子类实现：inflate 对应皮肤的布局，调用 setContentView， 并将布局中的各个 View 赋值给父类的 protected 字段。 */
  protected abstract void initViews();

  /** 子类可覆写以定制标题栏（Fun 版修改字体/背景等） */
  protected void configTitle(BackTitleBar bar) {
    bar.setOnBackIconClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
  }

  private void loadData() {
    viewModel = new ViewModelProvider(this).get(RobotInfoViewModel.class);

    viewModel
        .getDeleteResultLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                Toast.makeText(this, R.string.contact_robot_delete_success, Toast.LENGTH_SHORT)
                    .show();
                finish();
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                Toast.makeText(this, R.string.contact_operate_error_tip, Toast.LENGTH_SHORT).show();
              }
            });

    viewModel
        .getRefreshTokenResultLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                Toast.makeText(
                        this, R.string.contact_robot_refresh_token_success, Toast.LENGTH_SHORT)
                    .show();
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                Toast.makeText(this, R.string.contact_operate_error_tip, Toast.LENGTH_SHORT).show();
              }
            });

    accountId = getIntent().getStringExtra(RouterConstant.KEY_ACCOUNT_ID_KEY);
    String name = getIntent().getStringExtra(RouterConstant.KEY_ROBOT_NAME);
    String avatar = getIntent().getStringExtra(RouterConstant.KEY_ROBOT_AVATAR);
    if (TextUtils.isEmpty(accountId)) {
      finish();
      return;
    }
    robotBean = new RobotInfoBean(accountId, name, avatar);

    viewModel
        .getRobotInfoLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success && result.getData() != null) {
                robotBean = result.getData();
                avatarView.setData(
                    robotBean.getAvatar(),
                    robotBean.getName(),
                    ColorUtils.avatarColor(robotBean.getAccountId()));
                tvRobotName.setText(robotBean.getName());
              }
            });

    viewModel.loadRobotInfo(accountId);
  }

  private void bindEvents() {
    if (robotBean == null) return;

    avatarView.setData(
        robotBean.getAvatar(),
        robotBean.getName(),
        ColorUtils.avatarColor(robotBean.getAccountId()));
    tvRobotName.setText(robotBean.getName());

    setupEditEntry();

    rlyViewConfig.setOnClickListener(
        v -> {
          if (!checkNetwork()) return;
          onViewConfigClick();
        });
    rlyRefreshToken.setOnClickListener(v -> onRefreshTokenClick());
    tvChat.setOnClickListener(v -> goChat());
    tvDelete.setOnClickListener(v -> showDeleteConfirmDialog());
  }

  /** 配置编辑入口。默认：显示 rlyEdit 并绑定点击。 Fun 版覆写：隐藏 rlyEdit，改为 rlyAvatarName 点击。 */
  protected void setupEditEntry() {
    rlyEdit.setVisibility(View.VISIBLE);
    rlyEdit.setOnClickListener(v -> onEditClick());
  }

  protected void onEditClick() {
    Intent intent = new Intent(this, getEditActivityClass());
    intent.putExtra(RouterConstant.KEY_ACCOUNT_ID_KEY, robotBean.getAccountId());
    intent.putExtra(RouterConstant.KEY_ROBOT_NAME, robotBean.getName());
    intent.putExtra(RouterConstant.KEY_ROBOT_AVATAR, robotBean.getAvatar());
    editLauncher.launch(intent);
  }

  protected abstract Class<?> getEditActivityClass();

  protected void onViewConfigClick() {
    Intent intent = new Intent(this, getViewConfigActivityClass());
    intent.putExtra(RouterConstant.KEY_ACCOUNT_ID_KEY, robotBean.getAccountId());
    startActivity(intent);
  }

  protected Class<?> getViewConfigActivityClass() {
    return com.netease.yunxin.kit.contactkit.ui.normal.robot.RobotViewConfigActivity.class;
  }

  protected void goChat() {
    XKitRouter.withKey(getChatRouterPath())
        .withParam(RouterConstant.CHAT_ID_KRY, robotBean.getAccountId())
        .withContext(this)
        .navigate();
  }

  protected String getChatRouterPath() {
    return RouterConstant.PATH_CHAT_P2P_PAGE;
  }

  protected int getConfirmPositiveColor() {
    return ContextCompat.getColor(this, R.color.normal_page_primary_color);
  }

  /** 检查网络，无网 Toast 提示，返回 false 表示无网 */
  protected boolean checkNetwork() {
    if (!com.netease.yunxin.kit.common.utils.NetworkUtils.isConnected()) {
      Toast.makeText(this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT).show();
      return false;
    }
    return true;
  }

  protected void showDeleteConfirmDialog() {
    if (!checkNetwork()) return;
    CommonChoiceDialog dialog = new CommonChoiceDialog();
    dialog.setPositiveTextColor(ContextCompat.getColor(this, R.color.color_e6605c));
    dialog
        .setTitleStr(getString(R.string.contact_robot_delete_confirm_title))
        .setContentStr(getString(R.string.contact_robot_delete_confirm_message))
        .setNegativeStr(getString(R.string.cancel))
        .setPositiveStr(getString(R.string.contact_robot_delete_confirm_positive))
        .setConfirmListener(
            new ChoiceListener() {
              @Override
              public void onPositive() {
                viewModel.deleteRobot(robotBean.getAccountId());
              }

              @Override
              public void onNegative() {}
            });
    dialog.show(getSupportFragmentManager());
  }

  protected void onRefreshTokenClick() {
    if (!checkNetwork()) return;
    CommonChoiceDialog dialog = new CommonChoiceDialog();
    dialog.setPositiveTextColor(getConfirmPositiveColor());
    dialog
        .setTitleStr(getString(R.string.contact_robot_refresh_token_confirm_title))
        .setContentStr(getString(R.string.contact_robot_refresh_token_confirm_message))
        .setNegativeStr(getString(R.string.cancel))
        .setPositiveStr(getString(R.string.contact_dialog_sure))
        .setConfirmListener(
            new ChoiceListener() {
              @Override
              public void onPositive() {
                viewModel.refreshRobotToken(robotBean.getAccountId());
              }

              @Override
              public void onNegative() {}
            });
    dialog.show(getSupportFragmentManager());
  }
}
