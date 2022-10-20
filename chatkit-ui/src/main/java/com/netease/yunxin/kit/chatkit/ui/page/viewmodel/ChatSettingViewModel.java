// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;

public class ChatSettingViewModel extends BaseViewModel {
  private static final String TAG = "ChatSettingViewModel";
  private final MutableLiveData<FetchResult<UserInfo>> userInfoLiveData = new MutableLiveData<>();
  private final FetchResult<UserInfo> userInfoFetchResult = new FetchResult<>(LoadStatus.Finish);

  public MutableLiveData<FetchResult<UserInfo>> getUserInfoLiveData() {
    return userInfoLiveData;
  }

  public void getUserInfo(String accId) {
    ALog.d(LIB_TAG, TAG, "getP2pUserInfo:" + accId);
    ChatRepo.fetchUserInfo(
        accId,
        new FetchCallback<UserInfo>() {
          @Override
          public void onSuccess(@Nullable UserInfo param) {
            ALog.d(LIB_TAG, TAG, "getP2pUserInfo,onSuccess:" + (param == null));
            userInfoFetchResult.setData(param);
            userInfoFetchResult.setLoadStatus(LoadStatus.Success);
            userInfoLiveData.setValue(userInfoFetchResult);
          }

          @Override
          public void onFailed(int code) {}

          @Override
          public void onException(@Nullable Throwable exception) {}
        });
  }
}
