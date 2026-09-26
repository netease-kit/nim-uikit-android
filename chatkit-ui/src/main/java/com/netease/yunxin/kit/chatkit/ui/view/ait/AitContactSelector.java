// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ait;

import com.netease.yunxin.kit.chatkit.ui.model.ait.AitUserInfo;
import java.util.List;

/** UI-independent contract for selecting an @ contact. */
public interface AitContactSelector {
  void show();

  boolean isShowing();

  void setData(List<AitUserInfo> data, boolean refresh, boolean showAll);

  /** Controls whether AI users can be searched in this selector. */
  default void setAllowAISearch(boolean allow) {}

  default void addData(List<AitUserInfo> data) {}

  void setOnItemListener(ItemListener listener);

  interface ItemListener {
    void onSelect(AitUserInfo item);

    void onLoadMore();
  }
}
