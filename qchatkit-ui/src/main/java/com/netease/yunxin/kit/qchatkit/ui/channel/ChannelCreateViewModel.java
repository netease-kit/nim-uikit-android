// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.QChatChannelRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelModeEnum;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelTypeEnum;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;

/** create channel view model */
public class ChannelCreateViewModel extends BaseViewModel {

  private static final String TAG = "ChannelCreateViewModel";
  private final MutableLiveData<FetchResult<QChatChannelInfo>> resultLiveData =
      new MutableLiveData<>();
  private final FetchResult<QChatChannelInfo> fetchResult = new FetchResult<>(LoadStatus.Finish);

  public MutableLiveData<FetchResult<QChatChannelInfo>> getFetchResult() {
    return resultLiveData;
  }

  /** create new channel */
  public void createChannel(
      Long serverId, String name, String topic, QChatChannelModeEnum typeEnum) {
    ALog.d(TAG, "createChannel", "param:" + serverId + "," + name + "," + topic);
    QChatChannelRepo.createChannel(
        serverId,
        name,
        topic,
        typeEnum,
        QChatChannelTypeEnum.Message,
        new FetchCallback<QChatChannelInfo>() {
          @Override
          public void onSuccess(@Nullable QChatChannelInfo param) {
            ALog.d(TAG, "createChannel", "onSuccess");
            fetchResult.setStatus(LoadStatus.Success);
            fetchResult.setData(param);
            resultLiveData.postValue(fetchResult);
          }

          @Override
          public void onFailed(int code) {
            if (code == QChatConstant.ERROR_CODE_IM_NO_PERMISSION) {
              fetchResult.setError(code, R.string.qchat_no_permission);
            } else {
              fetchResult.setError(code, R.string.qchat_channel_create_error);
            }
            resultLiveData.postValue(fetchResult);
            ALog.d(TAG, "createChannel", "onFailed" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            fetchResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_CREATE, R.string.qchat_channel_create_error);
            resultLiveData.postValue(fetchResult);
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "createChannel", "onException:" + errorMsg);
          }
        });
  }
}
