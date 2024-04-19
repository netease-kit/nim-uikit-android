// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.common;

/** 会话模块常量 */
public class ConversationConstant {

  // 会话模块日志TAG
  public static final String LIB_TAG = "ConversationKit-UI";
  // 群默认人数限制大小
  public static final int MAX_TEAM_MEMBER = 200;
  // IM SDK 网络错误码
  public static final int ERROR_CODE_NETWORK = 192003;
  // 会话类型，列表中ViewHolder使用的ViewType，区分P2P和Team
  public static class ViewType {
    //p2p chat view
    public static final int CHAT_VIEW = 1;
    //team chat view
    public static final int TEAM_VIEW = 2;
  }

  // 会话弹窗操作按钮Action，区分点击事件使用
  public static class Action {
    //action to add or remove stick
    public static final String ACTION_STICK = "conversation/action/stick";
    //action to delete conversation
    public static final String ACTION_DELETE = "conversation/action/delete";
  }
}
