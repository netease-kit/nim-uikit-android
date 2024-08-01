// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.addfriend;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;

/** 添加好友ViewModel 提供根据账号ID的搜索功能 */
public class AddFriendViewModel extends BaseViewModel {
  private static final String TAG = "AddFriendViewModel";
  private final MutableLiveData<FetchResult<UserWithFriend>> resultLiveData =
      new MutableLiveData<>();
  private final FetchResult<UserWithFriend> fetchResult = new FetchResult<>(LoadStatus.Finish);

  // 获取搜索结果LiveData
  public MutableLiveData<FetchResult<UserWithFriend>> getFetchResult() {
    return resultLiveData;
  }

  /**
   * 根据账号ID搜索用户
   *
   * @param account 账号ID
   */
  public void getUser(String account) {
    ALog.d(LIB_TAG, TAG, "getUser:" + account);
    fetchResult.setStatus(LoadStatus.Loading);
    resultLiveData.postValue(fetchResult);
    ContactRepo.getFriendUserInfo(
        account,
        false,
        new FetchCallback<UserWithFriend>() {
          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getUser,onError,onFailed:" + errorCode);
            fetchResult.setError(errorCode, errorMsg);
            resultLiveData.postValue(fetchResult);
          }

          @Override
          public void onSuccess(@Nullable UserWithFriend param) {
            ALog.d(LIB_TAG, TAG, "getUser,onSuccess:" + (param == null));
            if (param != null) {
              fetchResult.setStatus(LoadStatus.Success);
              fetchResult.setData(param);
            } else {
              fetchResult.setData(null);
              fetchResult.setStatus(LoadStatus.Success);
            }
            resultLiveData.postValue(fetchResult);
          }
        });
  }
}
