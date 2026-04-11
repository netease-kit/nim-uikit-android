// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.robot;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMUserAIBot;
import com.netease.nimlib.sdk.v2.ai.result.V2NIMGetUserAIBotListResult;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.AIRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.model.RobotInfoBean;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.List;

/**
 * 我的机器人列表 ViewModel
 *
 * <p>通过 {@link AIRepo#getUserAIBotList} 拉取用户级 AI Bot 列表并转换为 {@link RobotInfoBean}
 */
public class RobotListViewModel extends BaseViewModel {

  private static final String TAG = "RobotListViewModel";

  private final MutableLiveData<FetchResult<List<RobotInfoBean>>> resultLiveData =
      new MutableLiveData<>();

  public MutableLiveData<FetchResult<List<RobotInfoBean>>> getRobotListLiveData() {
    return resultLiveData;
  }

  /** 拉取机器人列表，传 null 表示不过滤，从第一页开始查询 */
  public void getRobotList() {
    ALog.d(LIB_TAG, TAG, "getRobotList");
    AIRepo.getUserAIBotList(
        null,
        new FetchCallback<V2NIMGetUserAIBotListResult>() {
          @Override
          public void onSuccess(V2NIMGetUserAIBotListResult data) {
            List<RobotInfoBean> beanList = new ArrayList<>();
            if (data != null && data.getBots() != null) {
              for (V2NIMUserAIBot bot : data.getBots()) {
                beanList.add(new RobotInfoBean(bot));
              }
            }
            ALog.d(LIB_TAG, TAG, "getRobotList success, size=" + beanList.size());
            FetchResult<List<RobotInfoBean>> fetchResult = new FetchResult<>(LoadStatus.Success);
            fetchResult.setData(beanList);
            resultLiveData.postValue(fetchResult);
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.e(LIB_TAG, TAG, "getRobotList error, code=" + errorCode + ", msg=" + errorMsg);
            FetchResult<List<RobotInfoBean>> fetchResult = new FetchResult<>(LoadStatus.Error);
            fetchResult.setError(errorCode, errorMsg);
            resultLiveData.postValue(fetchResult);
          }
        });
  }
}
