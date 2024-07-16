// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.action;

import android.view.View;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public class ActionItem {
  private int iconResId;
  private int titleResId;
  private int titleColorResId;
  private String actionType;
  private boolean selected = false;

  public ActionItem(String actionType, @DrawableRes int iconResId) {
    this(actionType, iconResId, 0);
  }

  public ActionItem(String actionType, @DrawableRes int iconResId, @StringRes int titleId) {
    this.actionType = actionType;
    this.iconResId = iconResId;
    this.titleResId = titleId;
  }

  public int getTitleColorResId() {
    return titleColorResId;
  }

  public ActionItem setTitleColorResId(int titleColorResId) {
    this.titleColorResId = titleColorResId;
    return this;
  }

  public int getIconResId() {
    return iconResId;
  }

  public ActionItem setIconResId(int iconResId) {
    this.iconResId = iconResId;
    return this;
  }

  public int getTitleResId() {
    return titleResId;
  }

  public ActionItem setTitleResId(int titleResId) {
    this.titleResId = titleResId;
    return this;
  }

  public String getAction() {
    return actionType;
  }

  public ActionItem setAction(String action) {
    this.actionType = action;
    return this;
  }

  public boolean isSelected() {
    return selected;
  }

  public ActionItem setSelected(boolean selected) {
    this.selected = selected;
    return this;
  }

  public void onClick(View view) {}
}
