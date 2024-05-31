// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;

/** 消息类型 */
public interface ChatMessageType {

  /** normal message type */
  int TEXT_MESSAGE_VIEW_TYPE = V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT.getValue();

  int IMAGE_MESSAGE_VIEW_TYPE = V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE.getValue();

  int AUDIO_MESSAGE_VIEW_TYPE = V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO.getValue();

  int VIDEO_MESSAGE_VIEW_TYPE = V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO.getValue();

  int FILE_MESSAGE_VIEW_TYPE = V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE.getValue();

  /** notice message type */
  int NOTICE_MESSAGE_VIEW_TYPE = V2NIMMessageType.V2NIM_MESSAGE_TYPE_NOTIFICATION.getValue();

  /** tip message type */
  int TIP_MESSAGE_VIEW_TYPE = V2NIMMessageType.V2NIM_MESSAGE_TYPE_TIPS.getValue();

  /** location message type */
  int LOCATION_MESSAGE_VIEW_TYPE = V2NIMMessageType.V2NIM_MESSAGE_TYPE_LOCATION.getValue();

  int CALL_MESSAGE_VIEW_TYPE = V2NIMMessageType.V2NIM_MESSAGE_TYPE_CALL.getValue();

  // 会话中的会话消息
  int CHAT_MESSAGE_MODE = 0;
  //合并转发详情页的消息
  int FORWARD_MESSAGE_MODE = 1;

  // 多选消息自定义消息类型
  int MULTI_FORWARD_ATTACHMENT = 101;
  int RICH_TEXT_ATTACHMENT = 102;

  /** 自定义消息类型从1000开始 */
  int CUSTOM_START = 1000;

  /** 自定义消息贴图 */
  int CUSTOM_STICKER = 1001;
}
