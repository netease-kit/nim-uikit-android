// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageRichTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.textSelectionHelper.SelectableTextHelper;

public class ChatRichTextMessageViewHolder extends FunChatBaseMessageViewHolder {

  protected FunChatMessageRichTextViewHolderBinding viewBinding;

  public ChatRichTextMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  protected void addViewToMessageContainer() {
    viewBinding =
        FunChatMessageRichTextViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    if (message != null
        && message.getMessageData() != null
        && message.getMessageData().getAttachment() instanceof RichTextAttachment) {
      RichTextAttachment attachment = (RichTextAttachment) message.getMessageData().getAttachment();
      if (attachment != null) {
        viewBinding.messageTitle.setText(attachment.title);
        if (TextUtils.isEmpty(attachment.body)) {
          viewBinding.messageContent.setVisibility(View.GONE);
        } else {
          viewBinding.messageContent.setVisibility(View.VISIBLE);
          MessageHelper.identifyExpression(
              viewBinding.getRoot().getContext(),
              viewBinding.messageContent,
              attachment.body,
              message.getMessageData().getMessage());
          //    设置选中文本监听回调
          SelectableTextHelper.getInstance()
              .setSelectableOnChangeListener(
                  (view, pos, msg, text, isSelectAll) -> {
                    if (itemClickListener != null) {
                      itemClickListener.onTextSelected(
                          view, pos, msg, text.toString(), isSelectAll);
                    }
                  });
          //    设置长按事件
          viewBinding
              .getRoot()
              .setOnLongClickListener(
                  v -> {
                    if (isMultiSelect) {
                      return true;
                    }
                    SelectableTextHelper.getInstance()
                        .showSelectView(
                            viewBinding.messageContent,
                            viewBinding.messageContent.getLayout(),
                            position,
                            message);
                    return false;
                  });

          if (!isMultiSelect) {
            viewBinding
                .getRoot()
                .setOnClickListener(v -> SelectableTextHelper.getInstance().dismiss());
          } else {
            viewBinding.getRoot().setOnClickListener(this::clickSelect);
          }
        }
      }
    }
  }
}
