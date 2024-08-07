// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.popmenu;

import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.corekit.model.PluginAction;
import java.util.List;

public interface IChatPopMenu {

  /** custom actions will show first */
  @NonNull
  default List<PluginAction> customizePopMenu(
      List<PluginAction> menuList, ChatMessageBean messageBean) {
    return menuList;
  }

  /** false will show default actions true will show custom actions only */
  default boolean showDefaultPopMenu() {
    return true;
  }
}
