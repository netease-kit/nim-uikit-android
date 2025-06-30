// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

/** constants view type 1~9 for default ViewHolder */
public interface IViewTypeConstant {

  // 好友
  int CONTACT_FRIEND = 1;

  // 列表入口Item
  int CONTACT_ACTION_ENTER = 2;

  // 以下为自定义类型起始
  int CUSTOM_START = 10;

  // 黑名单
  int CONTACT_BLACK_LIST = 11;

  // 验证消息
  int CONTACT_VERIFY_INFO = 12;

  // 我的群列表
  int CONTACT_TEAM_LIST = 13;

  // AI数字人
  int CONTACT_AI_USER = 14;

  // 群验证消息
  int CONTACT_TEAM_VERIFY_INFO = 15;
}
