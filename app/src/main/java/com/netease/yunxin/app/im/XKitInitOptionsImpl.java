/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import com.netease.yunxin.kit.corekit.XKitInitOptions;
import com.netease.yunxin.kit.corekit.XKitLogLevel;
import com.netease.yunxin.kit.corekit.XKitLogOptions;

@Keep
public class XKitInitOptionsImpl implements XKitInitOptions {

    @Nullable
    @Override
    public XKitLogOptions logOption() {
        return new XKitLogOptions.Builder().level(XKitLogLevel.VERBOSE).build();
    }
}
