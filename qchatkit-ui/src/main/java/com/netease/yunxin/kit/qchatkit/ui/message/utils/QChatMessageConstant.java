// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.utils;

import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;

public interface QChatMessageConstant {
  //0 ~100 is User message
  int MESSAGE_USER_VIEW_TYPE_TEXT = MsgTypeEnum.text.getValue();

  int MESSAGE_USER_VIEW_TYPE_IMAGE = MsgTypeEnum.image.getValue();
}
