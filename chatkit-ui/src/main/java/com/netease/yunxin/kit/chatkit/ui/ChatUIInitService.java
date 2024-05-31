// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.listener.MessageRevokeNotification;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatConfigManager;
import com.netease.yunxin.kit.chatkit.ui.impl.MessageObserverImpl;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitService;
import com.netease.yunxin.kit.corekit.im2.IIMKitInitService;
import java.util.List;

/** Chat模块初始化服务。在初始化完成之后，会调用{@link #onInit(Context)}方法。 */
public class ChatUIInitService implements IIMKitInitService {
  @Override
  public void onInit(@NonNull Context context) {
    AitService.getInstance().init(context);
    registerForInsertLocalMsgWhenRevoke();
  }

  private void registerForInsertLocalMsgWhenRevoke() {
    ChatRepo.addMessageListener(
        new MessageObserverImpl() {
          @Override
          public void onMessageRevokeNotifications(
              List<MessageRevokeNotification> revokeNotifications) {
            if (ChatConfigManager.enableInsertLocalMsgWhenRevoke) {
              for (MessageRevokeNotification revokeNotification : revokeNotifications) {
                //非当前会话的保存
                if (!revokeNotification
                    .getNimNotification()
                    .getMessageRefer()
                    .getConversationId()
                    .equals(ChatRepo.getConversationId())) {
                  MessageHelper.saveLocalMessageForOthersRevokeMessage(
                      revokeNotification.getNimNotification());
                }
              }
            }
          }
        });
  }
}
