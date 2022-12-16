// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.conversationkit.ConversationService;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class ConversationUIService extends ConversationService {

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
  public ConversationService create(@NonNull Context context) {
    XKitRouter.registerRouter(RouterConstant.PATH_CONVERSATION_PAGE, ConversationActivity.class);
    return this;
  }
}
