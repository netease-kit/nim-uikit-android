// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.media;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageVideoAttachment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSimplePlayerViewBinding;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import com.netease.yunxin.kit.common.utils.TimeUtils;

/** video player for video watching */
public class SimpleVideoPlayer extends ConstraintLayout {
  private static final String TAG = "SimpleVideoPlayer";

  ChatSimplePlayerViewBinding binding;

  private MediaPlayer mediaPlayer;
  private SurfaceHolder surfaceHolder;
  private boolean isSurfaceCreated = false;
  private CountDownTimer actionsCountDown;
  private int currentPosition = -1;

  public enum PlayState {
    playing,
    stop,
    pause
  }

  private PlayState playState = PlayState.stop;
  private String videoFilePath;

  private final Runnable timeRunnable =
      new Runnable() {
        @Override
        public void run() {
          if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int leftTimes = mediaPlayer.getCurrentPosition();
            if (leftTimes < 0) {
              leftTimes = 0;
            }
            int seconds = (int) TimeUtils.getSecondsByMilliseconds(leftTimes);

            binding.videoProgressTime.setText(formatTime(seconds));
            binding.videoProgress.setProgress(seconds);
            postDelayed(this, 1000);
          }
        }
      };

  public SimpleVideoPlayer(@NonNull Context context) {
    this(context, null);
  }

  public SimpleVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SimpleVideoPlayer(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  void init() {
    binding = ChatSimplePlayerViewBinding.inflate(LayoutInflater.from(getContext()), this);
    binding.videoPlay.setOnClickListener(
        v -> {
          if (playState == PlayState.pause) {
            resumeVideo();
          } else {
            playVideo();
          }
        });
    binding.videoProgressAction.setOnClickListener(
        v -> {
          ALog.d(TAG, "progress action click -->> " + playState);
          if (playState == PlayState.pause) {
            resumeVideo();
          } else if (playState == PlayState.playing) {
            pauseVideo();
          } else if (playState == PlayState.stop) {
            playVideo();
          }
        });
    binding
        .getRoot()
        .setOnClickListener(
            v -> {
              ALog.d(TAG, "surface click -->> " + playState);
              if (binding.videoProgressLayout.getVisibility() == VISIBLE) {
                binding.videoProgressLayout.setVisibility(GONE);
                playerProgressAutoHide(false);
              } else {
                binding.videoProgressLayout.setVisibility(VISIBLE);
                playerProgressAutoHide(true);
              }
            });
    surfaceHolder = binding.videoView.getHolder();
    surfaceHolder.addCallback(
        new SurfaceHolder.Callback() {
          @Override
          public void surfaceCreated(@NonNull SurfaceHolder holder) {
            if (!isSurfaceCreated) {
              isSurfaceCreated = true;
              playVideo();
            }
          }

          @Override
          public void surfaceChanged(
              @NonNull SurfaceHolder holder, int format, int width, int height) {}

          @Override
          public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            isSurfaceCreated = false;
          }
        });
  }

  public void handlePlay(V2NIMMessage message) {
    if (message == null || message.getAttachment() == null) {
      return;
    }
    videoFilePath = MessageHelper.getMessageAttachPath(message);
    V2NIMMessageVideoAttachment videoAttachment =
        (V2NIMMessageVideoAttachment) message.getAttachment();
    binding.videoProgress.setIndeterminate(false);
    int duration = (int) TimeUtils.getSecondsByMilliseconds(videoAttachment.getDuration());
    binding.videoProgress.setMax(duration);
    binding.videoTotalTime.setText(formatTime(duration));
    playVideo();
  }

  private void playVideo() {
    if (videoFilePath == null) {
      ALog.e(TAG, "playVideo -->> videoFilePath is null");
      return;
    }
    ALog.d(TAG, "playVideo path:" + videoFilePath);
    binding.videoPlay.setVisibility(GONE);
    binding.videoProgressAction.setImageResource(R.drawable.ic_video_pause);
    playerProgressAutoHide(true);
    if (mediaPlayer != null) {
      if (mediaPlayer.isPlaying()) {
        playState = PlayState.stop;
        mediaPlayer.stop();
      } else {
        if (isSurfaceCreated) {
          mediaPlayer.setDisplay(surfaceHolder);
        } else {
          ToastX.showShortToast(R.string.chat_message_video_fail_try_again);
          return;
        }
      }
      mediaPlayer.reset();
      try {
        mediaPlayer.setDataSource(videoFilePath);
      } catch (Exception e) {
        ToastX.showShortToast(R.string.chat_message_video_fail_try_again);
        e.printStackTrace();
        return;
      }

      setMediaPlayerListener();
      mediaPlayer.prepareAsync();
    }
  }

  private void resumeVideo() {
    binding.videoPlay.setVisibility(GONE);
    binding.videoProgressAction.setImageResource(R.drawable.ic_video_pause);
    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
      mediaPlayer.start();
      playState = PlayState.playing;
      postDelayed(timeRunnable, 100);
    }
  }

  private void pauseVideo() {
    binding.videoPlay.setVisibility(VISIBLE);
    binding.videoProgressAction.setImageResource(R.drawable.ic_video_resume);
    playerProgressAutoHide(true);
    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
      playState = PlayState.pause;
      removeCallbacks(timeRunnable);
    }
  }

  private void setMediaPlayerListener() {
    mediaPlayer.setOnCompletionListener(
        mp -> {
          binding.videoPlay.setVisibility(View.VISIBLE);
          binding.videoProgressAction.setImageResource(R.drawable.ic_video_resume);
          binding.videoProgress.setProgress(0);
          playerProgressAutoHide(false);

          playState = PlayState.stop;
          binding.videoProgressTime.setText(formatTime(0));
          removeCallbacks(timeRunnable);
          currentPosition = -1;
        });

    mediaPlayer.setOnErrorListener(
        (mp, what, extra) -> {
          ALog.d(TAG, "onError -->> " + what);
          try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String type = "video/3gp";
            Uri name = Uri.parse("file://" + videoFilePath);
            intent.setDataAndType(name, type);
            getContext().startActivity(intent);
          } catch (Exception e) {
            ToastX.showShortToast(R.string.chat_message_video_error);
          }
          return true;
        });

    mediaPlayer.setOnPreparedListener(
        mp -> {
          playState = PlayState.playing;
          mediaPlayer.start();
          initVideoSize();
          postDelayed(timeRunnable, 100);
          if (currentPosition >= 0) {
            pauseVideo();
            mediaPlayer.seekTo(currentPosition);
            currentPosition = -1;
          }
        });
  }

  private void playerProgressAutoHide(boolean startCountDown) {
    if (actionsCountDown != null) {
      actionsCountDown.cancel();
    }
    if (startCountDown) {
      initCountDownTimer(3000);
    }
  }

  private void initCountDownTimer(long millisInFuture) {
    actionsCountDown =
        new CountDownTimer(millisInFuture, 1000) {
          @Override
          public void onTick(long millisUntilFinished) {}

          @Override
          public void onFinish() {
            if (playState == PlayState.playing || playState == PlayState.stop) {
              binding.videoProgressLayout.setVisibility(GONE);
            }
          }
        };
    actionsCountDown.start();
  }

  private void initVideoSize() {
    if (mediaPlayer == null) {
      return;
    }
    int width = mediaPlayer.getVideoWidth();
    int height = mediaPlayer.getVideoHeight();

    if (width <= 0 || height <= 0) {
      return;
    }

    int screenWidth = ScreenUtils.getDisplayWidth();
    int screenHeight = ScreenUtils.getDisplayHeight();

    int videoRatio = width / height;
    int screenRatio = screenWidth / screenHeight;

    if (screenRatio > videoRatio) {
      int newWidth = screenHeight * width / height;
      ConstraintLayout.LayoutParams params =
          (ConstraintLayout.LayoutParams) binding.videoView.getLayoutParams();
      params.width = newWidth;
      params.height = screenHeight;
      int margin = (screenWidth - newWidth) / 2;
      params.setMargins(margin, 0, margin, 0);
      binding.videoView.setLayoutParams(params);
    } else {
      int newHeight = screenWidth * height / width;
      ConstraintLayout.LayoutParams params =
          (ConstraintLayout.LayoutParams) binding.videoView.getLayoutParams();
      params.width = screenWidth;
      params.height = newHeight;
      int margin = (screenHeight - newHeight) / 2;
      params.setMargins(0, margin, 0, margin);
      binding.videoView.setLayoutParams(params);
    }
  }

  public void onResume() {
    mediaPlayer = new MediaPlayer();
    if (isSurfaceCreated) {
      playVideo();
    }
  }

  public void onPause() {
    if (mediaPlayer != null) {
      if (mediaPlayer.isPlaying()) {
        playState = PlayState.stop;
        mediaPlayer.stop();
      }
      currentPosition = mediaPlayer.getCurrentPosition();
      if (actionsCountDown != null) {
        actionsCountDown.cancel();
        actionsCountDown = null;
      }
      mediaPlayer.reset();
      mediaPlayer.release();
      mediaPlayer = null;
    }
  }

  public void onDestroy() {
    if (actionsCountDown != null) {
      actionsCountDown.cancel();
      actionsCountDown = null;
    }
  }

  private String formatTime(int time) {
    int minute = time / 60;
    int second = time % 60;
    return getContext().getString(R.string.chat_message_video_time, minute, second);
  }
}
