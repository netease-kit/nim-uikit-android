// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import androidx.annotation.NonNull;
import java.io.File;

public interface StickerFileCallback {
  void onSuccess(@NonNull File file, int width, int height);

  void onError(@NonNull Throwable error);
}
