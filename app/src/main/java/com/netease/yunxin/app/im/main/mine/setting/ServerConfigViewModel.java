// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.text.TextUtils;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerConfigViewModel extends BaseViewModel {
  public void saveServerConfig(String config, boolean configSwitch) {

    DataUtils.saveServerConfig(IMKitClient.getApplicationContext(), config);
    DataUtils.saveServerPrivateConfigSwitch(IMKitClient.getApplicationContext(), configSwitch);
  }

  public JSONObject getServerConfig() {
    String serverConfig = DataUtils.getServerConfig(IMKitClient.getApplicationContext());
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

  public String getServerConfigString() {
    return DataUtils.getServerConfig(IMKitClient.getApplicationContext());
  }

  public boolean getServiceConfigSwitch() {
    return DataUtils.getServerPrivateConfigSwitch(IMKitClient.getApplicationContext());
  }
}
