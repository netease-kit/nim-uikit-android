// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.input;

import androidx.viewpager2.widget.ViewPager2;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IItemActionListener;
import java.util.List;

/** more action panel in input view */
public class ActionsPanel {
  private ViewPager2 viewPager2;
  private ActionsPanelAdapter adapter;

  public void init(
      ViewPager2 viewPager2, List<ActionItem> actionItems, IItemActionListener listener) {
    this.viewPager2 = viewPager2;
    this.adapter = new ActionsPanelAdapter(viewPager2.getContext(), actionItems);
    this.adapter.setOnActionItemClick(listener);
    this.viewPager2.setAdapter(adapter);
  }
}
