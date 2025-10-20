// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ChatService;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatAIActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatForwardActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatP2PActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatPinActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatReaderActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatSearchActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatSettingActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatTeamActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunCollectionActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatAIActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatForwardActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatP2PActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatPinActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatReaderActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatSearchActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatSettingActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatTeamActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.CollectionActivity;
import com.netease.yunxin.kit.chatkit.ui.normal.page.CollectionDetailActivity;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/** Chat模块UI服务。在应用启动之后会调用{@link #create(Context)}方法。 当前用于对外能力接口的注册 */
public class ChatUIService extends ChatService {
  private final String TAG = "ChatUIService";

  @NonNull
  @Override
  public String getServiceName() {
    return "ChatUIKit";
  }

  @NonNull
  @Override
  public String getVersionName() {
    return BuildConfig.versionName;
  }

  @NonNull
  @Override
  public ChatService create(@NonNull Context context) {

    // 注册普通聊天页面到路由器
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_P2P_PAGE, ChatP2PActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_AI_P2P_PAGE, ChatAIActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_TEAM_PAGE, ChatTeamActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_SEARCH_PAGE, ChatSearchActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_PIN_PAGE, ChatPinActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_SETTING_PAGE, ChatSettingActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_ACK_PAGE, ChatReaderActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_FORWARD_PAGE, ChatForwardActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_COLLECTION_PAGE, CollectionActivity.class);
    XKitRouter.registerRouter(
        RouterConstant.PATH_COLLECTION_DETAIL_PAGE, CollectionDetailActivity.class);

    // 注册功能聊天页面到路由器
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_CHAT_P2P_PAGE, FunChatP2PActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_CHAT_AI_PAGE, FunChatAIActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_CHAT_TEAM_PAGE, FunChatTeamActivity.class);
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CHAT_SEARCH_PAGE, FunChatSearchActivity.class);

    XKitRouter.registerRouter(RouterConstant.PATH_FUN_CHAT_PIN_PAGE, FunChatPinActivity.class);
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CHAT_READER_PAGE, FunChatReaderActivity.class);
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CHAT_SETTING_PAGE, FunChatSettingActivity.class);
    XKitRouter.registerRouter(
        RouterConstant.PATH_FUN_CHAT_FORWARD_PAGE, FunChatForwardActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_FUN_COLLECTION_PAGE, FunCollectionActivity.class);

    // ===通用逻辑初始化===
    // 注册自定义消息类型
    ChatKitClient.addCustomAttach(
        ChatMessageType.MULTI_FORWARD_ATTACHMENT, MultiForwardAttachment.class);
    ChatKitClient.addCustomAttach(ChatMessageType.RICH_TEXT_ATTACHMENT, RichTextAttachment.class);
    // 添加初始化Listener，SDK初始化之后上层Kit进行部分功能初始化
    IMKitClient.addInitListener(new ChatUIInitService());
    return this;
  }
}
