// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.custom.NERTCCallAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageCallViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.ChatMessageViewHolderUIOptions;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.MessageStatusUIOption;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;

public class ChatCallMessageViewHolder extends FunChatBaseMessageViewHolder {

  FunChatMessageCallViewHolderBinding callBinding;

  protected NERTCCallAttachment attachment;

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

    V2NIMMessage message = messageBean.getMessageData().getMessage();
    if (messageBean.getMessageData().getCustomData() instanceof NERTCCallAttachment) {
      attachment = (NERTCCallAttachment) messageBean.getMessageData().getCustomData();
    } else if (message.getAttachment() != null) {
      // 此处只处理话单消息
      String attachmentRaw = message.getAttachment().getRaw();
      ALog.d("ChatCallMessageViewHolder", "attachment: " + attachmentRaw);
      if (TextUtils.isEmpty(attachmentRaw)) {
        return;
      }
      attachment = new NERTCCallAttachment(message.getMessageClientId(), attachmentRaw);
      messageBean.getMessageData().setCustomData(attachment);
    }
    boolean isSelf = message.isSelf();
    int callTypeIconRes;

    if (isForwardMsg()) {
      String callText = "";
      if (attachment != null && attachment.callType == 1) {
        callText =
            getMessageContainer().getContext().getString(R.string.chat_message_audio_call_text);
      } else if (attachment != null && attachment.callType == 2) {
        callText =
            getMessageContainer().getContext().getString(R.string.chat_message_video_call_text);
      } else {
        callText =
            getMessageContainer().getContext().getString(R.string.chat_message_not_call_tips);
      }
      callBinding.chatMessageCallIconIn.setVisibility(View.GONE);
      callBinding.chatMessageCallIconOut.setVisibility(View.GONE);
      callBinding.chatMessageCallText.setText(callText);
      return;
    }
    if (attachment.callType == 1) {
      callTypeIconRes = R.drawable.ic_message_call_audio;
    } else if (attachment.callType == 2) {
      callTypeIconRes = R.drawable.ic_message_call_video;
    } else {
      callTypeIconRes = 0;
    }
    if (!isSelf) {
      callBinding.chatMessageCallIconIn.setImageResource(callTypeIconRes);
      callBinding.chatMessageCallIconIn.setVisibility(View.VISIBLE);
      callBinding.chatMessageCallIconOut.setVisibility(View.GONE);
    } else {
      callBinding.chatMessageCallIconOut.setImageResource(callTypeIconRes);
      callBinding.chatMessageCallIconOut.setVisibility(View.VISIBLE);
      callBinding.chatMessageCallIconIn.setVisibility(View.GONE);
    }

    // 按照话单类型解析
    switch (attachment.callStatus) {
      case NERTCCallAttachment.NrtcCallStatusComplete:
        // 成功接听
        if (attachment.durationList.size() < 1) {
          break;
        }
        // 通话时长渲染
        int seconds = 0;
        for (NERTCCallAttachment.Duration duration : attachment.durationList) {
          // 参与通话用户
          String accId = duration.accid;
          if (TextUtils.equals(accId, IMKitClient.account())) {
            // 通话时长 单位为 秒
            seconds = duration.duration;
            break;
          }
        }
        String resStr =
            getMessageContainer()
                .getContext()
                .getResources()
                .getString(R.string.chat_message_call_completed);
        callBinding.chatMessageCallText.setText(
            String.format(resStr, ChatUtils.formatCallTime(seconds)));
        break;
      case NERTCCallAttachment.NrtcCallStatusCanceled:
        // 主叫用户取消
        if (isSelf) {
          callBinding.chatMessageCallText.setText(R.string.chat_message_call_canceled);
        } else {
          callBinding.chatMessageCallText.setText(R.string.chat_message_in_call_canceled);
        }
        break;
      case NERTCCallAttachment.NrtcCallStatusRejected:
        // 被叫用户拒接
        if (!isSelf) {
          callBinding.chatMessageCallText.setText(R.string.chat_message_call_refused);
        } else {
          callBinding.chatMessageCallText.setText(R.string.chat_message_call_refused_self);
        }
        break;
      case NERTCCallAttachment.NrtcCallStatusTimeout:
        // 被叫接听超时
        if (isSelf) {
          callBinding.chatMessageCallText.setText(R.string.chat_message_call_timeout);
        } else {
          callBinding.chatMessageCallText.setText(R.string.chat_message_in_call_canceled);
        }
        break;
      case NERTCCallAttachment.NrtcCallStatusBusy:
        // 被叫用户在通话中，占线
        if (isSelf) {
          callBinding.chatMessageCallText.setText(R.string.chat_message_call_busy);
        } else {
          callBinding.chatMessageCallText.setText(R.string.chat_message_call_busy_self);
        }
        break;
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
