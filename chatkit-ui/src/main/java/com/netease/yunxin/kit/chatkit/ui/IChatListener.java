// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;

public interface IChatListener {
  void onSessionChange(String sessionId, SessionTypeEnum typeEnum);
}
