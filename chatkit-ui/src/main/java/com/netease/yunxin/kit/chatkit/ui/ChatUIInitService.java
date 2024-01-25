// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification;
import com.netease.yunxin.kit.chatkit.repo.ChatObserverRepo;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatConfigManager;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitService;
import com.netease.yunxin.kit.corekit.im.IIMKitInitService;

/** Chat模块初始化服务。在初始化完成之后，会调用{@link #onInit(Context)}方法。 */
public class ChatUIInitService implements IIMKitInitService {
  @Override
  public void onInit(@NonNull Context context) {
    AitService.getInstance().init(context);
    registerForInsertLocalMsgWhenRevoke();
  }

  private void registerForInsertLocalMsgWhenRevoke() {
    ChatObserverRepo.registerRevokeMessageObserve(
        (Observer<RevokeMsgNotification>)
            revokeMsgNotification -> {
              // 注册消息撤回时是否插入本地消息
              if (ChatConfigManager.enableInsertLocalMsgWhenRevoke) {
                MessageHelper.saveLocalRevokeMessage(revokeMsgNotification.getMessage(), false);
              }
            });
  }
}
