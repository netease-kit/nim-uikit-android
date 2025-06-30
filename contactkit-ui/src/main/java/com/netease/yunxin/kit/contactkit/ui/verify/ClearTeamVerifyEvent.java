// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.verify;

import androidx.annotation.NonNull;
import com.netease.yunxin.kit.corekit.event.BaseEvent;

public class ClearTeamVerifyEvent extends BaseEvent {
  public static final String EVENT_TYPE = "ClearTeamVerifyEvent";

  @NonNull
  @Override
  public String getType() {
    return EVENT_TYPE;
  }
}
