// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatStickerViewHolder;
import com.netease.yunxin.kit.chatkit.ui.custom.CustomAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;

public class ChatDefaultFactory extends ChatMessageViewHolderFactory {

  //获取自定义消息的消息类型，一般采用CustomAttachment中的Type区分，Type是由用户定义值（大于1000），不与当前重复即可
  @Override
  public int getCustomViewType(ChatMessageBean messageBean) {
    if (messageBean != null) {
      if (messageBean.getMessageData().getMessage().getMsgType() == MsgTypeEnum.custom) {
        CustomAttachment attachment =
            (CustomAttachment) messageBean.getMessageData().getMessage().getAttachment();
        if (attachment != null) {
          return attachment.getType();
        }
      }
    }
    return -1;
  }

  @Nullable
  @Override
  public ChatBaseMessageViewHolder createViewHolderCustom(@NonNull ViewGroup parent, int viewType) {
    if (viewType == ChatMessageType.CUSTOM_STICKER) {
      ChatBaseMessageViewHolderBinding viewHolderBinding =
          ChatBaseMessageViewHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new ChatStickerViewHolder(viewHolderBinding, viewType);
    }
    return null;
  }
}
