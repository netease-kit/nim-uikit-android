// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.team;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;

/** 添加好友ViewModel 提供根据账号ID的搜索功能 */
public class JoinTeamViewModel extends BaseViewModel {
  private static final String TAG = "JoinTeamViewModel";
  private final MutableLiveData<FetchResult<V2NIMTeam>> resultLiveData = new MutableLiveData<>();
  private final FetchResult<V2NIMTeam> fetchResult = new FetchResult<>(LoadStatus.Finish);

  // 获取搜索结果LiveData
  public MutableLiveData<FetchResult<V2NIMTeam>> getFetchResult() {
    return resultLiveData;
  }

  /**
   * 根据账号ID搜索用户
   *
   * @param teamId 账号ID
   */
  public void getTeam(String teamId) {
    ALog.d(LIB_TAG, TAG, "getTeam:" + teamId);
    fetchResult.setStatus(LoadStatus.Loading);
    resultLiveData.postValue(fetchResult);
    TeamRepo.getTeamInfo(
        teamId,
        new FetchCallback<V2NIMTeam>() {
          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getTeam,onError,onFailed:" + errorCode);
            fetchResult.setError(errorCode, errorMsg);
            resultLiveData.postValue(fetchResult);
          }

          @Override
          public void onSuccess(@Nullable V2NIMTeam param) {
            ALog.d(LIB_TAG, TAG, "getTeam,onSuccess:" + (param == null));
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
