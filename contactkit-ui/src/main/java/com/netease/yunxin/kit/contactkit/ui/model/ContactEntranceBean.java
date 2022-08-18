// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import java.util.Objects;

/** Contact data for default ViewHolder */
public class ContactEntranceBean extends BaseContactBean {
  public @DrawableRes int icon;

  public int number;

  public boolean showRightArrow = true;

  public String title;

  public ContactEntranceBean(@DrawableRes int icon, @NonNull String title) {
    this.icon = icon;
    this.title = title;
    weight = ContactBeanWeight.ENTRANCE_WEIGHT;
    viewType = IViewTypeConstant.CONTACT_ACTION_ENTER;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ContactEntranceBean that = (ContactEntranceBean) o;
    return icon == that.icon && Objects.equals(title, that.title);
  }

  @Override
  public int hashCode() {
    return Objects.hash(icon, title);
  }

  @Override
  public boolean isShowDivision() {
    return false;
  }

  @Override
  public String getTarget() {
    return null;
  }
}
