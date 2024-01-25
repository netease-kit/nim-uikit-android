// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin;

import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatPinTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

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
        v -> itemListener.onViewClick(v, position, currentMessage));
  }

  @Override
  public void onBindData(ChatMessageBean message, int position) {
    super.onBindData(message, position);
    if (properties.getMessageTextSize() != null) {
      textBinding.messageText.setTextSize(properties.getMessageTextSize());
    }
    if (properties.getMessageTextColor() != null) {
      textBinding.messageText.setTextColor(properties.getMessageTextColor());
    }
    if (message.getMessageData().getMessage().getMsgType() == MsgTypeEnum.text) {
      MessageHelper.identifyExpression(
          textBinding.getRoot().getContext(),
          textBinding.messageText,
          message.getMessageData().getMessage());
    } else {
      //文件消息暂不支持所以展示提示信息
      textBinding.messageText.setText(
          parent.getContext().getResources().getString(R.string.chat_message_not_support_tips));
    }
  }
}
