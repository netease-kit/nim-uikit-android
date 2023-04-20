// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitService;
import com.netease.yunxin.kit.corekit.im.IIMKitInitService;

public class ChatUIInitService implements IIMKitInitService {
  @Override
  public void onInit(@NonNull Context context) {
    AitService.getInstance().init(context);
  }
}
