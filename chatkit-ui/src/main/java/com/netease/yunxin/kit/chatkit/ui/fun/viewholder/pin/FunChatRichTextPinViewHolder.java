// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin;

import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.TextLinkifyUtils;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatRichTextPinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/** view holder for Text message */
public class FunChatRichTextPinViewHolder extends FunChatBasePinViewHolder {

  FunChatRichTextPinViewHolderBinding textBinding;

  public FunChatRichTextPinViewHolder(
      @NonNull FunChatBasePinViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    textBinding =
        FunChatRichTextPinViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
    textBinding
        .getRoot()
        .setOnClickListener(v -> itemListener.onCustomViewClick(v, position, currentMessage));
  }

  @Override
  public void onBindData(ChatMessageBean message, int position) {
    super.onBindData(message, position);
    if (message != null
        && message.getMessageData() != null
        && message.getMessageData().getAttachment() instanceof RichTextAttachment) {
      RichTextAttachment attachment = (RichTextAttachment) message.getMessageData().getAttachment();
      if (attachment != null) {
        textBinding.messageTitle.setText(attachment.title);
        MessageHelper.identifyExpression(
            textBinding.getRoot().getContext(),
            textBinding.messageContent,
            attachment.body,
            message.getMessageData().getMessage());
        // 指定模式（例如只识别电话和邮箱）
        TextLinkifyUtils.addLinks(textBinding.messageTitle, null, position, null);

        // 指定模式（例如只识别电话和邮箱）
        TextLinkifyUtils.addLinks(textBinding.messageContent, null, position, null);
      }
    }
  }
}
