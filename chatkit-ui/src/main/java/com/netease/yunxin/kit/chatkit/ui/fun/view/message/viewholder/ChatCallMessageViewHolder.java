// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.msg.attachment.NetCallAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageCallViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.ChatMessageViewHolderUIOptions;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.MessageStatusUIOption;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import java.util.List;

public class ChatCallMessageViewHolder extends FunChatBaseMessageViewHolder {

  final int NrtcCallStatusComplete = 1;
  final int NrtcCallStatusCanceled = 2;
  final int NrtcCallStatusRejected = 3;
  final int NrtcCallStatusTimeout = 4;
  final int NrtcCallStatusBusy = 5;
  private static int index = 0;

  FunChatMessageCallViewHolderBinding callBinding;

  public ChatCallMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addViewToMessageContainer() {
    callBinding =
        FunChatMessageCallViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    loadData(message);
  }

  private void loadData(ChatMessageBean messageBean) {

    IMMessage message = messageBean.getMessageData().getMessage();
    if (message == null) {
      return;
    }
    // 此处只处理话单消息
    if (message.getAttachment() instanceof NetCallAttachment) {

      NetCallAttachment attachment = (NetCallAttachment) message.getAttachment();
      /** 消息来源方向详见 {@link MsgDirectionEnum} */
      MsgDirectionEnum direction = message.getDirect();
      // 音频/视频 类型通话
      int type = attachment.getType();
      // 话单类型
      int status = attachment.getStatus();
      int callTypeIconRes;
      if (type == 1) {
        callTypeIconRes = R.drawable.ic_message_call_audio;
      } else {
        callTypeIconRes = R.drawable.ic_message_call_video;
      }
      if (direction == MsgDirectionEnum.In) {
        callBinding.chatMessageCallIconIn.setImageResource(callTypeIconRes);
        callBinding.chatMessageCallIconIn.setVisibility(View.VISIBLE);
        callBinding.chatMessageCallIconOut.setVisibility(View.GONE);
      } else {
        callBinding.chatMessageCallIconOut.setImageResource(callTypeIconRes);
        callBinding.chatMessageCallIconOut.setVisibility(View.VISIBLE);
        callBinding.chatMessageCallIconIn.setVisibility(View.GONE);
      }

      // 时长列表
      List<NetCallAttachment.Duration> durations = attachment.getDurations();
      index++;
      // 按照话单类型解析
      switch (status) {
        case NrtcCallStatusComplete:
          // 成功接听
          if (attachment.getDurations() == null) {
            break;
          }
          // 通话时长渲染
          int seconds = 0;
          for (NetCallAttachment.Duration duration : durations) {
            // 参与通话用户
            String accId = duration.getAccid();
            if (TextUtils.equals(accId, IMKitClient.account())) {
              // 通话时长 单位为 秒
              seconds = duration.getDuration();
              break;
            }
          }
          String resStr =
              getMessageContainer()
                  .getContext()
                  .getResources()
                  .getString(R.string.chat_message_call_completed);
          callBinding.chatMessageCallText.setText(
              String.format(resStr, MessageHelper.formatCallTime(seconds)));
          break;
        case NrtcCallStatusCanceled:
          // 主叫用户取消
          if (direction == MsgDirectionEnum.Out) {
            callBinding.chatMessageCallText.setText(R.string.chat_message_call_canceled);
          } else {
            callBinding.chatMessageCallText.setText(R.string.chat_message_in_call_canceled);
          }
          break;
        case NrtcCallStatusRejected:
          // 被叫用户拒接
          if (direction == MsgDirectionEnum.Out) {
            callBinding.chatMessageCallText.setText(R.string.chat_message_call_refused);
          } else {
            callBinding.chatMessageCallText.setText(R.string.chat_message_call_refused_self);
          }
          break;
        case NrtcCallStatusTimeout:
          // 被叫接听超时
          if (direction == MsgDirectionEnum.Out) {
            callBinding.chatMessageCallText.setText(R.string.chat_message_call_timeout);
          } else {
            callBinding.chatMessageCallText.setText(R.string.chat_message_in_call_canceled);
          }
          break;
        case NrtcCallStatusBusy:
          // 被叫用户在通话中，占线
          if (direction == MsgDirectionEnum.Out) {
            callBinding.chatMessageCallText.setText(R.string.chat_message_call_busy);
          } else {
            callBinding.chatMessageCallText.setText(R.string.chat_message_call_busy_self);
          }
          break;
      }
    }
  }

  @Override
  protected ChatMessageViewHolderUIOptions provideUIOptions(ChatMessageBean messageBean) {
    MessageStatusUIOption messageStatusUIOption = new MessageStatusUIOption();
    messageStatusUIOption.enableStatus = false;
    return ChatMessageViewHolderUIOptions.wrapExitsOptions(super.provideUIOptions(messageBean))
        .messageStatusUIOption(messageStatusUIOption)
        .build();
  }
}
