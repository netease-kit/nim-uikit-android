// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.search;

import android.os.Bundle;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatSearchDateViewModel;
import com.netease.yunxin.kit.chatkit.utils.ConversationIdUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class ChatSearchDateActivity extends ChatDateSelectActivity {

  private ChatSearchDateViewModel chatSearchDateViewModel;
  private String conversationId;
  private V2NIMConversationType conversationType;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    chatSearchDateViewModel = new ChatSearchDateViewModel();
    conversationId = getIntent().getStringExtra(RouterConstant.KEY_SESSION_ID);
    if (conversationId == null) {
      finish();
      return;
    }
    conversationType = ConversationIdUtils.conversationType(conversationId);
    chatSearchDateViewModel.init(conversationId);
    chatSearchDateViewModel
        .getSearchDateLiveData()
        .observe(
            this,
            result -> {
              if (result.isSuccess()) {
                V2NIMMessage message = result.getData();
                String routerPath = RouterConstant.PATH_CHAT_TEAM_PAGE;
                if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
                  routerPath = RouterConstant.PATH_CHAT_P2P_PAGE;
                }
                XKitRouter.withKey(routerPath)
                    .withParam(RouterConstant.KEY_MESSAGE, message)
                    .withParam(
                        RouterConstant.CHAT_ID_KRY,
                        ConversationIdUtils.conversationTargetId(conversationId))
                    .withContext(ChatSearchDateActivity.this)
                    .navigate();
              }
              finish();
            });
  }

  @Override
  protected void onSelectDate(long selectedDate) {
    chatSearchDateViewModel.searchDateMessages(selectedDate);
  }
}
