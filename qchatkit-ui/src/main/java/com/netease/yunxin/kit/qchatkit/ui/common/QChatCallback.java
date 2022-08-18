// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.common;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.ui.R;

public class QChatCallback<T> implements FetchCallback<T> {

  public static final int NO_PERMISSION = 403;

  private final Context context;

  private View process;

  public QChatCallback(Context context) {
    this.context = context;
  }

  @Override
  public void onSuccess(@Nullable T param) {
    if (process != null) {
      process.setVisibility(View.GONE);
    }
  }

  @Override
  public void onFailed(int code) {
    showToast(code, context);
    if (process != null) {
      process.setVisibility(View.GONE);
    }
  }

  public static void showToast(int code, Context context) {
    if (code == NO_PERMISSION) {
      Toast.makeText(context, context.getString(R.string.qchat_no_permission), Toast.LENGTH_SHORT)
          .show();
    } else {
      Toast.makeText(
              context,
              context.getString(R.string.qchat_server_request_fail) + code,
              Toast.LENGTH_SHORT)
          .show();
    }
  }

  @Override
  public void onException(@Nullable Throwable exception) {
    Toast.makeText(
            context,
            context.getString(R.string.qchat_server_request_fail) + exception,
            Toast.LENGTH_SHORT)
        .show();
    if (process != null) {
      process.setVisibility(View.GONE);
    }
  }

  public QChatCallback<T> setProcess(View process) {
    this.process = process;
    return this;
  }
}
