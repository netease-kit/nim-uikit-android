// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.audio;

import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageAudioAttachment;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.corekit.im2.audioplayer.Playable;

/** support audio message control */
public class ChatMessageAudioPlayable implements Playable {

  /** 文件路径 */
  public String filePath;

  private final IMMessageInfo message;

  public IMMessageInfo getMessage() {
    return message;
  }

  public ChatMessageAudioPlayable(IMMessageInfo playableMessage, String filePath) {
    this.message = playableMessage;
    this.filePath = filePath;
  }

  @Override
  public long getDuration() {
    return ((V2NIMMessageAudioAttachment) message.getMessage().getAttachment()).getDuration();
  }

  @Override
  public String getPath() {
    if (TextUtils.isEmpty(
        ((V2NIMMessageAudioAttachment) message.getMessage().getAttachment()).getPath())) {
      return filePath;
    } else {
      return ((V2NIMMessageAudioAttachment) message.getMessage().getAttachment()).getPath();
    }
  }

  @Override
  public boolean isAudioEqual(Playable audio) {
    if (audio instanceof ChatMessageAudioPlayable) {
      return message.equals(((ChatMessageAudioPlayable) audio).getMessage());
    } else {
      return false;
    }
  }
}
