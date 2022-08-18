// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.annotation.ColorInt;
import com.netease.yunxin.kit.contactkit.ui.R;

/** attrs for {@link ContactListView} all attrs should be object */
public class ContactListViewAttrs {

  public static final int INT_NULL = -1;

  private int nameTextColor = INT_NULL;

  private int nameTextSize = INT_NULL;

  private int indexTextColor = INT_NULL;

  private int indexTextSize = INT_NULL;

  private Boolean showIndexBar;

  private Boolean showSelector;

  private float avatarCornerRadius = INT_NULL;

  private int divideLineColor = INT_NULL;

  public ContactListViewAttrs() {}

  public void parseAttrs(Context context, AttributeSet attrs) {
    if (attrs == null) {
      return;
    }
    TypedArray t;
    t = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ContactListView, 0, 0);
    nameTextColor = t.getColor(R.styleable.ContactListView_nameTextColor, INT_NULL);
    nameTextSize = t.getDimensionPixelSize(R.styleable.ContactListView_nameTextSize, INT_NULL);

    indexTextColor = t.getColor(R.styleable.ContactListView_indexTextColor, INT_NULL);
    indexTextSize = t.getDimensionPixelSize(R.styleable.ContactListView_indexTextSize, INT_NULL);

    showIndexBar = t.getBoolean(R.styleable.ContactListView_showIndexBar, true);

    showSelector = t.getBoolean(R.styleable.ContactListView_showSelector, false);
    avatarCornerRadius =
        t.getDimensionPixelSize(R.styleable.ContactListView_avatarCornerRadius, INT_NULL);
    divideLineColor = t.getColor(R.styleable.ContactListView_divideLineColor, INT_NULL);

    t.recycle();
  }

  public ContactListViewAttrs setAll(ContactListViewAttrs other) {
    if (other.nameTextColor != INT_NULL) {
      this.nameTextColor = other.nameTextColor;
    }

    if (other.nameTextSize != INT_NULL) {
      this.nameTextSize = other.nameTextSize;
    }

    if (other.indexTextColor != INT_NULL) {
      this.indexTextColor = other.indexTextColor;
    }

    if (other.indexTextSize != INT_NULL) {
      this.indexTextSize = other.indexTextSize;
    }

    if (other.avatarCornerRadius != INT_NULL) {
      this.avatarCornerRadius = other.avatarCornerRadius;
    }

    if (other.showIndexBar != null) {
      this.showIndexBar = other.showIndexBar;
    }

    if (other.showSelector != null) {
      this.showSelector = other.showSelector;
    }

    if (other.divideLineColor != INT_NULL) {
      this.divideLineColor = other.divideLineColor;
    }

    return this;
  }

  public void setNameTextColor(@ColorInt int nameTextColor) {
    this.nameTextColor = nameTextColor;
  }

  public int getNameTextColor() {
    return nameTextColor;
  }

  public void setNameTextSize(int textSize) {
    this.nameTextSize = textSize;
  }

  public int getNameTextSize() {
    return nameTextSize;
  }

  public void setIndexTextSize(int textSize) {
    this.indexTextSize = textSize;
  }

  public int getIndexTextSize() {
    return indexTextSize;
  }

  public void setIndexTextColor(@ColorInt int color) {
    this.indexTextColor = color;
  }

  public int getIndexTextColor() {
    return indexTextColor;
  }

  public Boolean getShowIndexBar() {
    return showIndexBar;
  }

  public void setShowIndexBar(Boolean showIndexBar) {
    this.showIndexBar = showIndexBar;
  }

  public Boolean getShowSelector() {
    return showSelector;
  }

  public void setShowSelector(Boolean showSelector) {
    this.showSelector = showSelector;
  }

  public void setAvatarCornerRadius(float radius) {
    this.avatarCornerRadius = radius;
  }

  public float getAvatarCornerRadius() {
    return this.avatarCornerRadius;
  }

  public void setDivideLineColor(@ColorInt int color) {
    this.divideLineColor = color;
  }

  public int getDivideLineColor() {
    return divideLineColor;
  }
}
