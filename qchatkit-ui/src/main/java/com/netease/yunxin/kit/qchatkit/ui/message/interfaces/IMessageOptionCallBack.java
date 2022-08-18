// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.interfaces;

import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;

public interface IMessageOptionCallBack {
  /** this message have been read */
  void onRead(QChatMessageInfo message);

  /** resend on failed message */
  void reSend(QChatMessageInfo message);

  /** copy a message success */
  void onCopy(QChatMessageInfo message);
}
