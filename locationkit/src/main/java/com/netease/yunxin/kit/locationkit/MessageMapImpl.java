// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.locationkit;

import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.map.IMessageMapProvider;

public class MessageMapImpl implements IMessageMapProvider {
  private static final String TAG = "MessageMapImpl";

  @NonNull
  @Override
  public String getChatMpaItemImage(double latitude, double longitude) {
    return ChatMapUtils.generateAMapImageUrl(latitude, longitude);
  }
}
