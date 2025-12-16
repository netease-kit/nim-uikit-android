// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.interfaces;

import android.view.View;
import com.netease.nimlib.coexist.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import java.io.File;
import java.util.Map;

/** handle message event in Chat page */
public interface IMessageProxy {
  boolean sendTextMessage(String msg, ChatMessageBean replyMsg);

  boolean sendRichTextMessage(String title, String content, ChatMessageBean replyMsg);

  void pickMedia();

  void takePicture();

  void captureVideo();

  boolean sendFile();

  boolean sendAudio(File audioFile, int audioLength, ChatMessageBean replyMsg);

  boolean sendCustomMessage(Map<String, Object> attachment, String content);

  void onTypeStateChange(boolean isTyping);

  boolean hasPermission(String[] permission);

  void onCustomAction(View view, String action);

  boolean onActionClick(View view, String action);

  boolean onMultiActionClick(View view, String action);

  void sendLocationLaunch();

  void videoCall();

  void audioCall();

  void onTranslateAction();

  boolean onAIHelperClick(View view, String action);

  String getConversationId();

  V2NIMConversationType getConversationType();
}
