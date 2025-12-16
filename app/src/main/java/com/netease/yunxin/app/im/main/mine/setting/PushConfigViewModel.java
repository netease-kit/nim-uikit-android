// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.text.TextUtils;
import com.netease.nimlib.coexist.sdk.v2.message.config.V2NIMMessagePushConfig;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.app.im.utils.MessageUtils;
import com.netease.yunxin.kit.chatkit.IMKitCustomFactory;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import org.json.JSONException;
import org.json.JSONObject;

public class PushConfigViewModel extends BaseViewModel {

  private final String defaultConfig =
      "{"
          + "\"pushNickEnabled\":true,"
          + "\"pushEnabled\":true,"
          + "\"forcePush\":false,"
          + "\"content\":\"\","
          + "\"payload\":\"\","
          + "\"forcePushContent\":\""
          + "\"}";

  public boolean savePushConfig(String config, boolean configSwitch) {
    if (configSwitch) {
      V2NIMMessagePushConfig pushContent = MessageUtils.convertToPushConfig(config);
      if (pushContent != null) {
        DataUtils.savePushConfig(IMKitClient.getApplicationContext(), configSwitch, config);
        IMKitCustomFactory.setPushConfig(pushContent);
      } else {
        ToastX.showShortToast("配置格式错误");
        return false;
      }
    } else {
      IMKitCustomFactory.setPushConfig(null);
    }
    return true;
  }

  public JSONObject getPushConfig() {
    String serverConfig = DataUtils.getPushConfigContent(IMKitClient.getApplicationContext());
    if (!TextUtils.isEmpty(serverConfig)) {
      try {
        JSONObject dataJson = new JSONObject(serverConfig);
        return dataJson;
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    return null;
  }

  public String getPushConfigString() {
    String content = DataUtils.getPushConfigContent(IMKitClient.getApplicationContext());
    if (TextUtils.isEmpty(content)) {
      return defaultConfig;
    } else {
      return content;
    }
  }

  public boolean getPushConfigSwitch() {
    return DataUtils.getPushConfigToggle(IMKitClient.getApplicationContext());
  }
}
