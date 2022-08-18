// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatThreeStatusSwitchLayoutBinding;

public class ThreeStatusSwitch extends LinearLayout implements View.OnClickListener {

  public static final int SWITCH_OPEN = 0;
  public static final int SWITCH_CLOSE = 1;
  public static final int SWITCH_NEUTRAL = 2;
  private final float normalAlpha = 1f;
  private boolean statusEnable = true;
  private int currentStatus = SWITCH_NEUTRAL;
  private int originStatus = SWITCH_NEUTRAL;

  private QChatThreeStatusSwitchLayoutBinding viewBinding;
  private OnClickListener mClickListener;

  public ThreeStatusSwitch(@NonNull Context context) {
    this(context, null);
  }

  public ThreeStatusSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ThreeStatusSwitch(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView();
  }

  private void initView() {
    setOrientation(HORIZONTAL);
    setBackgroundResource(R.drawable.bg_shape_qchat_switch);
    viewBinding =
        QChatThreeStatusSwitchLayoutBinding.inflate(LayoutInflater.from(getContext()), this);
    viewBinding.qchatThreeStatusCloseLayout.setOnClickListener(this);
    viewBinding.qchatThreeStatusNeutralLayout.setOnClickListener(this);
    viewBinding.qchatThreeStatusOpenLayout.setOnClickListener(this);
  }

  private void resetSwitch() {
    viewBinding.qchatThreeStatusCloseLayout.setBackgroundResource(0);
    viewBinding.qchatThreeStatusCloseIv.setImageResource(R.drawable.ic_red_close);

    viewBinding.qchatThreeStatusNeutralLayout.setBackgroundResource(0);
    viewBinding.qchatThreeStatusNeutralIv.setImageResource(R.drawable.ic_gray_neutral);

    viewBinding.qchatThreeStatusOpenLayout.setBackgroundResource(0);
    viewBinding.qchatThreeStatusOpenIv.setImageResource(R.drawable.ic_green_open);

    setEnable(statusEnable);
  }

  public void setEnable(boolean enable) {
    float unableAlpha = 0.5f;
    setAlpha(enable ? normalAlpha : unableAlpha);
    statusEnable = enable;
  }

  public boolean isEnable() {
    return statusEnable;
  }

  public int getCurrentStatus() {
    return currentStatus;
  }

  public boolean statusHasChange() {
    return originStatus != currentStatus;
  }

  public void setStatus(int value) {
    originStatus = value;
    switchStatus(value);
  }

  private void switchStatus(int value) {
    resetSwitch();
    switch (value) {
      case SWITCH_CLOSE:
        viewBinding.qchatThreeStatusCloseLayout.setBackgroundResource(
            R.drawable.bg_shape_qchat_switch_left);
        viewBinding.qchatThreeStatusCloseIv.setImageResource(R.drawable.ic_white_close);
        currentStatus = SWITCH_CLOSE;
        break;
      case SWITCH_NEUTRAL:
        viewBinding.qchatThreeStatusNeutralLayout.setBackgroundResource(
            R.drawable.bg_shape_qchat_switch_center);
        viewBinding.qchatThreeStatusNeutralIv.setImageResource(R.drawable.ic_white_neutral);
        currentStatus = SWITCH_NEUTRAL;
        break;
      case SWITCH_OPEN:
        viewBinding.qchatThreeStatusOpenLayout.setBackgroundResource(
            R.drawable.bg_shape_qchat_switch_right);
        viewBinding.qchatThreeStatusOpenIv.setImageResource(R.drawable.ic_white_open);
        currentStatus = SWITCH_OPEN;
        break;
    }
  }

  public void setOnClickListener(OnClickListener listener) {
    mClickListener = listener;
  }

  @Override
  public void onClick(View v) {

    if (v.getId() == viewBinding.qchatThreeStatusOpenLayout.getId()) {
      switchStatus(SWITCH_OPEN);
    } else if (v.getId() == viewBinding.qchatThreeStatusCloseLayout.getId()) {
      switchStatus(SWITCH_CLOSE);
    } else if (v.getId() == viewBinding.qchatThreeStatusNeutralLayout.getId()) {
      switchStatus(SWITCH_NEUTRAL);
    }
    if (mClickListener != null) {
      mClickListener.onClick(this);
    }
  }
}
