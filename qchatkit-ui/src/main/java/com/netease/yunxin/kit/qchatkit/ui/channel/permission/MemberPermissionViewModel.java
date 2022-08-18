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
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelMember;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatRoleOptionEnum;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatRoleResourceEnum;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import java.util.Map;

/** member permission view mode get or update member permission */
public class MemberPermissionViewModel extends BaseViewModel {

  private static final String TAG = "MemberPermissionViewModel";

  private final MutableLiveData<FetchResult<QChatChannelMember>> addLiveData =
      new MutableLiveData<>();
  private final FetchResult<QChatChannelMember> addResult = new FetchResult<>(LoadStatus.Finish);

  public MutableLiveData<FetchResult<QChatChannelMember>> getAddLiveData() {
    return addLiveData;
  }

  /** update member permission in channel */
  public void updateMemberRole(
      long serverId,
      long channelId,
      String accId,
      Map<QChatRoleResourceEnum, QChatRoleOptionEnum> options) {
    ALog.d(TAG, "updateMemberRole");
    QChatRoleRepo.updateChannelMember(
        serverId,
        channelId,
        accId,
        options,
        new FetchCallback<QChatChannelMember>() {
          @Override
          public void onSuccess(@Nullable QChatChannelMember param) {
            ALog.d(TAG, "updateMemberRole", "onSuccess");
            addResult.setLoadStatus(LoadStatus.Success);
            addResult.setData(param);
            addLiveData.postValue(addResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "updateMemberRole", "onFailed" + code);
            addResult.setError(code, R.string.qchat_channel_permission_update_error);
            addLiveData.postValue(addResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "updateMemberRole", "onException" + errorMsg);
            addResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_MEMBER_ADD,
                R.string.qchat_channel_permission_update_error);
            addLiveData.postValue(addResult);
          }
        });
  }
}
