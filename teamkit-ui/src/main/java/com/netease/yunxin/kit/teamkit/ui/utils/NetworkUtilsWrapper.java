// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.teamkit.ui.R;

/**
 * 网络工具类，统一处理网络相关的操作
 *
 * <p>
 */
public final class NetworkUtilsWrapper {

  public static void doActionAndFilterNetworkBroken(Context context, @NonNull Runnable runnable) {
    if (NetworkUtils.isConnected()) {
      runnable.run();
    } else {
      Toast.makeText(context, R.string.team_network_error, Toast.LENGTH_SHORT).show();
    }
  }

  public static boolean checkNetworkAndToast(Context context) {
    if (!NetworkUtils.isConnected()) {
      Toast.makeText(context, R.string.team_network_error, Toast.LENGTH_SHORT).show();
      return false;
    }
    return true;
  }

  // 处理网络错误结果
  public static boolean handleNetworkBrokenResult(Context context, FetchResult<?> resultInfo) {
    if (resultInfo == null) {
      return false;
    }
    if (!resultInfo.isSuccess()) {
      if (!NetworkUtils.isConnected()) {
        Toast.makeText(context, R.string.team_network_error, Toast.LENGTH_SHORT).show();
      } else {
        int code = resultInfo.getError() != null ? resultInfo.getError().getCode() : -1;
        ErrorUtils.showErrorCodeToast(context, code);
      }
      return true;
    }
    return false;
  }
}
