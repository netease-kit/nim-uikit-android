// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui;

import android.content.Context;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.qchatkit.QChatService;

@Keep
public class QChatUIService extends QChatService {

  @NonNull
  @Override
  public String getServiceName() {
    return "QChatUIService";
  }

  @NonNull
  @Override
  public QChatService create(@NonNull Context context) {
    return this;
  }
}
