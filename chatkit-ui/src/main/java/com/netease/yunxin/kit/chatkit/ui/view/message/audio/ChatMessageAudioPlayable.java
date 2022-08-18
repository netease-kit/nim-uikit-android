// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.audio;

import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.corekit.im.audioplayer.Playable;

/** support audio message control */
public class ChatMessageAudioPlayable implements Playable {
  private final IMMessageInfo message;

  public IMMessageInfo getMessage() {
    return message;
  }

  public ChatMessageAudioPlayable(IMMessageInfo playableMessage) {
    this.message = playableMessage;
  }

  @Override
  public long getDuration() {
    return ((AudioAttachment) message.getMessage().getAttachment()).getDuration();
  }

  @Override
  public String getPath() {
    return ((AudioAttachment) message.getMessage().getAttachment()).getPath();
  }

  @Override
  public boolean isAudioEqual(Playable audio) {
    if (audio instanceof ChatMessageAudioPlayable) {
      return message
          .getMessage()
          .isTheSame(((ChatMessageAudioPlayable) audio).getMessage().getMessage());
    } else {
      return false;
    }
  }
}
