// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.permission;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelMember;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatRoleOptionEnum;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatRoleResourceEnum;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatMemberPermissionActivityBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import java.util.HashMap;
import java.util.Map;

/** member permission setting modify member's channel permission */
public class QChatMemberPermissionActivity extends BaseActivity {

  private static final String TAG = "QChatMemberPermissionActivity";
  private QChatMemberPermissionActivityBinding viewBiding;
  private MemberPermissionViewModel viewModel;
  private QChatChannelMember channelMember;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ALog.d(TAG, "onCreate");
    changeStatusBarColor(R.color.color_eff1f4);
    viewBiding = QChatMemberPermissionActivityBinding.inflate(getLayoutInflater());
    viewModel = new ViewModelProvider(this).get(MemberPermissionViewModel.class);
    setContentView(viewBiding.getRoot());
    initData();
    initView();
  }

  private void initView() {
    viewBiding.qChatMemberPermissionTitleBar.setTitle(
        R.string.qchat_channel_member_permission_title);
    viewBiding.qChatMemberPermissionTitleBar.setOnBackIconClickListener(
        view -> {
          finish();
        });
    if (channelMember != null) {
      viewBiding.qChatMemberPermissionAvatarIv.setData(
          channelMember.getAvatarUrl(),
          channelMember.getNickName(),
          AvatarColor.avatarColor(channelMember.getAccId()));
      viewBiding.qChatMemberPermissionNameTv.setText(channelMember.getNickName());
      loadPermissionData();
    }
  }

  private void initData() {
    Bundle bundle = getIntent().getExtras();
    if (bundle != null && bundle.containsKey(QChatConstant.CHANNEL_MEMBER)) {
      channelMember = (QChatChannelMember) bundle.get(QChatConstant.CHANNEL_MEMBER);
    }

    if (channelMember == null) {
      finish();
      return;
    }
    ALog.d(TAG, "initData", "channelMember:" + channelMember.toString());
    //observer member permission data change
    viewModel
        .getAddLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                ALog.d(TAG, "AddLiveData", "success");
                channelMember = result.getData();
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                ALog.d(TAG, "AddLiveData", "Error");
                Toast.makeText(
                        this,
                        getResources().getString(result.errorMsg().getRes()),
                        Toast.LENGTH_SHORT)
                    .show();
              }
              loadPermissionData();
            });
  }

  private void loadPermissionData() {
    Map<QChatRoleResourceEnum, QChatRoleOptionEnum> auth = channelMember.getAuths();
    if (auth.size() > 0) {

      QChatRoleOptionEnum optionManagerRole = auth.get(QChatRoleResourceEnum.MANAGE_CHANNEL);
      if (optionManagerRole != null) {
        viewBiding.qChatMemberPermissionAttrSwitch.setStatus(optionManagerRole.ordinal());
        viewBiding.qChatMemberPermissionAttrSwitch.setOnClickListener(
            view ->
                switchChange(
                    QChatRoleResourceEnum.MANAGE_CHANNEL,
                    viewBiding.qChatMemberPermissionAttrSwitch.getCurrentStatus()));
      }

      QChatRoleOptionEnum optionManagerChannel = auth.get(QChatRoleResourceEnum.MANAGE_ROLE);
      if (optionManagerChannel != null) {
        viewBiding.qChatMemberPermissionChannelSwitch.setStatus(optionManagerChannel.ordinal());
        viewBiding.qChatMemberPermissionChannelSwitch.setOnClickListener(
            view ->
                switchChange(
                    QChatRoleResourceEnum.MANAGE_ROLE,
                    viewBiding.qChatMemberPermissionChannelSwitch.getCurrentStatus()));
      }

      QChatRoleOptionEnum optionSendMsg = auth.get(QChatRoleResourceEnum.SEND_MSG);
      if (optionSendMsg != null) {
        viewBiding.qChatMemberPermissionSendMessageSwitch.setStatus(optionSendMsg.ordinal());
        viewBiding.qChatMemberPermissionSendMessageSwitch.setOnClickListener(
            view ->
                switchChange(
                    QChatRoleResourceEnum.SEND_MSG,
                    viewBiding.qChatMemberPermissionSendMessageSwitch.getCurrentStatus()));
      }

      QChatRoleOptionEnum managerMemberOption =
          auth.get(QChatRoleResourceEnum.MANAGE_BLACK_WHITE_LIST);
      if (managerMemberOption != null) {
        viewBiding.qChatMemberPermissionManagerMemberSwitch.setStatus(
            managerMemberOption.ordinal());
        viewBiding.qChatMemberPermissionManagerMemberSwitch.setOnClickListener(
            view ->
                switchChange(
                    QChatRoleResourceEnum.MANAGE_BLACK_WHITE_LIST,
                    viewBiding.qChatMemberPermissionManagerMemberSwitch.getCurrentStatus()));
      }
    }
  }

  private void switchChange(QChatRoleResourceEnum type, int value) {
    Map<QChatRoleResourceEnum, QChatRoleOptionEnum> valueMap = new HashMap<>();
    valueMap.put(type, QChatRoleOptionEnum.Companion.typeOfValue(value));
    ALog.d(TAG, "switchChange", "info:" + type.name() + value);
    viewModel.updateMemberRole(
        channelMember.getServerId(),
        channelMember.getChannelId(),
        channelMember.getAccId(),
        valueMap);
  }

  public static void launch(Activity activity, QChatChannelMember data) {
    Intent intent = new Intent(activity, QChatMemberPermissionActivity.class);
    Bundle bundle = new Bundle();
    bundle.putSerializable(QChatConstant.CHANNEL_MEMBER, data);
    intent.putExtras(bundle);
    activity.startActivity(intent);
  }
}
