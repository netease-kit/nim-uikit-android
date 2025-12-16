// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMTeamMsgAckInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserChangedListener;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserManager;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import java.util.List;

/** chat read state info vide model fetch team read state info to read state page */
public class ChatReadStateViewModel extends BaseViewModel {
  private static final String TAG = "ChatReadStateViewModel";

  private final MutableLiveData<IMTeamMsgAckInfo> teamAckInfo = new MutableLiveData<>();

  /** 用户变更监听 */
  private final MutableLiveData<FetchResult<List<String>>> userChangeLiveData =
      new MutableLiveData<>();

  /**
   * 用户变更监听
   *
   * @return 用户变更监听
   */
  public MutableLiveData<FetchResult<List<String>>> getUserChangeLiveData() {
    return userChangeLiveData;
  }

  private final TeamUserChangedListener cacheUserChangedListener =
      new TeamUserChangedListener() {

        @Override
        public void onUsersChanged(List<String> accountIds) {
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success);
          result.setData(accountIds);
          userChangeLiveData.postValue(result);
        }

        @Override
        public void onUserDelete(List<String> accountIds) {}

        @Override
        public void onUsersAdd(List<String> accountIds) {}
      };

  public void fetchTeamAckInfo(V2NIMMessage message) {
    ALog.d(
        LIB_TAG,
        TAG,
        "fetchTeamAckInfo:" + (message == null ? "null" : message.getMessageClientId()));
    if (message == null) {
      return;
    }
    TeamUserManager.getInstance().addMemberChangedListener(cacheUserChangedListener);
    ChatRepo.getTeamMessageReceiptDetail(
        message,
        null,
        new FetchCallback<IMTeamMsgAckInfo>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "fetchTeamAckInfo error:" + errorCode + " errorMsg:" + errorMsg);
            if (!NetworkUtils.isConnected()) {
              ToastX.showShortToast(R.string.chat_network_error_tip);
            } else {
              ToastX.showShortToast(R.string.chat_server_request_fail);
            }
          }

          @Override
          public void onSuccess(@Nullable IMTeamMsgAckInfo data) {
            ALog.d(LIB_TAG, TAG, "fetchTeamAckInfo success:" + data);
            teamAckInfo.postValue(data);
          }
        });
  }

  public MutableLiveData<IMTeamMsgAckInfo> getTeamAckInfoLiveData() {
    return teamAckInfo;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    TeamUserManager.getInstance().removeMemberChangedListener(cacheUserChangedListener);
  }
}
