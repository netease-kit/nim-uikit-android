// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.content.Context;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;

public class SettingViewModel extends BaseViewModel {

  public boolean getShowReadStatus() {
    return SettingRepo.getShowReadStatus();
  }

  public void setShowReadStatus(boolean delete) {
    SettingRepo.setShowReadStatus(delete);
  }

  public boolean getLocalConversation(Context context) {
    return DataUtils.getLocalConversationConfigSwitch(context);
  }

  public void setLocalConversation(Context context, boolean isLocal) {
    DataUtils.saveLocalConversationConfigSwitch(context, isLocal);
  }

  public boolean getAudioPlayMode() {
    return SettingRepo.getHandsetMode();
  }

  public void setAudioPlayMode(boolean mode) {
    SettingRepo.setHandsetMode(mode);
  }
}
