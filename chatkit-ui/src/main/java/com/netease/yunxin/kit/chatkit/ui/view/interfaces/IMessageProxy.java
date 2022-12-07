// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.interfaces;

import android.view.View;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import java.io.File;

/** handle message event in Chat page */
public interface IMessageProxy {
  boolean sendTextMessage(String msg, ChatMessageBean replyMsg);

  void pickMedia();

  void takePicture();

  void captureVideo();

  boolean sendFile();

  boolean sendAudio(File audioFile, long audioLength, ChatMessageBean replyMsg);

  boolean sendCustomMessage(MsgAttachment attachment, String content);

  void onTypeStateChange(boolean isTyping);

  boolean hasPermission(String permission);

  void onCustomAction(View view, String action);
}
