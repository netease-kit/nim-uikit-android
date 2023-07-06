// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main;

import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.attachment.NetCallAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.alog.ParameterMap;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.DefaultCallOrderImpl;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.CallState;
import com.netease.yunxin.nertc.nertcvideocall.utils.CallParams;
import com.netease.yunxin.nertc.nertcvideocall.utils.NrtcCallStatus;
import java.util.Collections;
import java.util.List;

class CustomCallOrderHelper extends DefaultCallOrderImpl {
  private static final String TAG = "CustomCallOrderHelper";

  @Override
  public void onCanceled(ChannelType channelType, String accountId, int callType) {
    ALog.dApi(
        TAG,
        new ParameterMap("onCanceled")
            .append("channelType", channelType)
            .append("callType", callType)
            .append("accountId", accountId)
            .append("enableOrder", isEnable())
            .toValue());
    if (!isEnable()) {
      return;
    }
    sendOrder(channelType, accountId, NrtcCallStatus.NrtcCallStatusCanceled, callType);
  }

  @Override
  public void onReject(ChannelType channelType, String accountId, int callType) {
    ALog.dApi(
        TAG,
        new ParameterMap("onReject")
            .append("channelType", channelType)
            .append("callType", callType)
            .append("accountId", accountId)
            .append("enableOrder", isEnable())
            .toValue());
    if (!isEnable()) {
      return;
    }
    sendOrder(channelType, accountId, NrtcCallStatus.NrtcCallStatusRejected, callType);
  }

  @Override
  public void onTimeout(ChannelType channelType, String accountId, int callType) {
    ALog.dApi(
        TAG,
        new ParameterMap("onTimeout")
            .append("channelType", channelType)
            .append("callType", callType)
            .append("accountId", accountId)
            .append("enableOrder", isEnable())
            .toValue());
    if (!isEnable()) {
      return;
    }
    if (NERTCVideoCall.sharedInstance().getCurrentState() == CallState.STATE_INVITED) {
      return;
    }
    if (NetworkUtils.isConnected()) {
      sendOrder(channelType, accountId, NrtcCallStatus.NrtcCallStatusTimeout, callType);
    } else {
      sendOrder(channelType, accountId, NrtcCallStatus.NrtcCallStatusCanceled, callType);
    }
  }

  @Override
  public void onBusy(ChannelType channelType, String accountId, int callType) {
    ALog.dApi(
        TAG,
        new ParameterMap("onBusy")
            .append("channelType", channelType)
            .append("callType", callType)
            .append("accountId", accountId)
            .append("enableOrder", isEnable())
            .toValue());
    if (!isEnable()) {
      return;
    }
    sendOrder(channelType, accountId, NrtcCallStatus.NrtcCallStatusBusy, callType);
  }

  public static void sendOrder(
      ChannelType channelType, String accountId, int status, int callType) {
    sendOrder(channelType, accountId, status, Collections.emptyList(), callType);
  }

  public static void sendOrder(
      ChannelType channelType,
      String accountId,
      int status,
      List<NetCallAttachment.Duration> durations,
      int callType) {
    ALog.dApi(
        TAG,
        new ParameterMap("sendOrder")
            .append("status", status)
            .append("channelType", channelType)
            .append("callType", callType)
            .append("durations", durations)
            .append("accountId", accountId)
            .toValue());
    if (callType == CallParams.CallType.P2P) {
      NetCallAttachment netCallAttachment =
          new NetCallAttachment.NetCallAttachmentBuilder()
              .withType(channelType != null ? channelType.getValue() : ChannelType.VIDEO.getValue())
              .withStatus(status)
              .withDurations(durations)
              .build();
      IMMessage message =
          MessageBuilder.createNrtcNetcallMessage(
              accountId, SessionTypeEnum.P2P, netCallAttachment);
      ChatRepo.sendMessage(message, true, null);
    }
  }
}
