// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main;

import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.attachment.NetCallAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.alog.ParameterMap;
import com.netease.yunxin.kit.call.p2p.model.NERecord;
import com.netease.yunxin.kit.call.p2p.model.NERecordProvider;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;

public class CustomCallOrderProvider implements NERecordProvider {
  private static final String TAG = "CustomCallOrderProvider";

  @Override
  public void onRecordSend(NERecord neRecord) {
    sendOrder(neRecord.callType, neRecord.accId, neRecord.callState);
  }

  public static void sendOrder(int callType, String accountId, int status) {
    ALog.dApi(
        TAG,
        new ParameterMap("sendOrder")
            .append("status", status)
            .append("callType", callType)
            .append("accountId", accountId)
            .toValue());
    NetCallAttachment netCallAttachment =
        new NetCallAttachment.NetCallAttachmentBuilder()
            .withType(callType)
            .withStatus(status)
            .build();
    IMMessage message =
        MessageBuilder.createNrtcNetcallMessage(accountId, SessionTypeEnum.P2P, netCallAttachment);
    ChatRepo.sendMessage(message, true, null);
  }
}
