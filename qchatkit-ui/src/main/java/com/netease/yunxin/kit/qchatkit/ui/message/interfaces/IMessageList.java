// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.interfaces;

import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import java.util.List;

public interface IMessageList {
  /**
   * append a message list
   *
   * @param messages message list
   */
  void appendMessages(List<QChatMessageInfo> messages);

  /**
   * append one message,when you sen a message or received a message， call this and show in the view
   *
   * @param message one message
   */
  void appendMessage(QChatMessageInfo message);

  /**
   * update one message status as send message success
   *
   * @param message one message
   */
  void updateMessageStatus(QChatMessageInfo message);

  /**
   * add a message list forward
   *
   * @param message message list
   */
  void addMessagesForward(List<QChatMessageInfo> message);

  /** delete a message */
  void deleteMessage(QChatMessageInfo messageInfo);

  /** revoke message by id */
  void revokeMessage(String messageId);

  void setLoadHandler(IMessageLoadHandler handler);

  /**
   * set option call back
   *
   * @param callback call back
   */
  void setOptionCallback(IMessageOptionCallBack callback);
}
