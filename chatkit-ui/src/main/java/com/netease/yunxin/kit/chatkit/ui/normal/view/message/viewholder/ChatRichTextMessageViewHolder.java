// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.TextLinkifyUtils;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageRichTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.textSelectionHelper.SelectableTextHelper;

public class ChatRichTextMessageViewHolder extends NormalChatBaseMessageViewHolder {

  protected NormalChatMessageRichTextViewHolderBinding viewBinding;

  public ChatRichTextMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  protected void addViewToMessageContainer() {
    viewBinding =
        NormalChatMessageRichTextViewHolderBinding.inflate(
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
        if (!TextUtils.isEmpty(message.getKeyword())) {
          MessageHelper.identifyFaceExpressionAndHighlight(
              viewBinding.getRoot().getContext(),
              viewBinding.messageTitle,
              attachment.title,
              message.getKeyword(),
              viewBinding.getRoot().getContext().getResources().getColor(R.color.color_337eff));
          if (TextUtils.isEmpty(attachment.body)) {
            viewBinding.messageContent.setVisibility(View.GONE);
          } else {
            viewBinding.messageContent.setVisibility(View.VISIBLE);
            MessageHelper.identifyFaceExpressionAndHighlight(
                viewBinding.getRoot().getContext(),
                viewBinding.messageContent,
                attachment.body,
                message.getKeyword(),
                viewBinding
                    .getRoot()
                    .getContext()
                    .getResources()
                    .getColor(R.color.color_chat_message_highlight));
          }
        } else {
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
          }
          // 指定模式（例如只识别电话和邮箱）
          TextLinkifyUtils.addLinks(
              viewBinding.messageContent, itemClickListener, position, currentMessage);
          TextLinkifyUtils.addLinks(
              viewBinding.messageTitle, itemClickListener, position, currentMessage);
        }
        //    设置选中文本监听回调
        SelectableTextHelper.getInstance()
            .setSelectableOnChangeListener(
                (view, pos, msg, text, isSelectAll) -> {
                  if (itemClickListener != null) {
                    itemClickListener.onTextSelected(view, pos, msg, text.toString(), isSelectAll);
                  }
                });
        //    设置长按事件
        viewBinding.messageContent.setOnLongClickListener(
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

        viewBinding.messageTitle.setOnClickListener(
            v -> {
              if (!isMultiSelect) {
                if (itemClickListener != null) {
                  itemClickListener.onMessageClick(v, position, currentMessage);
                }
                SelectableTextHelper.getInstance().dismiss();
              } else {
                viewBinding.getRoot().setOnClickListener(this::clickSelect);
              }
            });

        viewBinding.messageContent.setOnClickListener(
            v -> {
              if (!isMultiSelect) {
                if (itemClickListener != null) {
                  itemClickListener.onMessageClick(v, position, currentMessage);
                }
                SelectableTextHelper.getInstance().dismiss();
              } else {
                viewBinding.getRoot().setOnClickListener(this::clickSelect);
              }
            });
      }
    }
  }
}
