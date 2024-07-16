// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.impl.MessageListenerImpl;
import com.netease.yunxin.kit.chatkit.listener.MessageRevokeNotification;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitService;
import com.netease.yunxin.kit.corekit.im2.IIMKitInitListener;
import java.util.List;

/** Chat模块初始化服务。在初始化完成之后，会调用{@link #onInit(Context)}方法。 */
public class ChatUIInitService implements IIMKitInitListener {

  public final String TAG = "ChatUIInitService";
  private Long timeGap = 2000L;

  @Override
  public void onInit(@NonNull Context context) {
    AitService.getInstance().init(context);
    //注册消息撤回监听
    ChatRepo.addMessageListener(
        new MessageListenerImpl() {
          @Override
          public void onMessageRevokeNotifications(
              @NonNull List<MessageRevokeNotification> revokeNotifications) {
            super.onMessageRevokeNotifications(revokeNotifications);
            for (MessageRevokeNotification revokeNotification : revokeNotifications) {
              // 撤回消息通知,如果不在当前聊天界面则添加到本地消息列表
              if (!TextUtils.equals(
                  ChatRepo.getConversationId(),
                  revokeNotification.getNimNotification().getMessageRefer().getConversationId())) {
                // 当前会话撤回消息
                MessageHelper.saveLocalMessageForOthersRevokeMessage(
                    revokeNotification.getNimNotification());
              }
            }
          }
        });
  }
}
