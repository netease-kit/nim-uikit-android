// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.audio;

import android.media.MediaPlayer;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.media.player.OnPlayListener;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageAudioAttachment;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.adapter.ChatMessageAdapter;
import com.netease.yunxin.kit.chatkit.utils.MessageExtensionHelper;
import com.netease.yunxin.kit.common.utils.FileUtils;
import com.netease.yunxin.kit.common.utils.storage.StorageUtil;
import com.netease.yunxin.kit.corekit.im2.audioplayer.BaseAudioControl;
import com.netease.yunxin.kit.corekit.im2.audioplayer.Playable;
import com.netease.yunxin.kit.corekit.im2.extend.ProgressFetchCallback;
import com.netease.yunxin.kit.corekit.im2.provider.V2MessageProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class ChatMessageAudioControl extends BaseAudioControl<IMMessageInfo> {
  private static ChatMessageAudioControl mChatMessageAudioControl = null;

  private static final String READ_KEY = "audioMessageHaveRead";

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

  @SuppressWarnings("unchecked")
  public void setAudioControlListenerWhenPlaying(AudioControlListener audioControlListener) {
    if (!isPlayingAudio()) {
      return;
    }
    OnPlayListener playListener = currentAudioPlayer.getOnPlayListener();
    if (playListener instanceof BaseAudioControl<?>.BasePlayerListener) {
      ((BasePlayerListener) playListener).setAudioControlListener(audioControlListener);
    }
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
    V2NIMMessageAudioAttachment audioAttachment =
        (V2NIMMessageAudioAttachment) message.getMessage().getAttachment();
    if (audioAttachment == null) {
      return;
    }
    String path = MessageHelper.getMessageAttachPath(message.getMessage());
    if (!TextUtils.isEmpty(path)) {
      if (FileUtils.isFileExists(path)) {
        startPlayAudio(message, audioControlListener, audioStreamType, true, delayMillis, path);
      } else {
        ChatRepo.downloadAttachment(
            message.getMessage(),
            path,
            new ProgressFetchCallback<>() {
              @Override
              public void onError(int errorCode, @NonNull String errorMsg) {}

              @Override
              public void onSuccess(@Nullable String data) {
                startPlayAudio(
                    message, audioControlListener, audioStreamType, true, delayMillis, data);
              }

              @Override
              public void onProgress(int progress) {}
            });
      }
    }
  }

  //need not resetOrigAudioStreamType while play audio one by one
  private void startPlayAudio(
      IMMessageInfo message,
      AudioControlListener audioControlListener,
      int audioStreamType,
      boolean resetOrigAudioStreamType,
      long delayMillis,
      String filePath) {
    if (StorageUtil.isExternalStorageExist()) {
      if (startAudio(
          new ChatMessageAudioPlayable(message, filePath),
          audioControlListener,
          audioStreamType,
          resetOrigAudioStreamType,
          delayMillis)) {
        if (isUnreadAudioMessage(message)) {
          setAudioMessageHaveRead(message);
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
    V2NIMMessageAudioAttachment attach =
        (V2NIMMessageAudioAttachment) message.getMessage().getAttachment();
    if (mChatMessageAudioControl != null && attach != null) {
      // 判断附件是否存在，如果没又则直接返回
      if (attach.getPath() == null || !FileUtils.isFileExists(attach.getPath())) {
        cancelPlayNext();
        return false;
      }
      //       更新消息已读状态，V2 可能需要放到localExtension 实现
      if (!isUnreadAudioMessage(message)) {
        setAudioMessageHaveRead(message);
      }
      //continuous play 1.go on playingAudioStreamType 2.stop
      //  resetOrigAudioStreamType
      mChatMessageAudioControl.startPlayAudio(
          message, null, getCurrentAudioStreamType(), false, 0, null);
      mItem = list.get(nextIndex).getMessageData();
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
    if (message.getMessage().getMessageType() != V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO
        || message.getMessage().isSelf()) {
      return false;
    }
    Map<String, Object> localExtensionMap =
        MessageExtensionHelper.parseJsonStringToMap(message.getMessage().getLocalExtension());
    if (localExtensionMap != null) {
      Object object = localExtensionMap.get(READ_KEY);
      if (object instanceof Boolean) {
        return !((Boolean) object);
      }
    }
    return false;
  }

  public void setAudioMessageHaveRead(IMMessageInfo message) {
    if (message.getMessage().getMessageType() != V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO
        || message.getMessage().isSelf()) {
      return;
    }
    Map<String, Object> localExtensionMap =
        MessageExtensionHelper.parseJsonStringToMap(message.getMessage().getLocalExtension());
    if (localExtensionMap == null) {
      localExtensionMap = new HashMap<>();
    }
    localExtensionMap.put(READ_KEY, true);
    V2MessageProvider.updateMessageLocalExtension(
        message.getMessage(), (new JSONObject(localExtensionMap)).toString(), null);
  }
}
