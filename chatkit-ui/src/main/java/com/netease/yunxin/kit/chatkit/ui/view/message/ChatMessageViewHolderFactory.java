/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.view.message;


import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_AUDIO;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_IMAGE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_VIDEO;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NOTICE_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.TIP_MESSAGE_VIEW_TYPE;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatAudioMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatImageMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatNotificationMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatTextMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatTipsMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatVideoMessageViewHolder;

/**
 * product view holder by type
 */
public abstract class ChatMessageViewHolderFactory implements IChatFactory {

    @Override
    public ChatBaseMessageViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType) {

        ChatBaseMessageViewHolder viewHolder = createViewHolderCustom(parent, viewType);
        if (viewHolder == null) {
            viewHolder = getViewHolderDefault(parent, viewType);
        }
        return viewHolder;
    }

    public abstract @Nullable
    ChatBaseMessageViewHolder createViewHolderCustom(@NonNull ViewGroup parent, int viewType);

    private ChatBaseMessageViewHolder getViewHolderDefault(@NonNull ViewGroup parent, int viewType) {

        ChatBaseMessageViewHolder viewHolder;
        ChatBaseMessageViewHolderBinding viewHolderBinding = ChatBaseMessageViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
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
        } else {
            //default as text message
            viewHolder = new ChatTextMessageViewHolder(viewHolderBinding, viewType);
        }

        return viewHolder;
    }

    public abstract int getCustomViewType(ChatMessageBean messageBean);

    @Override
    public int getItemViewType(ChatMessageBean messageBean){
        if (messageBean != null) {
            int customViewType = getCustomViewType(messageBean);
            if (customViewType > 0){
                return customViewType;
            }else {
                return messageBean.getViewType();
            }
        }
        return 0;
    }
}
