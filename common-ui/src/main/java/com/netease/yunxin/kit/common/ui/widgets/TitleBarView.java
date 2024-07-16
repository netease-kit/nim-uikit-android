// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.ui.R;
import com.netease.yunxin.kit.common.ui.databinding.CommonTitleBarLayoutBinding;

public class TitleBarView extends FrameLayout {

  private CommonTitleBarLayoutBinding viewBinding;

  public TitleBarView(@NonNull Context context) {
    super(context);
    initView(null);
  }

  public TitleBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(attrs);
  }

  public TitleBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView(attrs);
  }

  private void initView(AttributeSet attrs) {
    LayoutInflater layoutInflater = LayoutInflater.from(getContext());
    viewBinding = CommonTitleBarLayoutBinding.inflate(layoutInflater, this, true);
    if (attrs != null) {
      TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TitleBarView);
      String title = array.getString(R.styleable.TitleBarView_head_title);
      viewBinding.titleBarTitleTv.setText(title);

      int titleColor =
          array.getInt(R.styleable.TitleBarView_head_title_color, R.color.color_333333);
      viewBinding.titleBarTitleTv.setTextColor(titleColor);

      int visible = array.getInt(R.styleable.TitleBarView_head_img_visible, GONE);
      viewBinding.titleBarHeadImg.setVisibility(visible);

      int src = array.getInt(R.styleable.TitleBarView_head_img_src, R.drawable.ic_yunxin);
      viewBinding.titleBarHeadImg.setImageResource(src);
    }
  }

  public void setTitle(String title) {
    viewBinding.titleBarTitleTv.setText(title);
  }

  public void setTitleColor(int color) {
    viewBinding.titleBarTitleTv.setTextColor(color);
  }

  public void setHeadImageVisible(int visible) {
    viewBinding.titleBarHeadImg.setVisibility(visible);
  }

  public void setLeftImageRes(int res) {
    viewBinding.titleBarHeadImg.setImageResource(res);
  }

  public ImageView getLeftImageView() {
    return viewBinding.titleBarHeadImg;
  }

  public TextView getTitleTextView() {
    return viewBinding.titleBarTitleTv;
  }

  public ImageView getRightImageView() {
    return viewBinding.titleBarMoreImg;
  }

  public ImageView getRight2ImageView() {
    return viewBinding.titleBarSearchImg;
  }

  public TextView getCenterTitleTextView() {
    return viewBinding.titleBarTitleCenterTv;
  }

  public void setRight2ImageClick(OnClickListener listener) {
    viewBinding.titleBarSearchImg.setOnClickListener(listener);
  }

  public void setLeftImageClick(OnClickListener listener) {
    viewBinding.titleBarHeadImg.setOnClickListener(listener);
  }

  public void setRight2ImageRes(int res) {
    viewBinding.titleBarSearchImg.setImageResource(res);
  }

  public void showRight2ImageView(boolean show) {
    viewBinding.titleBarSearchImg.setVisibility(show ? VISIBLE : GONE);
  }

  public void setRightImageClick(OnClickListener listener) {
    viewBinding.titleBarMoreImg.setOnClickListener(listener);
  }

  public void setRightImageRes(int res) {
    viewBinding.titleBarMoreImg.setImageResource(res);
  }

  public void showRightImageView(boolean show) {
    viewBinding.titleBarMoreImg.setVisibility(show ? VISIBLE : GONE);
  }

  public void setTitleBgRes(int res) {
    viewBinding.getRoot().setBackgroundResource(res);
  }
}
