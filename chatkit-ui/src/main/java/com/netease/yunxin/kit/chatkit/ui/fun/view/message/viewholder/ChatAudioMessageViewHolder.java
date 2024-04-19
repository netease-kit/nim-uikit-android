// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static android.widget.RelativeLayout.END_OF;
import static android.widget.RelativeLayout.START_OF;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_REFRESH_AUDIO_ANIM;

import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageAudioAttachment;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageAudioViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.audio.ChatMessageAudioControl;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im2.audioplayer.Playable;
import java.util.List;
import java.util.Objects;

/** view holder for audio message */
public class ChatAudioMessageViewHolder extends FunChatBaseMessageViewHolder {

  private FunChatMessageAudioViewHolderBinding audioBinding;

  private ChatMessageBean currentMessage;

  private ChatMessageAudioControl audioControl;

  public static final int CLICK_TO_PLAY_AUDIO_DELAY = 500;

  public static final int MAX_LENGTH_FOR_AUDIO = 250;

  public static final int MIN_LENGTH_FOR_AUDIO = 80;

  private final ChatMessageAudioControl.AudioControlListener onPlayListener =
      new ChatMessageAudioControl.AudioControlListener() {

        @Override
        public void updatePlayingProgress(Playable playable, long curPosition) {
          //do nothing
        }

        @Override
        public void onAudioControllerReady(Playable playable) {
          if (isTheSame(currentMessage.getMessageData().getMessage().getMessageClientId())) {
            return;
          }
          play();
        }

        @Override
        public void onEndPlay(Playable playable) {
          if (isTheSame(currentMessage.getMessageData().getMessage().getMessageClientId())) {
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
    if (MessageHelper.isReceivedMessage(currentMessage)) {
      audioBinding.animation.setImageResource(R.drawable.ani_message_audio_from);
    } else {
      audioBinding.animation.setImageResource(R.drawable.ani_message_audio_to);
    }
  }

  private void endPlayAnim() {
    if (MessageHelper.isReceivedMessage(currentMessage)) {
      audioBinding.animation.setImageResource(R.drawable.ic_message_from_audio);
    } else {
      audioBinding.animation.setImageResource(R.drawable.ic_message_to_audio);
    }
  }

  private void updateTime(long milliseconds) {
    long seconds = milliseconds / 1000;

    if (seconds <= 0) {
      seconds = 1;
    }
    audioBinding.tvTime.setText(String.format("%s \"", seconds));
  }

  private boolean isTheSame(String uuid) {
    String current = audioBinding.tvTime.getTag().toString();
    return TextUtils.isEmpty(uuid) || !uuid.equals(current);
  }

  public ChatAudioMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addViewToMessageContainer() {
    audioBinding =
        FunChatMessageAudioViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    if (isForwardMsg()) {
      audioBinding.animation.setVisibility(View.GONE);
      audioBinding.tvTime.setVisibility(View.GONE);
      audioBinding.messageText.setVisibility(View.VISIBLE);
      return;
    }
    audioBinding.animation.setVisibility(View.VISIBLE);
    audioBinding.tvTime.setVisibility(View.VISIBLE);
    audioBinding.messageText.setVisibility(View.GONE);
    audioControl = ChatMessageAudioControl.getInstance();
    audioBinding.tvTime.setTag(message.getMessageData().getMessage().getMessageClientId());
    currentMessage = message;
    playControl(message);
    setAudioLayout(message);
    audioBinding.container.setOnClickListener(
        v -> {
          if (isMultiSelect) {
            clickSelect(v);
            return;
          }
          if (itemClickListener != null) {
            itemClickListener.onMessageClick(v, position, message);
          }
        });
    checkAudioPlayAndRefreshAnim();
  }

  @Override
  public void bindData(ChatMessageBean message, int position, @NonNull List<?> payload) {
    super.bindData(message, position, payload);
    if (payload.contains(PAYLOAD_REFRESH_AUDIO_ANIM)) {
      initPlayAnim();
      audioControl.setEarPhoneModeEnable(SettingRepo.getHandsetMode());
      audioControl.startPlayAudioDelay(
          CLICK_TO_PLAY_AUDIO_DELAY, message.getMessageData(), onPlayListener);
    }
  }

  private void checkAudioPlayAndRefreshAnim() {
    if (currentMessage == null) {
      return;
    }
    if (audioControl == null) {
      audioControl = ChatMessageAudioControl.getInstance();
    }
    if (!Objects.equals(audioControl.getPlayingAudio(), currentMessage.getMessageData())) {
      return;
    }
    audioControl.setAudioControlListenerWhenPlaying(onPlayListener);
    initPlayAnim();
    play();
  }

  private void setAudioLayout(ChatMessageBean message) {
    V2NIMMessageAudioAttachment audioAttachment =
        (V2NIMMessageAudioAttachment) message.getMessageData().getMessage().getAttachment();
    if (audioAttachment == null) {
      return;
    }
    long len = audioAttachment.getDuration() / 1000;
    ViewGroup.LayoutParams layoutParams = audioBinding.getRoot().getLayoutParams();
    if (len <= 2) {
      layoutParams.width = SizeUtils.dp2px(MIN_LENGTH_FOR_AUDIO);
    } else {
      layoutParams.width =
          Math.min(
              SizeUtils.dp2px(MAX_LENGTH_FOR_AUDIO),
              SizeUtils.dp2px(MIN_LENGTH_FOR_AUDIO + (len - 2) * 8));
    }
    audioBinding.getRoot().setLayoutParams(layoutParams);
    RelativeLayout.LayoutParams aniLp =
        (RelativeLayout.LayoutParams) audioBinding.animation.getLayoutParams();
    RelativeLayout.LayoutParams timeLp =
        (RelativeLayout.LayoutParams) audioBinding.tvTime.getLayoutParams();
    if (MessageHelper.isReceivedMessage(message)) {
      aniLp.removeRule(ALIGN_PARENT_RIGHT);
      aniLp.addRule(ALIGN_PARENT_LEFT);
      timeLp.removeRule(START_OF);
      timeLp.addRule(END_OF, R.id.animation);
      audioBinding.animation.setImageResource(R.drawable.ic_message_from_audio);
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

  private void playControl(ChatMessageBean message) {
    V2NIMMessageAudioAttachment audioAttachment =
        (V2NIMMessageAudioAttachment) message.getMessageData().getMessage().getAttachment();
    if (audioAttachment == null) {
      return;
    }
    updateTime(audioAttachment.getDuration());
    if (!isMessagePlaying(message)) {
      if (audioControl.getAudioControlListener() != null
          && audioControl.getAudioControlListener().equals(onPlayListener)) {
        audioControl.changeAudioControlListener(null);
      }

      updateTime(audioAttachment.getDuration());
      stop();
    } else {
      audioControl.changeAudioControlListener(onPlayListener);
      play();
    }
  }

  protected boolean isMessagePlaying(ChatMessageBean message) {
    return audioControl.getPlayingAudio() != null
        && audioControl.getPlayingAudio().equals(message.getMessageData());
  }
}
