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
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.chatkit.repo.ResourceRepo;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.photo.BasePhotoChoiceDialog;
import com.netease.yunxin.kit.common.ui.photo.PhotoChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.CommonCallback;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.ContactConstant;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.RobotInfoBean;
import com.netease.yunxin.kit.contactkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import java.io.File;

/**
 * 机器人创建页基类
 *
 * <p>子类在 {@link #initViews()} 中完成布局 inflate、setContentView，并给以下字段赋值。 Base 负责所有通用的选图/名称编辑/创建逻辑。
 */
public abstract class BaseRobotCreateActivity extends BaseLocalActivity {

  /** 进入页面时生成的 accid，贯穿整个创建流程（名称修改、头像上传、提交创建、跳转信息页） */
  private String pendingAccid;

  // ---------- 子类必须在 initViews() 中赋值的 View 字段 ----------
  protected BackTitleBar titleBar;
  protected ContactAvatarView avatarView;
  protected TextView tvName;
  protected View rlyAvatar;
  protected View rlyName;
  protected TextView tvSave;
  // ---------------------------------------------------------------

  protected RobotInfoViewModel viewModel;
  protected RobotBindViewModel bindViewModel;
  protected RobotInfoBean robotBean;
  protected String pendingAvatarUrl;
  protected String pendingName;

  /** 是否从绑定页进入，决定创建成功后的后续流程 */
  private boolean fromBindPage;
  /** 绑定时使用的 qrCodeId */
  private String bindQrCodeId;

  private final ActivityResultLauncher<Intent> editNameLauncher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
              String newName =
                  result.getData().getStringExtra(BaseRobotEditActivity.KEY_RESULT_NAME);
              if (!TextUtils.isEmpty(newName)) {
                pendingName = newName;
                tvName.setText(newName);
                // 保留同一个 pendingAccid，只更新名称
                robotBean = new RobotInfoBean(pendingAccid, newName, robotBean.getAvatar());
                refreshAvatarView(pendingAvatarUrl);
              }
            }
          });

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

  /** 子类可覆写定制标题栏 */
  protected void configTitle(BackTitleBar bar) {
    bar.setOnBackIconClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    bar.setTitle(R.string.contact_robot_create_title);
  }

  private void loadData() {
    fromBindPage = getIntent().getBooleanExtra(RouterConstant.KEY_FROM_BIND_PAGE, false);
    bindQrCodeId = getIntent().getStringExtra(RouterConstant.KEY_ROBOT_QR_CODE_ID);
    if (bindQrCodeId == null) bindQrCodeId = "";

    viewModel = new ViewModelProvider(this).get(RobotInfoViewModel.class);
    bindViewModel = new ViewModelProvider(this).get(RobotBindViewModel.class);

    // 观察创建结果
    viewModel
        .getCreateResultLiveData()
        .observe(
            this,
            result -> {
              dismissLoading();
              if (result.getLoadStatus() == LoadStatus.Success) {
                if (fromBindPage) {
                  // 从绑定页来：创建成功后继续绑定
                  showLoading();
                  bindViewModel.bindRobot(pendingAccid, result.getData(), bindQrCodeId);
                } else {
                  // 从列表页来：直接跳转信息页
                  navigateToRobotInfo();
                }
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                Toast.makeText(this, R.string.contact_operate_error_tip, Toast.LENGTH_SHORT).show();
              }
            });

    // 观察绑定结果（仅 fromBindPage 时触发）
    bindViewModel
        .getBindResultLiveData()
        .observe(
            this,
            result -> {
              dismissLoading();
              if (result.getLoadStatus() == LoadStatus.Success) {
                // 绑定成功：提示 + 跳转信息页，并通知绑定页关闭
                Toast.makeText(this, R.string.contact_robot_bind_success, Toast.LENGTH_SHORT)
                    .show();
                setResult(RESULT_OK);
                navigateToRobotInfo();
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                // 绑定失败：关闭创建页，回到绑定页
                int errorCode = result.getError() != null ? result.getError().getCode() : 0;
                int msgRes =
                    errorCode == ContactConstant.ERROR_QRCODE_BIND_USED
                        ? R.string.contact_robot_qrcode_bind_used
                        : R.string.contact_operate_error_tip;
                Toast.makeText(this, msgRes, Toast.LENGTH_SHORT).show();
                finish();
              }
            });

    // 进入页面时就生成 accid，整个创建流程复用同一个值
    pendingAccid = com.netease.yunxin.kit.contactkit.ui.utils.RobotUtils.generateAccid();
    String intentName = getIntent().getStringExtra(RouterConstant.KEY_ROBOT_NAME);
    pendingName = intentName;
    pendingAvatarUrl = null;
    robotBean = new RobotInfoBean(pendingAccid, pendingName, null);
  }

  private void bindEvents() {
    if (!TextUtils.isEmpty(pendingName)) {
      tvName.setText(pendingName);
    }
    refreshAvatarView(null);
    rlyAvatar.setOnClickListener(v -> choicePhoto());
    rlyName.setOnClickListener(v -> openNameEditPage());
    tvSave.setOnClickListener(v -> onSaveClick());
  }

  protected void refreshAvatarView(String avatarUrl) {
    avatarView.setData(
        avatarUrl, robotBean.getName(), ColorUtils.avatarColor(robotBean.getAccountId()));
  }

  protected void openNameEditPage() {
    Intent intent = new Intent(this, getNameEditActivityClass());
    intent.putExtra(RouterConstant.KEY_ACCOUNT_ID_KEY, pendingAccid);
    intent.putExtra(RouterConstant.KEY_ROBOT_NAME, pendingName);
    editNameLauncher.launch(intent);
  }

  protected abstract Class<?> getNameEditActivityClass();

  protected void choicePhoto() {
    getPhotoChoiceDialog()
        .show(
            new CommonCallback<File>() {
              @Override
              public void onSuccess(@Nullable File param) {
                if (!NetworkUtils.isConnected()) {
                  Toast.makeText(
                          getApplicationContext(),
                          R.string.contact_network_error_tip,
                          Toast.LENGTH_SHORT)
                      .show();
                  return;
                }
                if (param == null) return;
                showLoading();
                ResourceRepo.uploadFile(
                    param,
                    new FetchCallback<String>() {
                      @Override
                      public void onError(int errorCode, @Nullable String errorMsg) {
                        dismissLoading();
                        Toast.makeText(
                                getApplicationContext(),
                                R.string.contact_operate_error_tip,
                                Toast.LENGTH_SHORT)
                            .show();
                      }

                      @Override
                      public void onSuccess(@Nullable String urlParam) {
                        dismissLoading();
                        pendingAvatarUrl = urlParam;
                        refreshAvatarView(urlParam);
                      }
                    });
              }

              @Override
              public void onFailed(int code) {
                Toast.makeText(
                        getApplicationContext(),
                        R.string.contact_operate_error_tip,
                        Toast.LENGTH_SHORT)
                    .show();
              }

              @Override
              public void onException(@Nullable Throwable exception) {
                Toast.makeText(
                        getApplicationContext(),
                        R.string.contact_operate_error_tip,
                        Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  protected BasePhotoChoiceDialog getPhotoChoiceDialog() {
    return new PhotoChoiceDialog(this);
  }

  protected void onSaveClick() {
    if (!NetworkUtils.isConnected()) {
      Toast.makeText(this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT).show();
      return;
    }
    if (TextUtils.isEmpty(pendingName)) {
      Toast.makeText(this, R.string.contact_robot_create_name_required, Toast.LENGTH_SHORT).show();
      return;
    }
    showLoading();
    // pendingAccid 在 loadData() 时已生成，直接使用
    viewModel.createRobot(pendingAccid, pendingName, pendingAvatarUrl);
  }

  /** 创建成功后跳转到机器人信息页，子类可覆写以指定不同路由 */
  protected void navigateToRobotInfo() {
    com.netease.yunxin.kit.corekit.route.XKitRouter.withKey(getRobotInfoRouterPath())
        .withContext(this)
        .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, pendingAccid)
        .withParam(RouterConstant.KEY_ROBOT_NAME, pendingName)
        .withParam(RouterConstant.KEY_ROBOT_AVATAR, pendingAvatarUrl)
        .navigate();
    finish();
  }

  /** 返回机器人信息页路由，Fun 版子类可覆写 */
  protected String getRobotInfoRouterPath() {
    return RouterConstant.PATH_MY_ROBOT_INFO_PAGE;
  }
}
