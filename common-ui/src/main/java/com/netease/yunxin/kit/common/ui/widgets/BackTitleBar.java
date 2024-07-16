// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;
import com.netease.yunxin.kit.common.ui.R;
import com.netease.yunxin.kit.common.ui.databinding.BackTitleBarLayoutBinding;

public class BackTitleBar extends FrameLayout {

  BackTitleBarLayoutBinding binding;

  public BackTitleBar(@NonNull Context context) {
    super(context);
    initView(context, null);
  }

  public BackTitleBar(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context, attrs);
  }

  public BackTitleBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView(context, attrs);
  }

  private void initView(Context context, AttributeSet attrs) {
    LayoutInflater layoutInflater = LayoutInflater.from(getContext());
    binding = BackTitleBarLayoutBinding.inflate(layoutInflater, this, true);
    if (attrs != null) {
      TypedArray t =
          context.getTheme().obtainStyledAttributes(attrs, R.styleable.BackTitleBar, 0, 0);
      String title = t.getString(R.styleable.BackTitleBar_titleText);
      String leftTitle = t.getString(R.styleable.BackTitleBar_leftTitleText);
      String rightTitle = t.getString(R.styleable.BackTitleBar_rightTitleText);
      int titleColor =
          t.getColor(
              R.styleable.BackTitleBar_titleTextColor,
              context.getResources().getColor(R.color.color_333333));
      binding.tvTitle.setText(title);
      binding.tvTitle.setTextColor(titleColor);
      if (!TextUtils.isEmpty(rightTitle)) {
        binding.tvAction.setVisibility(VISIBLE);
        binding.tvAction.setText(rightTitle);
      }

      if (!TextUtils.isEmpty(leftTitle)) {
        binding.ivBack.setVisibility(GONE);
        binding.tvLeft.setVisibility(VISIBLE);
        binding.tvLeft.setText(leftTitle);
      }
    }
  }

  public void setBackIconVisible(int visible) {
    binding.ivBack.setVisibility(visible);
  }

  public void setLeftTextViewVisible(int visible) {
    binding.tvLeft.setVisibility(visible);
  }

  public void setRightTextViewVisible(int visible) {
    binding.tvAction.setVisibility(visible);
  }

  public void setRightImageViewVisible(int visible) {
    binding.ivAction.setVisibility(visible);
  }

  public ImageView getBackImageView() {
    return binding.ivBack;
  }

  public TextView getLeftTextView() {
    return binding.tvLeft;
  }

  public TextView getTitleTextView() {
    return binding.tvTitle;
  }

  public TextView getRightTextView() {
    return binding.tvAction;
  }

  public TextView getActionTextView() {
    return binding.tvAction;
  }

  public ImageView getRightImageView() {
    return binding.ivAction;
  }

  public ImageView getActionImageView() {
    return binding.ivAction;
  }

  public BackTitleBar setOnBackIconClickListener(OnClickListener listener) {
    binding.ivBack.setOnClickListener(listener);
    binding.tvLeft.setOnClickListener(listener);
    return this;
  }

  public BackTitleBar setLeftText(@StringRes int text) {
    binding.ivBack.setVisibility(GONE);
    binding.tvLeft.setVisibility(VISIBLE);
    binding.tvLeft.setText(text);
    return this;
  }

  public BackTitleBar setTitle(@StringRes int title) {
    binding.tvTitle.setText(title);
    return this;
  }

  public BackTitleBar setTitle(String title) {
    binding.tvTitle.setText(title);
    return this;
  }

  public BackTitleBar setActionText(@StringRes int actionText) {
    binding.tvAction.setText(actionText);
    binding.tvAction.setVisibility(VISIBLE);
    return this;
  }

  public BackTitleBar setActionEnable(boolean enable) {
    binding.tvAction.setEnabled(enable);
    binding.tvAction.setAlpha(enable ? 1 : 0.5f);
    return this;
  }

  public BackTitleBar setActionText(String actionText) {
    binding.tvAction.setText(actionText);
    binding.tvAction.setVisibility(VISIBLE);
    return this;
  }

  /**
   * 设置右侧按钮背景
   *
   * @param res 资源id
   */
  public BackTitleBar setActionBackgroundRes(@DrawableRes int res) {
    binding.tvAction.setBackground(
        ResourcesCompat.getDrawable(getContext().getResources(), res, null));
    binding.tvAction.setVisibility(VISIBLE);
    return this;
  }

  public BackTitleBar setActionTextColor(@ColorInt int textColor) {
    binding.tvAction.setTextColor(textColor);
    binding.tvAction.setVisibility(VISIBLE);
    return this;
  }

  public BackTitleBar setActionImg(@DrawableRes int actionImg) {
    binding.ivAction.setVisibility(VISIBLE);
    binding.ivAction.setImageDrawable(
        ResourcesCompat.getDrawable(getContext().getResources(), actionImg, null));
    return this;
  }

  public BackTitleBar setLeftActionListener(OnClickListener listener) {
    if (binding.tvLeft.getVisibility() == VISIBLE) {
      binding.tvLeft.setOnClickListener(listener);
    }
    return this;
  }

  public BackTitleBar setActionListener(OnClickListener listener) {
    if (binding.tvAction.getVisibility() == VISIBLE) {
      binding.tvAction.setOnClickListener(listener);
    } else if (binding.ivAction.getVisibility() == VISIBLE) {
      binding.ivAction.setOnClickListener(listener);
    }
    return this;
  }

  public void setActionTextListener(OnClickListener listener) {
    binding.tvAction.setOnClickListener(listener);
  }
}
