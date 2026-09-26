// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ait;

import android.content.Context;

/** Creates the contact selector owned by the active chat skin. */
public interface AitContactSelectorFactory {
  AitContactSelector create(Context context);
}
