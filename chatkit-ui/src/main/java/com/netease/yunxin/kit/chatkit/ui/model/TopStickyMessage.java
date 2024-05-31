// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model;

import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.yunxin.kit.chatkit.ChatConstants;
import org.json.JSONObject;

/** 置顶消息 */
public class TopStickyMessage {
  String idClient;
  V2NIMConversationType conversationType; //": message.conversationType.rawValue,
  String from; //": message.senderId as Any,
  String to; //": message.conversationId as Any,
  String idServer; //": message.messageServerId as Any,
  long time; //": message.createTime,
  String operator; //": IMKitClient.instance.account(), // 操作者
  int operation; //": 0, // 操作: 0 - "add"; 1 - "remove";

  public TopStickyMessage(
      String idClient,
      V2NIMConversationType conversationType,
      String from,
      String to,
      String idServer,
      long time,
      String operator,
      int operation) {
    this.idClient = idClient;
    this.conversationType = conversationType;
    this.from = from;
    this.to = to;
    this.idServer = idServer;
    this.time = time;
    this.operator = operator;
    this.operation = operation;
  }

  public int getOperation() {
    return operation;
  }

  public long getTime() {
    return time;
  }

  public String getFrom() {
    return from;
  }

  public String getIdClient() {
    return idClient;
  }

  public String getIdServer() {
    return idServer;
  }

  public String getOperator() {
    return operator;
  }

  public String getTo() {
    return to;
  }

  public V2NIMConversationType getConversationType() {
    return conversationType;
  }

  public static TopStickyMessage fromJson(JSONObject jsonObject) {
    try {
      String idClient = jsonObject.getString(ChatConstants.KEY_STICKY_MESSAGE_CLIENT_ID);
      long time = jsonObject.getLong(ChatConstants.KEY_STICKY_MESSAGE_TIME);
      int scene = jsonObject.getInt(ChatConstants.KEY_STICKY_MESSAGE_SCENE);
      String from = jsonObject.getString(ChatConstants.KEY_STICKY_MESSAGE_FROM);
      String to = jsonObject.getString(ChatConstants.KEY_STICKY_MESSAGE_TO);
      String idServer = jsonObject.getString(ChatConstants.KEY_STICKY_MESSAGE_SERVER_ID);
      String operator = jsonObject.getString(ChatConstants.KEY_STICKY_MESSAGE_OPERATOR);
      int operation = jsonObject.getInt(ChatConstants.KEY_STICKY_MESSAGE_OPERATION);
      return new TopStickyMessage(
          idClient,
          V2NIMConversationType.typeOfValue(scene),
          from,
          to,
          idServer,
          time,
          operator,
          operation);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
