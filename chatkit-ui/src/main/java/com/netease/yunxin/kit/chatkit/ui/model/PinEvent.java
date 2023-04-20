// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model;

import androidx.annotation.NonNull;
import com.netease.yunxin.kit.corekit.event.BaseEvent;

public class PinEvent extends BaseEvent {

  public String msgUuid;
  public boolean isRemove;

  public PinEvent(String uuid, boolean remove) {
    msgUuid = uuid;
    isRemove = remove;
  }

  @NonNull
  @Override
  public String getType() {
    return "PinEvent";
  }
}
