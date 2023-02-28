// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.input;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.media.record.AudioRecorder;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatMessageRecordViewBinding;

/** record panel in input view */
public class RecordView extends FrameLayout {
  public static final String TAG = "RecordView";
  private QChatMessageRecordViewBinding mBinding;
  private AudioRecorder mAudioRecorder;
  private IAudioRecordCallback recordCallback;
  private IPermissionRequest permissionRequest;

  private boolean started = false;

  public RecordView(@NonNull Context context) {
    this(context, null);
  }

  public RecordView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RecordView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @SuppressLint("ClickableViewAccessibility")
  private void init() {
    mBinding = QChatMessageRecordViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
    mBinding.recordButton.setOnTouchListener(
        (v, event) -> {
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (permissionRequest != null
                && !permissionRequest.requestPermission(Manifest.permission.RECORD_AUDIO)) {
              return false;
            }
            mBinding.recordButtonIcon.setBackgroundResource(R.drawable.ic_record_pressed);
            initAudioRecord();
            startAudioRecord();
          } else if (event.getAction() == MotionEvent.ACTION_CANCEL
              || event.getAction() == MotionEvent.ACTION_UP) {
            mBinding.recordButtonIcon.setBackgroundResource(R.drawable.ic_record_normal);
            endAudioRecord(isCancelled(v, event));
          } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mBinding.recordButtonIcon.setBackgroundResource(R.drawable.ic_record_pressed);
            willCancelAudioRecord(isCancelled(v, event));
          }
          return true;
        });
  }

  public void setPermissionRequest(IPermissionRequest request) {
    this.permissionRequest = request;
  }

  public void setRecordCallback(IAudioRecordCallback callback) {
    recordCallback = callback;
  }

  private void initAudioRecord() {
    if (mAudioRecorder == null) {
      mAudioRecorder = new AudioRecorder(getContext(), RecordType.AAC, 60, recordCallback);
    }
  }

  private void startAudioRecord() {
    if (getContext() instanceof Activity) {
      ((Activity) getContext())
          .getWindow()
          .setFlags(
              WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
              WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    mAudioRecorder.startRecord();
  }

  private void endAudioRecord(boolean cancel) {
    ALog.d(TAG, "endAudioRecord -->> cancel:" + cancel);
    if (getContext() instanceof Activity) {
      ((Activity) getContext())
          .getWindow()
          .setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    mAudioRecorder.completeRecord(cancel);
  }

  private void willCancelAudioRecord(boolean cancel) {
    ALog.d(TAG, "willCancelAudioRecord -->> cancel:" + cancel);
    // do nothing
  }

  private boolean isCancelled(View view, MotionEvent event) {
    int[] location = new int[2];
    view.getLocationOnScreen(location);

    int radius = SizeUtils.dp2px(103) / 2;
    return !isInCircle(
        event.getRawX(), event.getRawY(), location[0] + radius, location[1] + radius, radius);
  }

  private boolean isInCircle(float x, float y, float cX, float cY, float radius) {
    return radius * radius > (x - cX) * (x - cX) + (y - cY) * (y - cY);
  }

  protected void startRecord() {
    started = true;
    mBinding.recordPressedToSpeak.setVisibility(INVISIBLE);
    mBinding.recordButtonWave.setVisibility(VISIBLE);

    Animation waveAnimator =
        AnimationUtils.loadAnimation(getContext(), R.anim.anim_record_button_wave);
    waveAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    waveAnimator.setDuration(1000);
    waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
    mBinding.recordButtonWave.startAnimation(waveAnimator);
  }

  protected void endRecord() {
    started = false;
    mBinding.recordPressedToSpeak.setVisibility(VISIBLE);
    mBinding.recordButtonWave.setVisibility(GONE);
    mBinding.recordButtonWave.clearAnimation();
  }

  protected void recordReachMaxTime(int maxTime) {
    mAudioRecorder.handleEndRecord(true, maxTime);
  }
}
