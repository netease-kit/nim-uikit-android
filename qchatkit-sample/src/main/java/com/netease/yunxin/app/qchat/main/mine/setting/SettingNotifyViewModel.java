// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.main.mine.setting;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.app.qchat.R;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.SettingRepo;

public class SettingNotifyViewModel extends BaseViewModel {

  private final MutableLiveData<FetchResult<Boolean>> notifyDetailLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<Boolean>> toggleNotificationLiveDataLiveData =
      new MutableLiveData<>();

  public MutableLiveData<FetchResult<Boolean>> getNotifyDetailLiveData() {
    return notifyDetailLiveData;
  }

  public MutableLiveData<FetchResult<Boolean>> getToggleNotificationLiveData() {
    return toggleNotificationLiveDataLiveData;
  }

  public boolean getToggleNotification() {
    return SettingRepo.isPushNotify();
  }

  public void setToggleNotification(boolean value) {
    SettingRepo.setMessageNotification(value);
    SettingRepo.setPushNotify(
        value,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ToastX.showShortToast(R.string.setting_success);
            FetchResult<Boolean> fetchResult = new FetchResult<>(LoadStatus.Success);
            fetchResult.setData(value);
            toggleNotificationLiveDataLiveData.postValue(fetchResult);
          }

          @Override
          public void onFailed(int code) {
            ToastX.showShortToast(R.string.setting_fail);
            FetchResult<Boolean> fetchResult = new FetchResult<>(LoadStatus.Error);
            fetchResult.setData(value);
            toggleNotificationLiveDataLiveData.postValue(fetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ToastX.showShortToast(R.string.setting_fail);
            FetchResult<Boolean> fetchResult = new FetchResult<>(LoadStatus.Error);
            fetchResult.setData(value);
            toggleNotificationLiveDataLiveData.postValue(fetchResult);
          }
        });
  }

  public boolean getRingToggle() {
    return SettingRepo.getRingMode();
  }

  public void setRingToggle(boolean ring) {
    SettingRepo.setRingMode(ring);
  }

  public boolean getVibrateToggle() {
    return SettingRepo.getVibrateMode();
  }

  public void setVibrateToggle(boolean mode) {
    SettingRepo.setVibrateMode(mode);
  }

  public boolean getMultiPortPushOpen() {
    return SettingRepo.getMultiPortPushMode();
  }

  public void setMultiPortPushOpen(boolean mode) {
    SettingRepo.setMultiPortPushMode(mode);
  }

  public boolean getPushShowNoDetail() {
    return !SettingRepo.getPushShowDetail();
  }

  public void setPushShowNoDetail(boolean mode) {
    SettingRepo.setPushShowDetail(
        mode,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ToastX.showShortToast(R.string.setting_success);
            FetchResult<Boolean> fetchResult = new FetchResult<>(LoadStatus.Success);
            fetchResult.setData(mode);
            notifyDetailLiveData.postValue(fetchResult);
          }

          @Override
          public void onFailed(int code) {
            ToastX.showShortToast(R.string.setting_fail);
            FetchResult<Boolean> fetchResult = new FetchResult<>(LoadStatus.Error);
            fetchResult.setData(mode);
            notifyDetailLiveData.postValue(fetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ToastX.showShortToast(R.string.setting_fail);
            FetchResult<Boolean> fetchResult = new FetchResult<>(LoadStatus.Error);
            fetchResult.setData(mode);
            notifyDetailLiveData.postValue(fetchResult);
          }
        });
  }
}
