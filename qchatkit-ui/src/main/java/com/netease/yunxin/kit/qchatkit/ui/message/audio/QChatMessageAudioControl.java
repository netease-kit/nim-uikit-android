// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.audio;

import android.media.MediaPlayer;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.common.utils.storage.StorageUtil;
import com.netease.yunxin.kit.corekit.im.audioplayer.BaseAudioControl;
import com.netease.yunxin.kit.corekit.im.audioplayer.Playable;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.QChatMessageRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import com.netease.yunxin.kit.qchatkit.ui.message.view.QChatMessageAdapter;
import java.io.File;
import java.util.List;

public class QChatMessageAudioControl extends BaseAudioControl<QChatMessageInfo> {
  private static QChatMessageAudioControl mChatMessageAudioControl = null;

  private boolean mIsNeedPlayNext = false;

  private QChatMessageAdapter mAdapter;

  private QChatMessageInfo mItem = null;

  private QChatMessageAudioControl() {
    super(true);
  }

  public static QChatMessageAudioControl getInstance() {
    if (mChatMessageAudioControl == null) {
      synchronized (QChatMessageAudioControl.class) {
        if (mChatMessageAudioControl == null) {
          mChatMessageAudioControl = new QChatMessageAudioControl();
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
  public QChatMessageInfo getPlayingAudio() {
    if (isPlayingAudio() && currentPlayable instanceof QChatMessageAudioPlayable) {
      return ((QChatMessageAudioPlayable) currentPlayable).getMessage();
    } else {
      return null;
    }
  }

  @Override
  public void startPlayAudioDelay(
      final long delayMillis,
      final QChatMessageInfo message,
      final AudioControlListener audioControlListener,
      final int audioStreamType) {
    // if not exit need download
    AudioAttachment audioAttachment = (AudioAttachment) message.getMessage().getAttachment();
    if (audioAttachment == null) {
      return;
    }
    File file = new File(audioAttachment.getPathForSave());
    if (!file.exists()) {
      QChatMessageRepo.downloadAttachment(
          message,
          new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
              startPlayAudio(message, audioControlListener, audioStreamType, true, delayMillis);
            }

            @Override
            public void onException(@Nullable Throwable exception) {}

            @Override
            public void onFailed(int code) {}
          });
      return;
    }
    startPlayAudio(message, audioControlListener, audioStreamType, true, delayMillis);
  }

  //need not resetOrigAudioStreamType while play audio one by one
  private void startPlayAudio(
      QChatMessageInfo message,
      AudioControlListener audioControlListener,
      int audioStreamType,
      boolean resetOrigAudioStreamType,
      long delayMillis) {
    if (StorageUtil.isExternalStorageExist()) {
      if (startAudio(
          new QChatMessageAudioPlayable(message),
          audioControlListener,
          audioStreamType,
          resetOrigAudioStreamType,
          delayMillis)) {
        // remove unread signal and update database
        //        if (isUnreadAudioMessage(message)) {
        //          message.getMessage().setStatus(MsgStatusEnum.read);
        //          NIMClient.getService(MsgService.class).updateIMMessageStatus(message.getMessage());
        //        }
      }
    } else {
      //todo show toast sdcard not exist
    }
  }

  private boolean playNextAudio(QChatMessageAdapter tAdapter, QChatMessageInfo messageItem) {
    final List<QChatMessageInfo> list = tAdapter.getMessageList();
    int index = 0;
    int nextIndex = -1;
    //find the one is playing
    for (int i = 0; i < list.size(); ++i) {
      QChatMessageInfo item = list.get(i);
      if (item.equals(messageItem)) {
        index = i;
        break;
      }
    }
    //find next
    for (int i = index; i < list.size(); ++i) {
      QChatMessageInfo message = list.get(i);
      if (isUnreadAudioMessage(message)) {
        nextIndex = i;
        break;
      }
    }

    if (nextIndex == -1) {
      cancelPlayNext();
      return false;
    }
    QChatMessageInfo message = list.get(nextIndex);
    AudioAttachment attach = (AudioAttachment) message.getMessage().getAttachment();
    if (mChatMessageAudioControl != null && attach != null) {
      if (message.getMessage().getAttachStatus() != AttachStatusEnum.transferred) {
        cancelPlayNext();
        return false;
      }
      //      if (message.getMessage().getStatus() != MsgStatusEnum.read) {
      //        message.getMessage().setStatus(MsgStatusEnum.read);
      //        NIMClient.getService(QChatMessageService.class).updateMessage(message.getMessage());
      //      }
      //continuous play 1.go on playingAudioStreamType 2.stop
      //  resetOrigAudioStreamType
      mChatMessageAudioControl.startPlayAudio(message, null, getCurrentAudioStreamType(), false, 0);
      mItem = list.get(nextIndex);
      //todo need recheck
      tAdapter.notifyItemChanged(index);
      return true;
    }
    return false;
  }

  private void cancelPlayNext() {
    setPlayNext(false, null, null);
  }

  public void setPlayNext(boolean isPlayNext, QChatMessageAdapter adapter, QChatMessageInfo item) {
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

  public boolean isUnreadAudioMessage(QChatMessageInfo message) {
    return (message.getMessage().getMsgType() == MsgTypeEnum.audio)
        && message.getMessage().getDirect() == MsgDirectionEnum.In
        && message.getMessage().getAttachStatus() == AttachStatusEnum.transferred
        && message.getMessage().getStatus() != MsgStatusEnum.read;
  }
}
