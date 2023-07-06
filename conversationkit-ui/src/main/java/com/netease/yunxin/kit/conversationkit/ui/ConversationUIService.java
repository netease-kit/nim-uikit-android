// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ChatService;
import com.netease.yunxin.kit.conversationkit.ui.fun.page.FunConversationActivity;
import com.netease.yunxin.kit.conversationkit.ui.normal.page.ConversationActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class ConversationUIService extends ChatService {

  @NonNull
  @Override
  public String getServiceName() {
    return "ConversationUIKit";
  }

  @NonNull
  @Override
  public String getVersionName() {
    return BuildConfig.versionName;
  }

  @NonNull
  @Override
  public ChatService create(@NonNull Context context) {
    //normal
    XKitRouter.registerRouter(RouterConstant.PATH_CONVERSATION_PAGE, ConversationActivity.class);

    //fun
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CONVERSATION_PAGE, FunConversationActivity.class);
    return this;
  }
}
