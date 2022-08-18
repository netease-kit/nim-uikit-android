// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.utils;

import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;

public class ErrorUtils {

  public static int getErrorText(int code, int defaultRes) {

    if (code == QChatConstant.ERROR_CODE_IM_NO_PERMISSION) {
      return R.string.qchat_no_permission;
    }
    return defaultRes;
  }
}
