// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.robot;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMUserAIBot;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMBindUserAIBotToQrCodeParams;
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
 * 绑定机器人页面 ViewModel
 *
 * <p>负责拉取机器人列表（{@link AIRepo#getUserAIBotList}）和绑定操作（{@link AIRepo#bindUserAIBotToQrCode}）
 */
public class RobotBindViewModel extends BaseViewModel {

  private static final String TAG = "RobotBindViewModel";

  private final MutableLiveData<FetchResult<List<RobotInfoBean>>> robotListLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<FetchResult<Boolean>> bindResultLiveData = new MutableLiveData<>();

  public MutableLiveData<FetchResult<List<RobotInfoBean>>> getRobotListLiveData() {
    return robotListLiveData;
  }

  public MutableLiveData<FetchResult<Boolean>> getBindResultLiveData() {
    return bindResultLiveData;
  }

  /** 拉取机器人列表，传 null 表示不分页过滤，从第一页开始查询 */
  public void loadRobotList() {
    ALog.d(LIB_TAG, TAG, "loadRobotList");
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
            ALog.d(LIB_TAG, TAG, "loadRobotList success, size=" + beanList.size());
            FetchResult<List<RobotInfoBean>> result = new FetchResult<>(LoadStatus.Success);
            result.setData(beanList);
            robotListLiveData.postValue(result);
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.e(LIB_TAG, TAG, "loadRobotList error, code=" + errorCode + ", msg=" + errorMsg);
            FetchResult<List<RobotInfoBean>> result = new FetchResult<>(LoadStatus.Error);
            result.setError(errorCode, errorMsg);
            robotListLiveData.postValue(result);
          }
        });
  }

  /**
   * 将指定机器人绑定到扫码得到的二维码
   *
   * @param botAccountId 机器人的 accountId（accid）
   * @param botToken 机器人的登录密钥（token），从 {@link V2NIMUserAIBot#getToken()} 获取
   * @param qrCode 扫码结果中携带的二维码值（qrCode 字段）
   */
  public void bindRobot(String botAccountId, String botToken, String qrCode) {
    ALog.d(LIB_TAG, TAG, "bindRobot, botAccountId=" + botAccountId);
    V2NIMBindUserAIBotToQrCodeParams params = new V2NIMBindUserAIBotToQrCodeParams();
    params.setAccid(botAccountId);
    params.setToken(botToken);
    params.setQrCode(qrCode);
    AIRepo.bindUserAIBotToQrCode(
        params,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(Void data) {
            ALog.d(LIB_TAG, TAG, "bindRobot success");
            FetchResult<Boolean> result = new FetchResult<>(LoadStatus.Success);
            result.setData(true);
            bindResultLiveData.postValue(result);
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.e(LIB_TAG, TAG, "bindRobot error, code=" + errorCode + ", msg=" + errorMsg);
            FetchResult<Boolean> result = new FetchResult<>(LoadStatus.Error);
            result.setError(errorCode, errorMsg);
            bindResultLiveData.postValue(result);
          }
        });
  }
}
