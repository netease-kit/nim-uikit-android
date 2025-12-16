// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.coexist.sdk.NIMClient;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;

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

  public boolean getToggleNotification(Context context) {
    return DataUtils.getToggleNotification(context);
  }

  public void setToggleNotification(Context context, boolean value) {
    IMKitClient.toggleNotification(value);
    DataUtils.saveToggleNotification(context, value);
    ToastX.showShortToast(R.string.setting_success);
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

  public boolean getPushShowNoDetail(Context context) {
    return DataUtils.getNotificationHideContent(context);
  }

  public void setPushShowNoDetail(Context context, boolean mode) {
    NIMClient.getSDKOptions().statusBarNotificationConfig.hideContent = mode;
    DataUtils.saveNotificationHideContent(context, mode);
    ToastX.showShortToast(R.string.setting_success);
  }
}
