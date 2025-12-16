// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;

/**
 * 通用的回调
 *
 * @param <T>
 */
public class V2ChatCallback<T> implements FetchCallback<T> {

  boolean showSuccess;

  public V2ChatCallback<T> setShowSuccess(boolean showSuccess) {
    this.showSuccess = showSuccess;
    return this;
  }

  public V2ChatCallback() {}

  @Override
  public void onSuccess(@Nullable T param) {
    if (showSuccess) {
      ToastX.showShortToast(R.string.chat_server_request_success);
    }
  }

  @Override
  public void onError(int errorCode, @Nullable String errorMsg) {
    ToastX.showShortToast(R.string.chat_server_request_fail);
  }
}
