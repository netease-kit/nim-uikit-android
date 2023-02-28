// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.interfaces;

import android.content.Context;
import java.io.File;

public interface IMessageProxy {

  boolean sendTextMessage(String msg);

  boolean sendImage();

  boolean sendFile();

  boolean sendEmoji();

  boolean sendVoice();

  boolean pickMedia();

  boolean takePicture();

  boolean captureVideo();

  boolean hasPermission(String permission);

  boolean sendAudio(File audioFile, long audioLength);

  void onInputPanelExpand();

  void shouldCollapseInputPanel();

  String getAccount();

  Context getActivityContext();
}
