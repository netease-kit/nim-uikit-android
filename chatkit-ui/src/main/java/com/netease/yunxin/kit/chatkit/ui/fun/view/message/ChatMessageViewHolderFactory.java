// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message;

import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.AUDIO_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.CALL_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.FILE_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.IMAGE_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.LOCATION_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NOTICE_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.TIP_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.VIDEO_MESSAGE_VIEW_TYPE;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder.ChatAudioMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder.ChatCallMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder.ChatFileMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder.ChatForwardMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder.ChatImageMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder.ChatLocationMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder.ChatNotificationMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder.ChatRichTextMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder.ChatTextMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder.ChatTipsMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder.ChatVideoMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.CommonBaseMessageViewHolder;

/** product view holder by type */
public abstract class ChatMessageViewHolderFactory implements IChatFactory {

  @Override
  public CommonBaseMessageViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType) {

    CommonBaseMessageViewHolder viewHolder = createViewHolderCustom(parent, viewType);
    if (viewHolder == null) {
      viewHolder = getViewHolderDefault(parent, viewType);
    }
    return viewHolder;
  }

  public abstract @Nullable CommonBaseMessageViewHolder createViewHolderCustom(
      @NonNull ViewGroup parent, int viewType);

  private ChatBaseMessageViewHolder getViewHolderDefault(@NonNull ViewGroup parent, int viewType) {

    ChatBaseMessageViewHolder viewHolder;
    ChatBaseMessageViewHolderBinding viewHolderBinding =
        ChatBaseMessageViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    if (viewType == AUDIO_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatAudioMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == IMAGE_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatImageMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == VIDEO_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatVideoMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NOTICE_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatNotificationMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == TIP_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatTipsMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == FILE_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatFileMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == LOCATION_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatLocationMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == CALL_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatCallMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.MULTI_FORWARD_ATTACHMENT) {
      viewHolder = new ChatForwardMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.RICH_TEXT_ATTACHMENT) {
      viewHolder = new ChatRichTextMessageViewHolder(viewHolderBinding, viewType);
    } else {
      //default as text message
      viewHolder = new ChatTextMessageViewHolder(viewHolderBinding, viewType);
    }

    return viewHolder;
  }

  public abstract int getCustomViewType(ChatMessageBean messageBean);

  @Override
  public int getItemViewType(ChatMessageBean messageBean) {
    if (messageBean != null) {
      int customViewType = getCustomViewType(messageBean);
      if (customViewType > 0) {
        return customViewType;
      } else {
        return messageBean.getViewType();
      }
    }
    return 0;
  }
}
