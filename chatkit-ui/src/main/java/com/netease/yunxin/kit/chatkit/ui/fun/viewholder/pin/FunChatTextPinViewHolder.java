// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin;

import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.TextLinkifyUtils;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatPinTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.MarkDownViwUtils;

/** view holder for Text message */
public class FunChatTextPinViewHolder extends FunChatBasePinViewHolder {

  FunChatPinTextViewHolderBinding textBinding;

  public FunChatTextPinViewHolder(@NonNull FunChatBasePinViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    textBinding =
        FunChatPinTextViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
    textBinding.messageText.setOnClickListener(
        v -> itemListener.onCustomViewClick(v, position, currentMessage));
  }

  @Override
  public void onBindData(ChatMessageBean message, int position) {
    super.onBindData(message, position);
    // 设置消息文本
    if (MessageHelper.isReceivedMessage(message)) {
      if (properties.getReceiveMessageTextSize() != null) {
        textBinding.messageText.setTextSize(properties.getReceiveMessageTextSize());
      }
      if (properties.getReceiveMessageTextColor() != null) {
        textBinding.messageText.setTextColor(properties.getReceiveMessageTextColor());
      }
    } else {
      if (properties.getSelfMessageTextSize() != null) {
        textBinding.messageText.setTextSize(properties.getSelfMessageTextSize());
      }
      if (properties.getSelfMessageTextColor() != null) {
        textBinding.messageText.setTextColor(properties.getSelfMessageTextColor());
      }
    }
    if (message.getMessageData().getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
      if (MessageHelper.isAIResponseMessage(message.getMessageData())) {
        MarkDownViwUtils.makeMarkDown(
            textBinding.getRoot().getContext(),
            textBinding.messageText,
            message.getMessageData().getMessage().getText());
      } else {
        MessageHelper.identifyExpression(
            textBinding.getRoot().getContext(),
            textBinding.messageText,
            message.getMessageData().getMessage());
      }
    } else {
      //文件消息暂不支持所以展示提示信息
      textBinding.messageText.setText(
          parent.getContext().getResources().getString(R.string.chat_message_not_support_tips));
    }
    // 指定模式（例如只识别电话和邮箱）
    TextLinkifyUtils.addLinks(
        textBinding.messageText,
        new IMessageItemClickListener() {
          @Override
          public boolean onMessageTelClick(
              View view, int position, ChatMessageBean messageInfo, String target) {
            itemListener.onMessageTelClick(view, position, messageInfo, target);
            return true;
          }
        },
        position,
        currentMessage);
  }
}
