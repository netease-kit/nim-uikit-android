// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.permission;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelRoleInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatRoleOptionEnum;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatRoleResourceEnum;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatRolePermissionActivityBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import java.util.HashMap;
import java.util.Map;

/** role permission activity modify role's permission in channel */
public class QChatRolePermissionActivity extends BaseActivity {

  private QChatRolePermissionActivityBinding viewBiding;
  private RolePermissionViewModel viewModel;
  private QChatChannelRoleInfo roleInfo;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_eff1f4);
    viewBiding = QChatRolePermissionActivityBinding.inflate(getLayoutInflater());
    viewModel = new ViewModelProvider(this).get(RolePermissionViewModel.class);
    setContentView(viewBiding.getRoot());
    initData();
    initView();
  }

  private void initData() {
    Bundle bundle = getIntent().getExtras();
    if (bundle != null && bundle.containsKey(QChatConstant.CHANNEL_ROLE)) {
      roleInfo = (QChatChannelRoleInfo) bundle.get(QChatConstant.CHANNEL_ROLE);
    }

    if (roleInfo == null) {
      finish();
      return;
    }

    viewModel
        .getRolePermissionLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                roleInfo = result.getData();
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                String errorMsg = null;
                if (result.errorMsg() != null) {
                  errorMsg = getResources().getString(result.errorMsg().getRes());
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
              }
              loadPermissionData();
            });
  }

  private void initView() {
    viewBiding
        .qChatRolePermissionTitleBar
        .getTitleTextView()
        .setEllipsize(TextUtils.TruncateAt.MIDDLE);
    viewBiding.qChatRolePermissionTitleBar.setOnBackIconClickListener(
        view -> {
          finish();
        });
    if (roleInfo != null) {
      String title = getResources().getString(R.string.qchat_role_permission_title);
      viewBiding.qChatRolePermissionTitleBar.setTitle(String.format(title, roleInfo.getName()));
      loadPermissionData();
    }
  }

  private void loadPermissionData() {
    Map<QChatRoleResourceEnum, QChatRoleOptionEnum> auth = roleInfo.getAuths();
    if (auth.size() > 0) {
      QChatRoleOptionEnum optionManagerRole = auth.get(QChatRoleResourceEnum.MANAGE_CHANNEL);
      if (optionManagerRole != null) {
        viewBiding.qChatRolePermissionAttrSwitch.setStatus(optionManagerRole.ordinal());
        viewBiding.qChatRolePermissionAttrSwitch.setOnClickListener(
            view ->
                switchChange(
                    QChatRoleResourceEnum.MANAGE_CHANNEL,
                    viewBiding.qChatRolePermissionAttrSwitch.getCurrentStatus()));
      }

      QChatRoleOptionEnum optionManagerChannel = auth.get(QChatRoleResourceEnum.MANAGE_ROLE);
      if (optionManagerChannel != null) {
        viewBiding.qChatRolePermissionChannelSwitch.setStatus(optionManagerChannel.ordinal());
        viewBiding.qChatRolePermissionChannelSwitch.setOnClickListener(
            view ->
                switchChange(
                    QChatRoleResourceEnum.MANAGE_ROLE,
                    viewBiding.qChatRolePermissionChannelSwitch.getCurrentStatus()));
      }

      QChatRoleOptionEnum optionSendMsg = auth.get(QChatRoleResourceEnum.SEND_MSG);
      if (optionSendMsg != null) {
        viewBiding.qChatRolePermissionSendMessageSwitch.setStatus(optionSendMsg.ordinal());
        viewBiding.qChatRolePermissionSendMessageSwitch.setOnClickListener(
            view ->
                switchChange(
                    QChatRoleResourceEnum.SEND_MSG,
                    viewBiding.qChatRolePermissionSendMessageSwitch.getCurrentStatus()));
      }

      QChatRoleOptionEnum optionMemberSelf =
          auth.get(QChatRoleResourceEnum.MANAGE_BLACK_WHITE_LIST);
      if (optionMemberSelf != null) {
        viewBiding.qChatRolePermissionManagerMemberSwitch.setStatus(optionMemberSelf.ordinal());
        viewBiding.qChatRolePermissionManagerMemberSwitch.setOnClickListener(
            view ->
                switchChange(
                    QChatRoleResourceEnum.MANAGE_BLACK_WHITE_LIST,
                    viewBiding.qChatRolePermissionManagerMemberSwitch.getCurrentStatus()));
      }
    }
  }

  private void switchChange(QChatRoleResourceEnum type, int value) {
    Map<QChatRoleResourceEnum, QChatRoleOptionEnum> valueMap = new HashMap<>();
    valueMap.put(type, QChatRoleOptionEnum.Companion.typeOfValue(value));
    viewModel.updateChannelRole(
        roleInfo.getServerId(), roleInfo.getChannelId(), roleInfo.getRoleId(), valueMap);
  }

  public static void launch(Activity activity, QChatChannelRoleInfo data) {
    Intent intent = new Intent(activity, QChatRolePermissionActivity.class);
    Bundle bundle = new Bundle();
    bundle.putSerializable(QChatConstant.CHANNEL_ROLE, data);
    intent.putExtras(bundle);
    activity.startActivity(intent);
  }
}
