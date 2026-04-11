// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.robot;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMUserAIBot;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMCreateUserAIBotParams;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMDeleteUserAIBotParams;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMGetUserAIBotParams;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMRefreshUserAIBotTokenParams;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMUpdateUserAIBotParams;
import com.netease.nimlib.sdk.v2.ai.result.V2NIMCreateUserAIBotResult;
import com.netease.nimlib.sdk.v2.ai.result.V2NIMRefreshUserAIBotTokenResult;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.AIRepo;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.model.RobotInfoBean;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.Arrays;

/**
 * 机器人信息页 ViewModel
 *
 * <p>负责机器人的增删改及刷新 Token 操作，统一通过 {@link AIRepo} 调用底层 SDK。
 */
public class RobotInfoViewModel extends BaseViewModel {

  private static final String TAG = "RobotInfoViewModel";

  private final MutableLiveData<FetchResult<Boolean>> deleteResultLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<FetchResult<Boolean>> updateResultLiveData =
      new MutableLiveData<>();

  /** 创建成功时携带新建机器人的 accountId，便于后续跳转 */
  private final MutableLiveData<FetchResult<String>> createResultLiveData = new MutableLiveData<>();

  /** 刷新 Token 成功时携带新 Token 字符串 */
  private final MutableLiveData<FetchResult<String>> refreshTokenResultLiveData =
      new MutableLiveData<>();

  /** 拉取单个机器人完整信息的 LiveData */
  private final MutableLiveData<FetchResult<RobotInfoBean>> robotInfoLiveData =
      new MutableLiveData<>();

  public MutableLiveData<FetchResult<Boolean>> getDeleteResultLiveData() {
    return deleteResultLiveData;
  }

  public MutableLiveData<FetchResult<Boolean>> getUpdateResultLiveData() {
    return updateResultLiveData;
  }

  public MutableLiveData<FetchResult<String>> getCreateResultLiveData() {
    return createResultLiveData;
  }

  public MutableLiveData<FetchResult<String>> getRefreshTokenResultLiveData() {
    return refreshTokenResultLiveData;
  }

  public MutableLiveData<FetchResult<RobotInfoBean>> getRobotInfoLiveData() {
    return robotInfoLiveData;
  }

  /**
   * 拉取单个机器人的完整信息
   *
   * <p>成功后通过 {@link #getRobotInfoLiveData()} 回调 {@link RobotInfoBean}（包装 SDK 对象）
   *
   * @param accountId 机器人的 accountId
   */
  public void loadRobotInfo(String accountId) {
    ALog.d(LIB_TAG, TAG, "loadRobotInfo, accountId=" + accountId);
    // V2NIMGetUserAIBotParams 字段：accid (setAccid / getAccid)
    V2NIMGetUserAIBotParams params = new V2NIMGetUserAIBotParams();
    params.setAccid(accountId);
    AIRepo.getUserAIBot(
        params,
        new FetchCallback<V2NIMUserAIBot>() {
          @Override
          public void onSuccess(V2NIMUserAIBot data) {
            ALog.d(LIB_TAG, TAG, "loadRobotInfo success");
            FetchResult<RobotInfoBean> result = new FetchResult<>(LoadStatus.Success);
            result.setData(data != null ? new RobotInfoBean(data) : null);
            robotInfoLiveData.postValue(result);
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.e(LIB_TAG, TAG, "loadRobotInfo error, code=" + errorCode + ", msg=" + errorMsg);
            FetchResult<RobotInfoBean> result = new FetchResult<>(LoadStatus.Error);
            result.setError(errorCode, errorMsg);
            robotInfoLiveData.postValue(result);
          }
        });
  }

  /**
   * 删除机器人
   *
   * @param accountId 机器人的 accountId
   */
  public void deleteRobot(String accountId) {
    ALog.d(LIB_TAG, TAG, "deleteRobot, accountId=" + accountId);
    V2NIMDeleteUserAIBotParams params = new V2NIMDeleteUserAIBotParams();
    params.setAccid(accountId);
    AIRepo.deleteUserAIBot(
        params,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(Void data) {
            ALog.d(LIB_TAG, TAG, "deleteRobot success");
            FetchResult<Boolean> result = new FetchResult<>(LoadStatus.Success);
            result.setData(true);
            deleteResultLiveData.postValue(result);
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.e(LIB_TAG, TAG, "deleteRobot error, code=" + errorCode + ", msg=" + errorMsg);
            FetchResult<Boolean> result = new FetchResult<>(LoadStatus.Error);
            result.setError(errorCode, errorMsg);
            deleteResultLiveData.postValue(result);
          }
        });
  }

  /**
   * 更新机器人昵称和头像
   *
   * @param accountId 机器人 accountId
   * @param name 新昵称（null 表示不修改）
   * @param avatar 新头像 URL（null 表示不修改）
   */
  public void updateRobot(String accountId, String name, String avatar) {
    ALog.d(LIB_TAG, TAG, "updateRobot, accountId=" + accountId);
    V2NIMUpdateUserAIBotParams params = new V2NIMUpdateUserAIBotParams();
    params.setAccid(accountId);
    params.setName(name);
    params.setIcon(avatar);
    AIRepo.updateUserAIBot(
        params,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(Void data) {
            ALog.d(LIB_TAG, TAG, "updateRobot success");
            FetchResult<Boolean> result = new FetchResult<>(LoadStatus.Success);
            result.setData(true);
            updateResultLiveData.postValue(result);
            updateRobotUserInfo(accountId);
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.e(LIB_TAG, TAG, "updateRobot error, code=" + errorCode + ", msg=" + errorMsg);
            FetchResult<Boolean> result = new FetchResult<>(LoadStatus.Error);
            result.setError(errorCode, errorMsg);
            updateResultLiveData.postValue(result);
          }
        });
  }

  private void updateRobotUserInfo(String accountId) {
    ContactRepo.getUserListFromCloud(Arrays.asList(accountId), null);
  }

  /**
   * 创建机器人，成功后通过 LiveData 返回新建机器人的 accountId
   *
   * @param name 机器人昵称
   * @param avatar 机器人头像 URL
   */
  public void createRobot(String accid, String name, String avatar) {
    ALog.d(LIB_TAG, TAG, "createRobot, accid=" + accid + ", name=" + name);
    V2NIMCreateUserAIBotParams params = new V2NIMCreateUserAIBotParams();
    params.setAccid(accid);
    params.setName(name);
    if (avatar != null) {
      params.setIcon(avatar);
    }
    AIRepo.createUserAIBot(
        params,
        new FetchCallback<V2NIMCreateUserAIBotResult>() {
          @Override
          public void onSuccess(V2NIMCreateUserAIBotResult data) {
            String token = data != null ? data.getToken() : null;
            ALog.d(LIB_TAG, TAG, "createRobot success, token=" + token);
            FetchResult<String> result = new FetchResult<>(LoadStatus.Success);
            result.setData(token);
            createResultLiveData.postValue(result);
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.e(LIB_TAG, TAG, "createRobot error, code=" + errorCode + ", msg=" + errorMsg);
            FetchResult<String> result = new FetchResult<>(LoadStatus.Error);
            result.setError(errorCode, errorMsg);
            createResultLiveData.postValue(result);
          }
        });
  }

  /**
   * 刷新机器人登录 Token，成功后通过 LiveData 返回新 Token 字符串
   *
   * @param accountId 机器人的 accountId
   */
  public void refreshRobotToken(String accountId) {
    ALog.d(LIB_TAG, TAG, "refreshRobotToken, accountId=" + accountId);
    V2NIMRefreshUserAIBotTokenParams params = new V2NIMRefreshUserAIBotTokenParams();
    params.setAccid(accountId);
    AIRepo.refreshUserAIBotToken(
        params,
        new FetchCallback<V2NIMRefreshUserAIBotTokenResult>() {
          @Override
          public void onSuccess(V2NIMRefreshUserAIBotTokenResult data) {
            String newToken = data != null ? data.getToken() : null;
            ALog.d(LIB_TAG, TAG, "refreshRobotToken success");
            FetchResult<String> result = new FetchResult<>(LoadStatus.Success);
            result.setData(newToken);
            refreshTokenResultLiveData.postValue(result);
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.e(
                LIB_TAG, TAG, "refreshRobotToken error, code=" + errorCode + ", msg=" + errorMsg);
            FetchResult<String> result = new FetchResult<>(LoadStatus.Error);
            result.setError(errorCode, errorMsg);
            refreshTokenResultLiveData.postValue(result);
          }
        });
  }
}
