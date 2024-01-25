// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.view.message;

import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.CALL_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.LOCATION_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.MULTI_FORWARD_ATTACHMENT;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_AUDIO;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_FILE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_IMAGE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_VIDEO;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NOTICE_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.RICH_TEXT_ATTACHMENT;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.TIP_MESSAGE_VIEW_TYPE;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder.ChatAudioMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder.ChatCallMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder.ChatFileMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder.ChatForwardMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder.ChatImageMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder.ChatLocationMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder.ChatNotificationMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder.ChatRichTextMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder.ChatTextMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder.ChatTipsMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder.ChatVideoMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.CommonBaseMessageViewHolder;

/** 标准皮肤，聊天页面消息ViewHolder工厂类。 */
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

  protected CommonBaseMessageViewHolder getViewHolderDefault(
      @NonNull ViewGroup parent, int viewType) {

    ChatBaseMessageViewHolder viewHolder;
    ChatBaseMessageViewHolderBinding viewHolderBinding =
        ChatBaseMessageViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    if (viewType == NORMAL_MESSAGE_VIEW_TYPE_AUDIO) {
      viewHolder = new ChatAudioMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_IMAGE) {
      viewHolder = new ChatImageMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_VIDEO) {
      viewHolder = new ChatVideoMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NOTICE_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatNotificationMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == TIP_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatTipsMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_FILE) {
      viewHolder = new ChatFileMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == LOCATION_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatLocationMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == CALL_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatCallMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == MULTI_FORWARD_ATTACHMENT) {
      //custom message
      viewHolder = new ChatForwardMessageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == RICH_TEXT_ATTACHMENT) {
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
