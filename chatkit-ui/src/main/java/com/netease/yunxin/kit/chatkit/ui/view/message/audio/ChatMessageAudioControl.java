// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.audio;

import android.media.MediaPlayer;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.storage.StorageUtil;
import com.netease.yunxin.kit.chatkit.ui.common.ChatCallback;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.adapter.ChatMessageAdapter;
import com.netease.yunxin.kit.corekit.im.audioplayer.BaseAudioControl;
import com.netease.yunxin.kit.corekit.im.audioplayer.Playable;
import java.io.File;
import java.util.List;

public class ChatMessageAudioControl extends BaseAudioControl<IMMessageInfo> {
  private static ChatMessageAudioControl mChatMessageAudioControl = null;

  private boolean mIsNeedPlayNext = false;

  private ChatMessageAdapter mAdapter;

  private IMMessageInfo mItem = null;

  private ChatMessageAudioControl() {
    super(true);
  }

  public static ChatMessageAudioControl getInstance() {
    if (mChatMessageAudioControl == null) {
      synchronized (ChatMessageAudioControl.class) {
        if (mChatMessageAudioControl == null) {
          mChatMessageAudioControl = new ChatMessageAudioControl();
        }
      }
    }

    return mChatMessageAudioControl;
  }

  @Override
  protected void setOnPlayListener(
      Playable playingPlayable, AudioControlListener audioControlListener) {
    this.audioControlListener = audioControlListener;

    BasePlayerListener basePlayerListener =
        new BasePlayerListener(currentAudioPlayer, playingPlayable) {

          @Override
          public void onInterrupt() {
            if (!checkAudioPlayerValid()) {
              return;
            }

            super.onInterrupt();
            cancelPlayNext();
          }

          @Override
          public void onError(String error) {
            if (!checkAudioPlayerValid()) {
              return;
            }

            super.onError(error);
            cancelPlayNext();
          }

          @Override
          public void onCompletion() {
            if (!checkAudioPlayerValid()) {
              return;
            }

            resetAudioController(listenerPlayingPlayable);

            boolean isLoop = false;
            if (mIsNeedPlayNext) {
              if (mAdapter != null && mItem != null) {
                isLoop = playNextAudio(mAdapter, mItem);
              }
            }

            if (!isLoop) {
              if (audioControlListener != null) {
                audioControlListener.onEndPlay(currentPlayable);
              }

              playSuffix();
            }
          }
        };

    basePlayerListener.setAudioControlListener(audioControlListener);
    currentAudioPlayer.setOnPlayListener(basePlayerListener);
  }

  @Override
  public IMMessageInfo getPlayingAudio() {
    if (isPlayingAudio() && currentPlayable instanceof ChatMessageAudioPlayable) {
      return ((ChatMessageAudioPlayable) currentPlayable).getMessage();
    } else {
      return null;
    }
  }

  @Override
  public void startPlayAudioDelay(
      final long delayMillis,
      final IMMessageInfo message,
      final AudioControlListener audioControlListener,
      final int audioStreamType) {
    // if not exit need download
    AudioAttachment audioAttachment = (AudioAttachment) message.getMessage().getAttachment();
    if (audioAttachment == null) {
      return;
    }
    File file = new File(audioAttachment.getPathForSave());
    if (!file.exists()) {
      ChatRepo.downloadAttachment(
          message.getMessage(),
          false,
          new ChatCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
              super.onSuccess(param);
              startPlayAudio(message, audioControlListener, audioStreamType, true, delayMillis);
            }
          });
      return;
    }
    startPlayAudio(message, audioControlListener, audioStreamType, true, delayMillis);
  }

  //need not resetOrigAudioStreamType while play audio one by one
  private void startPlayAudio(
      IMMessageInfo message,
      AudioControlListener audioControlListener,
      int audioStreamType,
      boolean resetOrigAudioStreamType,
      long delayMillis) {
    if (StorageUtil.isExternalStorageExist()) {
      if (startAudio(
          new ChatMessageAudioPlayable(message),
          audioControlListener,
          audioStreamType,
          resetOrigAudioStreamType,
          delayMillis)) {
        // remove unread signal and update database
        if (isUnreadAudioMessage(message)) {
          message.getMessage().setStatus(MsgStatusEnum.read);
          NIMClient.getService(MsgService.class).updateIMMessageStatus(message.getMessage());
        }
      }
    } else {
      //todo show toast sdcard not exist
    }
  }

  private boolean playNextAudio(ChatMessageAdapter tAdapter, IMMessageInfo messageItem) {
    final List<ChatMessageBean> list = tAdapter.getMessageList();
    int index = 0;
    int nextIndex = -1;
    //find the one is playing
    for (int i = 0; i < list.size(); ++i) {
      IMMessageInfo item = list.get(i).getMessageData();
      if (item.equals(messageItem)) {
        index = i;
        break;
      }
    }
    //find next
    for (int i = index; i < list.size(); ++i) {
      IMMessageInfo message = list.get(i).getMessageData();
      if (isUnreadAudioMessage(message)) {
        nextIndex = i;
        break;
      }
    }

    if (nextIndex == -1) {
      cancelPlayNext();
      return false;
    }
    IMMessageInfo message = list.get(nextIndex).getMessageData();
    AudioAttachment attach = (AudioAttachment) message.getMessage().getAttachment();
    if (mChatMessageAudioControl != null && attach != null) {
      if (message.getMessage().getAttachStatus() != AttachStatusEnum.transferred) {
        cancelPlayNext();
        return false;
      }
      if (message.getMessage().getStatus() != MsgStatusEnum.read) {
        message.getMessage().setStatus(MsgStatusEnum.read);
        NIMClient.getService(MsgService.class).updateIMMessageStatus(message.getMessage());
      }
      //continuous play 1.go on playingAudioStreamType 2.stop
      //  resetOrigAudioStreamType
      mChatMessageAudioControl.startPlayAudio(message, null, getCurrentAudioStreamType(), false, 0);
      mItem = list.get(nextIndex).getMessageData();
      //todo need recheck
      tAdapter.notifyItemChanged(index);
      return true;
    }
    return false;
  }

  private void cancelPlayNext() {
    setPlayNext(false, null, null);
  }

  public void setPlayNext(boolean isPlayNext, ChatMessageAdapter adapter, IMMessageInfo item) {
    mIsNeedPlayNext = isPlayNext;
    mAdapter = adapter;
    mItem = item;
  }

  @Override
  protected MediaPlayer getSuffixPlayer() {
    //todo if you need suffix play,set here
    return null;
  }

  public void stopAudio() {
    super.stopAudio();
  }

  public boolean isUnreadAudioMessage(IMMessageInfo message) {
    return (message.getMessage().getMsgType() == MsgTypeEnum.audio)
        && message.getMessage().getDirect() == MsgDirectionEnum.In
        && message.getMessage().getAttachStatus() == AttachStatusEnum.transferred
        && message.getMessage().getStatus() != MsgStatusEnum.read;
  }
}
