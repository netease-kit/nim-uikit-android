// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.utils;

import android.content.Context;
import android.widget.Toast;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.qchatkit.ui.R;

public class QChatUtils {

  public static void isConnectedToastAndRun(Context context, Runnable runnable) {
    isConnectedToastAndRun(context, context.getString(R.string.qchat_network_error), runnable);
  }

  public static void isConnectedToastAndRun(Context context, String toast, Runnable runnable) {
    isConnectedToastAndRun(
        runnable, () -> Toast.makeText(context, toast, Toast.LENGTH_SHORT).show());
  }

  public static void isConnectedToastAndRun(Runnable SuccessRunnable, Runnable failRunnable) {
    if (NetworkUtils.isConnected()) {
      if (SuccessRunnable != null) {
        SuccessRunnable.run();
      }
    } else {
      if (failRunnable != null) {
        failRunnable.run();
      }
    }
  }
}
