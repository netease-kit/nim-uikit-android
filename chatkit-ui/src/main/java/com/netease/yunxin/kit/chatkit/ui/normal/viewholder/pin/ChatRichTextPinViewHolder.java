// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin;

import android.text.util.Linkify;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatRichTextPinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

public class ChatRichTextPinViewHolder extends ChatBasePinViewHolder {

  protected ChatRichTextPinViewHolderBinding viewBinding;

  public ChatRichTextPinViewHolder(@NonNull ChatBasePinViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    viewBinding =
        ChatRichTextPinViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
    viewBinding
        .getRoot()
        .setOnClickListener(v -> itemListener.onViewClick(v, position, currentMessage));
  }

  @Override
  public void onBindData(ChatMessageBean message, int position) {
    super.onBindData(message, position);
    if (message != null
        && message.getMessageData() != null
        && message.getMessageData().getAttachment() instanceof RichTextAttachment) {
      RichTextAttachment attachment = (RichTextAttachment) message.getMessageData().getAttachment();
      if (attachment != null) {
        viewBinding.messageTitle.setText(attachment.title);
        MessageHelper.identifyExpression(
            viewBinding.getRoot().getContext(),
            viewBinding.messageContent,
            attachment.body,
            message.getMessageData().getMessage());
        // 指定模式（例如只识别电话和邮箱）
        Linkify.addLinks(
            viewBinding.messageContent,
            Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
        Linkify.addLinks(
            viewBinding.messageTitle,
            Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
      }
    }
  }
}
