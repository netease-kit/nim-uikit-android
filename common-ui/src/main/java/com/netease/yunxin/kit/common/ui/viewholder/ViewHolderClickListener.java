// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.viewholder;

import android.view.View;

public interface ViewHolderClickListener {
  default boolean onClick(View view, BaseBean data, int position) {
    return false;
  }

  default boolean onAvatarClick(View view, BaseBean data, int position) {
    return false;
  }

  default boolean onLongClick(View view, BaseBean data, int position) {
    return false;
  }

  default boolean onAvatarLongClick(View view, BaseBean data, int position) {
    return false;
  }
}
