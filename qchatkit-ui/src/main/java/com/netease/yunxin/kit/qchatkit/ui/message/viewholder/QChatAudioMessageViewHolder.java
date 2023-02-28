// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.viewholder;

import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static android.widget.RelativeLayout.END_OF;
import static android.widget.RelativeLayout.START_OF;

import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.audioplayer.Playable;
import com.netease.yunxin.kit.corekit.im.repo.SettingRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatMessageAudioViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QchatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.message.audio.QChatMessageAudioControl;

/** view holder for audio message */
public class QChatAudioMessageViewHolder extends QChatBaseMessageViewHolder {

  private QChatMessageAudioViewHolderBinding audioBinding;

  private QChatMessageInfo currentMessage;

  private QChatMessageAudioControl audioControl;

  public static final int CLICK_TO_PLAY_AUDIO_DELAY = 500;

  public static final int MAX_LENGTH_FOR_AUDIO = 230;

  public static final int MIN_LENGTH_FOR_AUDIO = 80;

  private final QChatMessageAudioControl.AudioControlListener onPlayListener =
      new QChatMessageAudioControl.AudioControlListener() {

        @Override
        public void updatePlayingProgress(Playable playable, long curPosition) {
          //do nothing
        }

        @Override
        public void onAudioControllerReady(Playable playable) {
          if (isTheSame(currentMessage.getMessage().getUuid())) {
            return;
          }
          play();
        }

        @Override
        public void onEndPlay(Playable playable) {
          if (isTheSame(currentMessage.getMessage().getUuid())) {
            return;
          }

          updateTime(playable.getDuration());
          stop();
        }
      };

  private void play() {
    if (audioBinding.animation.getDrawable() instanceof AnimationDrawable) {
      AnimationDrawable animation = (AnimationDrawable) audioBinding.animation.getDrawable();
      animation.start();
    }
  }

  private void stop() {
    if (audioBinding.animation.getDrawable() instanceof AnimationDrawable) {
      AnimationDrawable animation = (AnimationDrawable) audioBinding.animation.getDrawable();
      animation.stop();

      endPlayAnim();
    }
  }

  private void initPlayAnim() {
    if (isReceivedMessage(currentMessage)) {
      audioBinding.animation.setImageResource(R.drawable.ani_qchat_message_audio_from);
    } else {
      audioBinding.animation.setImageResource(R.drawable.ani_qchat_message_audio_to);
    }
  }

  private void endPlayAnim() {
    if (isReceivedMessage(currentMessage)) {
      audioBinding.animation.setImageResource(R.drawable.ic_qchat_message_from_audio);
    } else {
      audioBinding.animation.setImageResource(R.drawable.ic_message_to_audio);
    }
  }

  private void updateTime(long milliseconds) {
    long seconds = milliseconds / 1000;
    if (seconds <= 0) {
      seconds = 1;
    }
    audioBinding.tvTime.setText(String.format("%ss", seconds));
  }

  private boolean isTheSame(String uuid) {
    String current = audioBinding.tvTime.getTag().toString();
    return TextUtils.isEmpty(uuid) || !uuid.equals(current);
  }

  public QChatAudioMessageViewHolder(
      @NonNull QchatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent);
  }

  @Override
  public void addContainer() {
    audioBinding =
        QChatMessageAudioViewHolderBinding.inflate(
            LayoutInflater.from(getParent().getContext()), getContainer(), true);
  }

  @Override
  public void bindData(QChatMessageInfo message, QChatMessageInfo lastMessage) {
    super.bindData(message, lastMessage);
    audioControl = QChatMessageAudioControl.getInstance();
    audioBinding.tvTime.setTag(message.getMessage().getUuid());
    currentMessage = message;
    playControl(message);
    setAudioLayout(message);
    audioBinding.container.setOnClickListener(
        v -> {
          initPlayAnim();
          audioControl.setEarPhoneModeEnable(SettingRepo.INSTANCE.getHandsetMode());
          audioControl.startPlayAudioDelay(CLICK_TO_PLAY_AUDIO_DELAY, message, onPlayListener);
        });
  }

  private void setAudioLayout(QChatMessageInfo message) {
    AudioAttachment audioAttachment = (AudioAttachment) message.getMessage().getAttachment();
    if (audioAttachment == null) {
      return;
    }
    long len = audioAttachment.getDuration() / 1000;
    LinearLayout.LayoutParams layoutParams =
        (LinearLayout.LayoutParams) getContainer().getLayoutParams();
    if (len <= 2) {
      layoutParams.width = SizeUtils.dp2px(MIN_LENGTH_FOR_AUDIO);
    } else {
      layoutParams.width =
          Math.min(
              SizeUtils.dp2px(MAX_LENGTH_FOR_AUDIO),
              SizeUtils.dp2px(MIN_LENGTH_FOR_AUDIO + (len - 2) * 8));
    }
    getContainer().setLayoutParams(layoutParams);
    RelativeLayout.LayoutParams aniLp =
        (RelativeLayout.LayoutParams) audioBinding.animation.getLayoutParams();
    RelativeLayout.LayoutParams timeLp =
        (RelativeLayout.LayoutParams) audioBinding.tvTime.getLayoutParams();
    if (isReceivedMessage(message)) {
      aniLp.removeRule(ALIGN_PARENT_RIGHT);
      aniLp.addRule(ALIGN_PARENT_LEFT);
      timeLp.removeRule(START_OF);
      timeLp.addRule(END_OF, R.id.animation);
      audioBinding.animation.setImageResource(R.drawable.ic_qchat_message_from_audio);
    } else {
      aniLp.removeRule(ALIGN_PARENT_LEFT);
      aniLp.addRule(ALIGN_PARENT_RIGHT);
      timeLp.removeRule(END_OF);
      timeLp.addRule(START_OF, R.id.animation);
      audioBinding.animation.setImageResource(R.drawable.ic_message_to_audio);
    }
    audioBinding.animation.setLayoutParams(aniLp);
    audioBinding.tvTime.setLayoutParams(timeLp);
  }

  private void playControl(QChatMessageInfo message) {
    AudioAttachment audioAttachment = (AudioAttachment) message.getMessage().getAttachment();
    if (audioAttachment == null) {
      return;
    }
    updateTime(audioAttachment.getDuration());
    if (!isMessagePlaying(message)) {
      if (audioControl.getAudioControlListener() != null
          && audioControl.getAudioControlListener().equals(onPlayListener)) {
        audioControl.changeAudioControlListener(null);
      }
      stop();
    } else {
      audioControl.changeAudioControlListener(onPlayListener);
      play();
    }
  }

  protected boolean isMessagePlaying(QChatMessageInfo message) {
    return audioControl.getPlayingAudio() != null
        && audioControl.getPlayingAudio().getMessage().isTheSame(message.getMessage());
  }
}
