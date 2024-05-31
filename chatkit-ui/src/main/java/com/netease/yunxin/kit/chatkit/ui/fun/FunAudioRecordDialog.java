// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatRecordDialogBinding;
import com.netease.yunxin.kit.common.utils.SizeUtils;

public class FunAudioRecordDialog extends Dialog {

  private final String TAG = "FunAudioRecordDialog";
  private final FunChatRecordDialogBinding viewBinding;
  private final Rect opViewRect = new Rect();
  private final int remainingSeconds = 10;
  private final int STATUS_RECORD = 0;
  private final int STATUS_MOVE_CANCEL = 1;

  private int timeDuration = 60;
  private String remindText = "%d";

  private int status = STATUS_RECORD;
  private CountDownTimer countDownTimer;

  public FunAudioRecordDialog(@NonNull Context context) {
    super(context);
    viewBinding = FunChatRecordDialogBinding.inflate(LayoutInflater.from(getContext()));
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final Window window = getWindow();
    if (window != null) {
      window.requestFeature(Window.FEATURE_NO_TITLE);
      setContentView(viewBinding.getRoot());
      window.setBackgroundDrawable(new ColorDrawable(0x000000));
      window.setLayout(
          WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }
    remindText = getContext().getString(R.string.fun_chat_audio_record_max_time_tips);
    setCanceledOnTouchOutside(false);
    viewBinding.cancelView.post(
        () -> {
          int[] location = new int[2];
          viewBinding.cancelView.getLocationOnScreen(location);
          int viewLeft = location[0];
          int viewTop = location[1];
          int viewRight = viewLeft + viewBinding.cancelView.getWidth();
          int viewBottom = viewTop + viewBinding.cancelView.getHeight();
          opViewRect.left = viewLeft;
          opViewRect.top = viewTop;
          opViewRect.right = viewRight;
          opViewRect.bottom = viewBottom;
        });
  }

  @Override
  public void show() {
    ALog.d(LIB_TAG, TAG, "show");
    super.show();
    resetView();
  }

  public void show(int duration) {
    ALog.d(LIB_TAG, TAG, "show:" + timeDuration);
    timeDuration = duration;
    playRecordAnimator();
    this.show();
  }

  public void resetView() {
    ALog.d(LIB_TAG, TAG, "resetView");
    status = STATUS_RECORD;
    switchView();
  }

  public void playRecordAnimator() {
    ALog.d(LIB_TAG, TAG, "playRecordAnimator:" + viewBinding.recordBg.getWidth());

    if (countDownTimer == null) {
      viewBinding.recordLottieView.setIgnoreDisabledSystemAnimations(true);
      int endLength = SizeUtils.dp2px(315);
      int startLength = SizeUtils.dp2px(165);
      int subLength = endLength - startLength;
      ViewGroup.LayoutParams params = viewBinding.recordBg.getLayoutParams();
      countDownTimer =
          new CountDownTimer(timeDuration * 1000L, 200) {
            @Override
            public void onTick(long millisUntilFinished) {
              int showTime = (int) (float) millisUntilFinished / 1000;
              if (showTime < remainingSeconds) {
                showCountDownView(showTime + 1);
              } else {
                params.width =
                    (int)
                        (startLength
                            + subLength
                                * (timeDuration - (millisUntilFinished / 1000.f))
                                / timeDuration);
                viewBinding.recordBg.setLayoutParams(params);
              }
            }

            @Override
            public void onFinish() {
              dismiss();
            }
          };
      countDownTimer.start();
    }
  }

  @Override
  public void dismiss() {
    ALog.d(LIB_TAG, TAG, "dismiss");
    if (countDownTimer != null) {
      countDownTimer.cancel();
      countDownTimer = null;
    }
    timeDuration = -1;
    status = STATUS_RECORD;
    super.dismiss();
  }

  public void showCancelView() {
    ALog.d(LIB_TAG, TAG, "showCancelView");
    if (status == STATUS_MOVE_CANCEL) {
      return;
    }
    status = STATUS_MOVE_CANCEL;
    switchView();
  }

  public void showRecordingView() {
    ALog.d(LIB_TAG, TAG, "showRecordingView");
    if (status == STATUS_RECORD) {
      return;
    }
    status = STATUS_RECORD;
    switchView();
  }

  private void switchView() {
    ALog.d(LIB_TAG, TAG, "switchView:" + status);
    if (status == STATUS_RECORD) {
      viewBinding.recordBg.setBackgroundResource(R.drawable.fun_bg_chat_audio_recording);
      viewBinding.cancelView.setImageResource(R.drawable.fun_ic_chat_input_audio_record_cancel_btn);
      viewBinding.cancelTipsView.setVisibility(View.GONE);
      viewBinding.sendTipsView.setVisibility(View.VISIBLE);
      viewBinding.bottomLayout.setBackgroundResource(
          R.drawable.fun_bg_chat_input_audio_bottom_recording);
      if (timeDuration > -1 && timeDuration < remainingSeconds) {
        viewBinding.recordingMaxTimeTv.setVisibility(View.VISIBLE);
        viewBinding.recordLottieView.setVisibility(View.GONE);
      } else {
        viewBinding.recordingMaxTimeTv.setVisibility(View.GONE);
        viewBinding.recordLottieView.setVisibility(View.VISIBLE);
      }

    } else if (status == STATUS_MOVE_CANCEL) {
      viewBinding.recordBg.setBackgroundResource(R.drawable.fun_bg_chat_audio_recording_cancel);
      viewBinding.cancelView.setImageResource(
          R.drawable.fun_ic_chat_input_audio_record_cancel_selected);
      viewBinding.cancelTipsView.setVisibility(View.VISIBLE);
      viewBinding.sendTipsView.setVisibility(View.GONE);
      viewBinding.bottomLayout.setBackgroundResource(
          R.drawable.fun_bg_chat_input_audio_record_bottom_cancel);
    }
  }

  public Rect getOpViewRect() {
    return opViewRect;
  }

  public void showCountDownView(int timer) {
    ALog.d(LIB_TAG, TAG, "showCountDownView,timer:" + timer + ",status:" + status);
    if (timer < 0) {
      timer = 0;
    }
    viewBinding.recordingMaxTimeTv.setText(String.format(remindText, timer));
    viewBinding.recordingMaxTimeTv.setVisibility(View.VISIBLE);
    viewBinding.recordLottieView.setVisibility(View.GONE);
  }
}
