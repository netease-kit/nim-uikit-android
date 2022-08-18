// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im;

import android.content.Context;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.corekit.XKitInitOptions;
import com.netease.yunxin.kit.corekit.XKitLogLevel;
import com.netease.yunxin.kit.corekit.XKitLogOptions;
import com.netease.yunxin.kit.corekit.XKitReporterInfoOptions;

@Keep
public class XKitInitOptionsImpl implements XKitInitOptions {

  @Nullable
  @Override
  public XKitLogOptions logOption() {
    return new XKitLogOptions.Builder().level(XKitLogLevel.VERBOSE).build();
  }

  @Nullable
  @Override
  public XKitReporterInfoOptions reporterInfoOption(@NonNull Context context) {
    return new XKitReporterInfoOptions.Builder()
        .appKey(DataUtils.readAppKey(context))
        .imVersion(NIMClient.getSDKVersion())
        .debug(true)
        .build();
  }
}
