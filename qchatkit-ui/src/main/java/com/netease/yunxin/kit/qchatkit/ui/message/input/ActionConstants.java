// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.input;

public class ActionConstants {
  // input view record action
  public static final String ACTION_TYPE_RECORD = "ACTION_TYPE_RECORD";
  // input view emoji action
  public static final String ACTION_TYPE_EMOJI = "ACTION_TYPE_EMOJI";
  // input view album action
  public static final String ACTION_TYPE_ALBUM = "ACTION_TYPE_ALBUM";
  // input view file action
  public static final String ACTION_TYPE_FILE = "ACTION_TYPE_FILE";
  // input view more action
  public static final String ACTION_TYPE_MORE = "ACTION_TYPE_MORE";

  // select item take photo action
  public static final String ACTION_TYPE_TAKE_PHOTO = "ACTION_TYPE_TAKE_PHOTO";
  // select item take video action
  public static final String ACTION_TYPE_TAKE_VIDEO = "ACTION_TYPE_TAKE_VIDEO";

  // select item video call action
  public static final String ACTION_TYPE_VIDEO_CALL_ACTION = "ACTION_TYPE_VIDEO_CALL_ACTION";
  // select item audio call action
  public static final String ACTION_TYPE_AUDIO_CALL_ACTION = "ACTION_TYPE_AUDIO_CALL_ACTION";

  // action take shoot in more action
  public static final String ACTION_TYPE_CAMERA = "ACTION_TYPE_TAKE_CAMERA";
  // action location in more action
  public static final String ACTION_TYPE_LOCATION = "ACTION_TYPE_LOCATION";
  // action video call in more action
  public static final String ACTION_TYPE_VIDEO_CALL = "ACTION_TYPE_VIDEO_CALL";

  //消息长按菜单-回复
  public static final String POP_ACTION_REPLY = "POP_ACTION_REPLY";
  //消息长按菜单-复制
  public static final String POP_ACTION_COPY = "POP_ACTION_COPY";
  //消息长按菜单-撤回
  public static final String POP_ACTION_RECALL = "POP_ACTION_RECALL";
  //消息长按菜单-标记
  public static final String POP_ACTION_PIN = "POP_ACTION_PIN";
  //消息长按菜单-多选
  public static final String POP_ACTION_MULTI_SELECT = "POP_ACTION_MULTI_SELECT";
  //消息长按菜单-收藏
  public static final String POP_ACTION_COLLECTION = "POP_ACTION_COLLECTION";
  //消息长按菜单-删除
  public static final String POP_ACTION_DELETE = "POP_ACTION_DELETE";
  //消息长按菜单-转发
  public static final String POP_ACTION_TRANSMIT = "POP_ACTION_TRANSMIT";

  //消息列表payload
  public static final String PAYLOAD_STATUS = "messageStatus";
  public static final String PAYLOAD_PROGRESS = "messageProgress";
  public static final String PAYLOAD_REVOKE = "messageRevoke";
  public static final String PAYLOAD_SIGNAL = "messageSignal";
  public static final String PAYLOAD_USERINFO = "userInfo";
}
