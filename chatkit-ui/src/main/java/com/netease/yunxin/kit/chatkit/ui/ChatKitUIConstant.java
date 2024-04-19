// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package com.netease.yunxin.kit.chatkit.ui;

/** Chat模块常量类。 */
public class ChatKitUIConstant {
  public static final String LIB_TAG = "ChatKit-UI";

  public static final int ERROR_CODE_SEND_MSG = -10001;

  public static final int ERROR_CODE_FETCH_MSG = -10002;

  // PIN消息数量限制
  public static final int ERROR_CODE_PIN_MSG_LIMIT = 107319;

  // 被加入黑名单发送消息错误码
  public static final int ERROR_CODE_IN_BLACK_LIST = 102426;

  //发送文件大小限制
  public static final long FILE_LIMIT = 200;

  // 合并转发消息数量限制
  public static final int MULTI_FORWARD_MSG_LIMIT = 100;
  public static final int MULTI_DELETE_MSG_LIMIT = 50;
  public static final int SINGLE_FORWARD_MSG_LIMIT = 10;

  public static final int CHAT_P2P_INVITER_USER_LIMIT = 199;

  public static final int CHAT_FORWARD_USER_LIMIT = 6;
  public static final int CHAT_FORWARD_ABSTRACTS_LIMIT = 3;
  //合并转发深度限制，即合并转发消息嵌套不超过3层
  public static final int CHAT_MULTI_FORWARD_DEEP_LIMIT = 3;

  public static final String REPLY_REMOTE_EXTENSION_KEY = "yxReplyMsg";
  public static final String REPLY_UUID_KEY = "idClient";
  public static final String REPLY_TYPE_KEY = "scene";
  public static final String REPLY_FROM_KEY = "from";
  public static final String REPLY_TO_KEY = "to";
  public static final String REPLY_SERVER_ID_KEY = "idServer";
  public static final String REPLY_TIME_KEY = "time";

  public static final String AIT_REMOTE_EXTENSION_KEY = "yxAitMsg";
  //合并转发消息，消息中的扩展字段，用来保存消息发送者昵称
  public static final String KEY_MERGE_REMOTE_EXTENSION_NICK = "mergedMessageNickKey";
  //合并转发消息，消息中的扩展字段，用来保存消息发送者头像
  public static final String KEY_MERGE_REMOTE_EXTENSION_AVATAR = "mergedMessageAvatarKey";

  public static final int ERROR_CODE_NETWORK = 415;

  public static final String KEY_MAP_FOR_MESSAGE = "chat_message_map";
  public static final String KEY_MAP_FOR_PIN = "chat_pin_map";

  public static final String KEY_MULTI_TRANSMIT_SESSION_ID = "sessionId";
  public static final String KEY_MULTI_TRANSMIT_SESSION_NAME = "sessionName";
  public static final String KEY_MULTI_TRANSMIT_URL = "url";
  public static final String KEY_MULTI_TRANSMIT_MD5 = "md5";
  public static final String KEY_MULTI_TRANSMIT_DEPTH = "depth";
  public static final String KEY_MULTI_TRANSMIT_ABSTRACTS = "abstracts";
  public static final String KEY_MULTI_TRANSMIT_SEND_NICK = "senderNick";
  public static final String KEY_MULTI_TRANSMIT_SEND_CONTENT = "content";
  public static final String KEY_MULTI_TRANSMIT_SEND_ID = "userAccId";
  public static final String KEY_RICH_TEXT_TITLE = "title";
  public static final String KEY_RICH_TEXT_BODY = "body";

  // 群自定义配置参数，用于是否群中所有人都可以@所有人配置的KEY值
  public static final String KEY_EXTENSION_AT_ALL = "yxAllowAt";
  // 群自定义配置参数，用于是否群中管理员可以@所有人，允许所有群成员@所有人
  public static final String TYPE_EXTENSION_ALLOW_ALL = "all";
  // 群自定义配置参数，用于是否群中管理员可以@所有人，只允许管理员@所有人
  public static final String TYPE_EXTENSION_ALLOW_MANAGER = "manager";
}
