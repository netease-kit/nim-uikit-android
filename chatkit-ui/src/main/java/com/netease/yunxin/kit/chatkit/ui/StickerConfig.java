// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.StickerProvider;

/** Configuration for the optional sticker picker. */
public class StickerConfig {
  public boolean enabled;
  @Nullable public StickerProvider provider;
}
