// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main;

//import com.netease.nimlib.coexist.sdk.msg.constant.SessionTypeEnum;
//import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessage;
//import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessageCreator;
//import com.netease.nimlib.coexist.sdk.v2.message.model.V2NIMMessageCallDuration;
//import com.netease.nimlib.coexist.sdk.v2.utils.V2NIMConversationIdUtil;
//import com.netease.yunxin.kit.alog.ALog;
//import com.netease.yunxin.kit.alog.ParameterMap;
//import com.netease.yunxin.kit.call.p2p.model.NERecord;
//import com.netease.yunxin.kit.call.p2p.model.NERecordProvider;
//import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
//import java.util.Collections;

/** 自定义通话话单提供者 呼叫组件断网、挂断该提供者发送通话话单 */
//public class CustomCallOrderProvider implements NERecordProvider {
//  private static final String TAG = "CustomCallOrderProvider";
//
//  @Override
//  public void onRecordSend(NERecord neRecord) {
//    sendOrder(neRecord.callType, neRecord.accId, neRecord.callState);
//  }
//
//  // 发送通话话单
//  public static void sendOrder(int callType, String accountId, int status) {
//    ALog.dApi(
//        TAG,
//        new ParameterMap("sendOrder")
//            .append("status", status)
//            .append("callType", callType)
//            .append("accountId", accountId)
//            .toValue());
//
//    // 通话时长
//    V2NIMMessageCallDuration durations = new V2NIMMessageCallDuration(accountId, 0);
//    // 创建通话话单消息
//    V2NIMMessage message =
//        V2NIMMessageCreator.createCallMessage(
//            callType, "", status, Collections.singletonList(durations), null);
//    ChatRepo.sendMessage(
//        message,
//        V2NIMConversationIdUtil.conversationId(accountId, SessionTypeEnum.P2P),
//        null,
//        null);
//  }
//}
