// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui;

import androidx.annotation.Nullable;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;

public class FetchCallbackImpl<T> implements FetchCallback<T> {

  String data;

  public FetchCallbackImpl(String data) {
    this.data = data;
  }

  @Override
  public void onSuccess(@Nullable T param) {}

  @Override
  public void onFailed(int code) {}

  @Override
  public void onException(@Nullable Throwable exception) {}
}
