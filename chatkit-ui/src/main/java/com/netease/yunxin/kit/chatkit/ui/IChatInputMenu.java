// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import android.view.View;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import java.util.List;

public interface IChatInputMenu {

  public default List<ActionItem> customizeInputBar(List<ActionItem> actionItemList) {
    return actionItemList;
  }

  public default List<ActionItem> customizeInputMore(List<ActionItem> actionItemList) {
    return actionItemList;
  }

  public default boolean onCustomInputClick(Context context, View view, String action) {
    return false;
  }

  public default boolean onInputClick(Context context, View view, String action) {
    return false;
  }

  public default boolean onAIHelperClick(
      Context context, View view, String action, List<IMMessageInfo> messageInfoList) {
    return false;
  }
}
