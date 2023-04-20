// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model;

import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.msg.model.IMMessage;

public class AnchorScrollInfo {
  IMMessage anchorMessage;

  public AnchorScrollInfo(@NonNull IMMessage anchorMessage) {
    this.anchorMessage = anchorMessage;
  }

  public String messageUuid() {
    return anchorMessage.getUuid();
  }
}
