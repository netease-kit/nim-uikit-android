// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.teamkit.ui.R;

public final class NetworkUtilsWrapper {

  public static void doActionAndFilterNetworkBroken(Context context, @NonNull Runnable runnable) {
    if (NetworkUtils.isConnected()) {
      runnable.run();
    } else {
      Toast.makeText(context, R.string.team_network_error, Toast.LENGTH_SHORT).show();
    }
  }

  public static boolean handleNetworkBrokenResult(Context context, ResultInfo<?> resultInfo) {
    if (resultInfo == null) {
      return false;
    }
    if (!resultInfo.getSuccess()) {
      if (!NetworkUtils.isConnected()) {
        Toast.makeText(context, R.string.team_network_error, Toast.LENGTH_SHORT).show();
      } else {
        if (resultInfo != null
            && resultInfo.getMsg() != null
            && resultInfo.getMsg().getCode() == TeamUIKitConstant.QUIT_TEAM_ERROR_CODE_NO_MEMBER) {
          Toast.makeText(context, R.string.team_operate_no_permission_tip, Toast.LENGTH_SHORT)
              .show();
        } else {
          Toast.makeText(context, R.string.team_request_fail, Toast.LENGTH_SHORT).show();
        }
      }
      return true;
    }
    return false;
  }
}
