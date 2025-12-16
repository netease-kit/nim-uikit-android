// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection;

import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static android.widget.RelativeLayout.END_OF;
import static android.widget.RelativeLayout.START_OF;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_REFRESH_AUDIO_ANIM;

import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import com.netease.nimlib.coexist.sdk.v2.message.attachment.V2NIMMessageAudioAttachment;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunCollectionAudioViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunCollectionBaseViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.CollectionBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.audio.ChatMessageAudioControl;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.coexist.im2.audioplayer.Playable;
import java.util.List;
import java.util.Objects;

/** 收藏列表中语音消息ViewHolder */
public class FunCollectionAudioViewHolder extends FunCollectionBaseViewHolder {

  // 语音消息布局
  private FunCollectionAudioViewHolderBinding audioBinding;

  private ChatMessageAudioControl audioControl;

  public static final int CLICK_TO_PLAY_AUDIO_DELAY = 500;

  public static final int MAX_LENGTH_FOR_AUDIO = 250;

  public static final int MIN_LENGTH_FOR_AUDIO = 80;

  private final ChatMessageAudioControl.AudioControlListener onPlayListener =
      new ChatMessageAudioControl.AudioControlListener() {

        @Override
        public void updatePlayingProgress(Playable playable, long curPosition) {
          // do nothing
        }

        @Override
        public void onAudioControllerReady(Playable playable) {
          if (isTheSame(currentCollection.getMessageData().getMessageClientId())) {
            return;
          }
          play();
        }

        @Override
        public void onEndPlay(Playable playable) {
          if (isTheSame(currentCollection.getMessageData().getMessageClientId())) {
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
    audioBinding.animation.setImageResource(R.drawable.ani_message_audio_from);
  }

  private void endPlayAnim() {
    audioBinding.animation.setImageResource(R.drawable.ic_message_from_audio);
  }

  private void updateTime(long milliseconds) {
    long seconds = milliseconds / 1000;

    if (seconds <= 0) {
      seconds = 1;
    }
    audioBinding.audioTimeTv.setText(String.format("%ss", seconds));
  }

  private boolean isTheSame(String uuid) {
    String current = audioBinding.audioTimeTv.getTag().toString();
    return TextUtils.isEmpty(uuid) || !uuid.equals(current);
  }

  public FunCollectionAudioViewHolder(
      @NonNull FunCollectionBaseViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    audioBinding =
        FunCollectionAudioViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
  }

  @Override
  public void onBindData(CollectionBean message, int position) {
    super.onBindData(message, position);
    audioControl = ChatMessageAudioControl.getInstance();
    audioBinding.audioTimeTv.setTag(message.getMessageData().getMessageClientId());
    playControl(message);
    setAudioLayout(message);
    checkAudioPlayAndRefreshAnim();
  }

  @Override
  public void onBindData(CollectionBean data, int position, @NonNull List<?> payload) {
    super.onBindData(data, position, payload);
    if (payload.contains(PAYLOAD_REFRESH_AUDIO_ANIM)) {
      initPlayAnim();
      audioControl.startPlayAudioDelay(
          CLICK_TO_PLAY_AUDIO_DELAY, new IMMessageInfo(data.getMessageData()), onPlayListener);
    }
  }

  private void checkAudioPlayAndRefreshAnim() {
    if (currentCollection == null) {
      return;
    }
    if (audioControl == null) {
      audioControl = ChatMessageAudioControl.getInstance();
    }
    if (!Objects.equals(audioControl.getPlayingAudio(), currentCollection.getMessageData())) {
      return;
    }
    audioControl.setAudioControlListenerWhenPlaying(onPlayListener);
    initPlayAnim();
    play();
  }

  private void setAudioLayout(CollectionBean message) {
    V2NIMMessageAudioAttachment audioAttachment =
        (V2NIMMessageAudioAttachment) message.getMessageData().getAttachment();
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
        (RelativeLayout.LayoutParams) audioBinding.audioTimeTv.getLayoutParams();
    aniLp.removeRule(ALIGN_PARENT_RIGHT);
    aniLp.addRule(ALIGN_PARENT_LEFT);
    timeLp.removeRule(START_OF);
    timeLp.addRule(END_OF, R.id.animation);
    audioBinding.animation.setImageResource(R.drawable.ic_message_from_audio);
    audioBinding.animation.setLayoutParams(aniLp);
    audioBinding.audioTimeTv.setLayoutParams(timeLp);
  }

  private void playControl(CollectionBean message) {
    V2NIMMessageAudioAttachment audioAttachment =
        (V2NIMMessageAudioAttachment) message.getMessageData().getAttachment();
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

  protected boolean isMessagePlaying(CollectionBean message) {
    return audioControl.getPlayingAudio() != null
        && audioControl.getPlayingAudio().equals(message.getMessageData());
  }
}
