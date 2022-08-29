// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ChatService;
import com.netease.yunxin.kit.chatkit.ui.page.ChatP2PActivity;
import com.netease.yunxin.kit.chatkit.ui.page.ChatSearchActivity;
import com.netease.yunxin.kit.chatkit.ui.page.ChatTeamActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/** launch service when app start the ChatUIService will be created it need to config in manifest */
public class ChatUIService extends ChatService {

  @NonNull
  @Override
  public String getServiceName() {
    return "ChatUIService";
  }

  @NonNull
  @Override
  public String getVersionName() {
    return BuildConfig.versionName;
  }

  @NonNull
  @Override
  public ChatService create(@NonNull Context context) {
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_P2P_PAGE, ChatP2PActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_TEAM_PAGE, ChatTeamActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_SEARCH_PAGE, ChatSearchActivity.class);
    return this;
  }
}
