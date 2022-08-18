// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.permission;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.QChatRoleRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelRoleInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatRoleOptionEnum;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatRoleResourceEnum;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import java.util.Map;

/** role permission view mode get or update role permission */
public class RolePermissionViewModel extends BaseViewModel {

  private static final String TAG = "RolePermissionViewModel";
  private final MutableLiveData<FetchResult<QChatChannelRoleInfo>> rolePermissionLiveData =
      new MutableLiveData<>();
  private final FetchResult<QChatChannelRoleInfo> rolePermissionResult =
      new FetchResult<>(LoadStatus.Finish);

  public MutableLiveData<FetchResult<QChatChannelRoleInfo>> getRolePermissionLiveData() {
    return rolePermissionLiveData;
  }

  /** update role permission in channel */
  public void updateChannelRole(
      long serverId,
      long channelId,
      long roleId,
      Map<QChatRoleResourceEnum, QChatRoleOptionEnum> options) {
    ALog.d(TAG, "updateChannelRole", "info:" + serverId + "," + channelId + "," + roleId);
    QChatRoleRepo.updateChannelRole(
        serverId,
        channelId,
        roleId,
        options,
        new FetchCallback<QChatChannelRoleInfo>() {
          @Override
          public void onSuccess(@Nullable QChatChannelRoleInfo param) {
            ALog.d(TAG, "updateChannelRole", "onSuccess");
            rolePermissionResult.setLoadStatus(LoadStatus.Success);
            rolePermissionResult.setData(param);
            rolePermissionLiveData.postValue(rolePermissionResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "updateChannelRole", "onFailed:" + code);
            rolePermissionResult.setError(code, R.string.qchat_channel_permission_update_error);
            rolePermissionLiveData.postValue(rolePermissionResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "updateChannelRole", "onException:" + errorMsg);
            rolePermissionResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_MEMBER_ADD,
                R.string.qchat_channel_permission_update_error);
            rolePermissionLiveData.postValue(rolePermissionResult);
          }
        });
  }
}
