// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMTeamMsgAckInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;

/** chat read state info vide model fetch team read state info to read state page */
public class ChatReadStateViewModel extends BaseViewModel {
  private static final String TAG = "ChatReadStateViewModel";

  private final MutableLiveData<IMTeamMsgAckInfo> teamAckInfo = new MutableLiveData<>();

  public void fetchTeamAckInfo(IMMessage message) {
    ALog.d(LIB_TAG, TAG, "fetchTeamAckInfo:" + (message == null ? "null" : message.getUuid()));
    ChatRepo.fetchTeamMessageReceiptDetail(
        message,
        new FetchCallback<IMTeamMsgAckInfo>() {
          @Override
          public void onSuccess(@Nullable IMTeamMsgAckInfo param) {
            teamAckInfo.postValue(param);
          }

          @Override
          public void onFailed(int code) {
            if (!NetworkUtils.isConnected()) {
              ToastX.showShortToast(R.string.chat_network_error_tip);
            } else {
              ToastX.showShortToast(R.string.chat_server_request_fail);
            }
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            if (!NetworkUtils.isConnected()) {
              ToastX.showShortToast(R.string.chat_network_error_tip);
            } else {
              ToastX.showShortToast(R.string.chat_server_request_fail);
            }
          }
        });
  }

  public MutableLiveData<IMTeamMsgAckInfo> getTeamAckInfoLiveData() {
    return teamAckInfo;
  }
}
