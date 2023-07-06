// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.custom;

import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.netease.yunxin.app.im.databinding.ChatMessageStickerViewBinding;
import com.netease.yunxin.kit.chatkit.ui.custom.StickerAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.StickerManager;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;

public class ChatStickerViewHolder extends ChatBaseMessageViewHolder {
  private static final String TAG = "ChatStickerViewHolder";

  private ChatMessageStickerViewBinding binding;

  public ChatStickerViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addViewToMessageContainer() {
    binding =
        ChatMessageStickerViewBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    currentMessage = message;
    StickerAttachment attachment =
        (StickerAttachment) message.getMessageData().getMessage().getAttachment();
    if (attachment == null) {
      return;
    }

    Glide.with(parent.getContext())
        .load(
            StickerManager.getInstance()
                .getStickerUri(attachment.getCatalog(), attachment.getChartLet()))
        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
        .into(binding.thumbnail);
  }
}
