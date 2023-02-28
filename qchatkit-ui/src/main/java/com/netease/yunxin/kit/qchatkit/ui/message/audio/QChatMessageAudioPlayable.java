// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.audio;

import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.yunxin.kit.corekit.im.audioplayer.Playable;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;

/** support audio message control */
public class QChatMessageAudioPlayable implements Playable {
  private final QChatMessageInfo message;

  public QChatMessageInfo getMessage() {
    return message;
  }

  public QChatMessageAudioPlayable(QChatMessageInfo playableMessage) {
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
    if (audio instanceof QChatMessageAudioPlayable) {
      return message
          .getMessage()
          .isTheSame(((QChatMessageAudioPlayable) audio).getMessage().getMessage());
    } else {
      return false;
    }
  }
}
