// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.interfaces;

import android.view.View;
import com.netease.yunxin.kit.common.ui.action.ActionItem;

public interface IItemActionListener {
  void onClick(View view, int position, ActionItem item);
}
