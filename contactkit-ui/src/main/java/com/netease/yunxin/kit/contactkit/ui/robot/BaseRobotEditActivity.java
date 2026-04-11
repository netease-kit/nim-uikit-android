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
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.RobotInfoBean;
import com.netease.yunxin.kit.contactkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import java.io.File;

/**
 * 机器人编辑页基类
 *
 * <p>子类在 {@link #initViews()} 中完成布局 inflate、setContentView，并给以下字段赋值。 Base 负责所有通用的选图/名称编辑/保存逻辑。
 */
public abstract class BaseRobotEditActivity extends BaseLocalActivity {

  public static final String KEY_RESULT_AVATAR = "result_avatar";
  public static final String KEY_RESULT_NAME = "result_name";

  // ---------- 子类必须在 initViews() 中赋值的 View 字段 ----------
  protected BackTitleBar titleBar;
  protected ContactAvatarView avatarView;
  protected TextView tvName;
  protected View rlyAvatar;
  protected View rlyName;
  protected TextView tvSave;
  // ---------------------------------------------------------------

  protected RobotInfoViewModel viewModel;
  protected RobotInfoBean robotBean;
  protected String pendingAvatarUrl;
  protected String pendingName;

  private final ActivityResultLauncher<Intent> editNameLauncher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
              String newName = result.getData().getStringExtra(KEY_RESULT_NAME);
              if (!TextUtils.isEmpty(newName)) {
                pendingName = newName;
                tvName.setText(newName);
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
    bar.setTitle(R.string.contact_robot_edit_title);
  }

  private void loadData() {
    viewModel = new ViewModelProvider(this).get(RobotInfoViewModel.class);

    viewModel
        .getUpdateResultLiveData()
        .observe(
            this,
            result -> {
              dismissLoading();
              if (result.getLoadStatus() == LoadStatus.Success) {
                Intent intent = new Intent();
                intent.putExtra(KEY_RESULT_AVATAR, pendingAvatarUrl);
                intent.putExtra(KEY_RESULT_NAME, pendingName);
                setResult(RESULT_OK, intent);
                finish();
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                Toast.makeText(this, R.string.contact_operate_error_tip, Toast.LENGTH_SHORT).show();
              }
            });

    String accountId = getIntent().getStringExtra(RouterConstant.KEY_ACCOUNT_ID_KEY);
    String name = getIntent().getStringExtra(RouterConstant.KEY_ROBOT_NAME);
    String avatar = getIntent().getStringExtra(RouterConstant.KEY_ROBOT_AVATAR);
    if (TextUtils.isEmpty(accountId)) {
      finish();
      return;
    }
    robotBean = new RobotInfoBean(accountId, name, avatar);
    pendingAvatarUrl = avatar;
    pendingName = name;
  }

  private void bindEvents() {
    if (robotBean == null) return;
    refreshAvatarView(pendingAvatarUrl);
    tvName.setText(pendingName);
    rlyAvatar.setOnClickListener(v -> choicePhoto());
    rlyName.setOnClickListener(v -> openNameEditPage());
    tvSave.setOnClickListener(v -> onSaveClick());
  }

  protected void refreshAvatarView(String avatarUrl) {
    avatarView.setData(avatarUrl, pendingName, ColorUtils.avatarColor(robotBean.getAccountId()));
  }

  protected void openNameEditPage() {
    Intent intent = new Intent(this, getNameEditActivityClass());
    intent.putExtra(RouterConstant.KEY_ACCOUNT_ID_KEY, robotBean.getAccountId());
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
    showLoading();
    viewModel.updateRobot(robotBean.getAccountId(), pendingName, pendingAvatarUrl);
  }
}
