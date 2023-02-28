// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.emoji;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.res.ResourcesCompat;

public class CheckedImageButton extends AppCompatImageButton {

  private boolean checked;

  private int normalBkResId;

  private int checkedBkResId;

  private Drawable normalImage;

  private Drawable checkedImage;

  private int leftPadding, topPadding, rightPadding, bottomPadding;

  public CheckedImageButton(Context context) {
    super(context);
  }

  public CheckedImageButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CheckedImageButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void setPaddingValue(int value) {
    setPaddingValue(value, value, value, value);
  }

  public void setPaddingValue(int left, int top, int right, int bottom) {
    leftPadding = left;
    topPadding = top;
    rightPadding = right;
    bottomPadding = bottom;
    setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
  }

  public boolean isChecked() {
    return checked;
  }

  public void setChecked(boolean push) {
    this.checked = push;

    Drawable image = push ? checkedImage : normalImage;
    if (image != null) {
      updateImage(image);
    }

    int background = push ? checkedBkResId : normalBkResId;
    if (background != 0) {
      updateBackground(background);
    }
  }

  public void setNormalBkResId(int normalBkResId) {
    this.normalBkResId = normalBkResId;
    updateBackground(normalBkResId);
  }

  public void setCheckedBkResId(int checkedBkResId) {
    this.checkedBkResId = checkedBkResId;
  }

  public void setNormalImageId(int normalResId) {
    normalImage = ResourcesCompat.getDrawable(getResources(), normalResId, getContext().getTheme());
    updateImage(normalImage);
  }

  public void setCheckedImageId(int pushedResId) {
    checkedImage =
        ResourcesCompat.getDrawable(getResources(), pushedResId, getContext().getTheme());
  }

  public void setNormalImage(Bitmap bitmap) {

    this.normalImage = new BitmapDrawable(getResources(), bitmap);
    updateImage(this.normalImage);
  }

  public void setCheckedImage(Bitmap bitmap) {
    this.checkedImage = new BitmapDrawable(getResources(), bitmap);
  }

  private void updateBackground(int resId) {
    setBackgroundResource(resId);
    setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
  }

  private void updateImage(Drawable drawable) {
    setImageDrawable(drawable);
  }
}
