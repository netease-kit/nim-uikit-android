// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.ui.widgets.TitleBarView;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactViewLayoutBinding;

public class ContactLayout extends LinearLayout {

  private ContactViewLayoutBinding viewBinding;

  public ContactLayout(@NonNull Context context) {
    super(context);
    init(null);
  }

  public ContactLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public ContactLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  public ContactLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    LayoutInflater layoutInflater = LayoutInflater.from(getContext());
    viewBinding = ContactViewLayoutBinding.inflate(layoutInflater, this);
  }

  public TitleBarView getTitleBar() {
    return viewBinding.contactTitleLayout;
  }

  public LinearLayout getTopLayout() {
    return viewBinding.contactHeaderLayout;
  }

  public FrameLayout getBodyLayout() {
    return viewBinding.contactBodyLayout;
  }

  public ContactListView getContactListView() {
    return viewBinding.contactListview;
  }

  public FrameLayout getBottomLayout() {
    return viewBinding.contactBottomLayout;
  }

  public FrameLayout getBodyTopLayout() {
    return viewBinding.contactBodyTopLayout;
  }
}
