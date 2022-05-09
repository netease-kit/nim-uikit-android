/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.view.message;


import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.CUSTOM_MESSAGE_VIEW_TYPE_STICKER;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_AUDIO;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_IMAGE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_VIDEO;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NOTICE_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.TIP_MESSAGE_VIEW_TYPE;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatStickerViewHolder;
import com.netease.yunxin.kit.chatkit.ui.custom.CustomAttachment;
import com.netease.yunxin.kit.chatkit.ui.custom.CustomAttachmentType;
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
public abstract class ChatMessageViewHolderFactory {

    public ChatBaseMessageViewHolder getViewHolder(@NonNull ViewGroup parent, int viewType) {

        ChatBaseMessageViewHolder viewHolder = getViewHolderCustom(parent, viewType);
        if (viewHolder == null) {
            viewHolder = getViewHolderDefault(parent, viewType);
        }
        return viewHolder;
    }

    public abstract @Nullable
    ChatBaseMessageViewHolder getViewHolderCustom(@NonNull ViewGroup parent, int viewType);

    private ChatBaseMessageViewHolder getViewHolderDefault(@NonNull ViewGroup parent, int viewType) {

        ChatBaseMessageViewHolder viewHolder;
        if (viewType == NORMAL_MESSAGE_VIEW_TYPE_AUDIO) {
            viewHolder = new ChatAudioMessageViewHolder(parent, viewType);
        } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_IMAGE) {
            viewHolder = new ChatImageMessageViewHolder(parent, viewType);
        } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_VIDEO) {
            viewHolder = new ChatVideoMessageViewHolder(parent, viewType);
        } else if (viewType == NOTICE_MESSAGE_VIEW_TYPE) {
            viewHolder = new ChatNotificationMessageViewHolder(parent, viewType);
        } else if (viewType == TIP_MESSAGE_VIEW_TYPE) {
            viewHolder = new ChatTipsMessageViewHolder(parent, viewType);
        } else if (viewType == CUSTOM_MESSAGE_VIEW_TYPE_STICKER) {
            viewHolder = new ChatStickerViewHolder(parent, viewType);
        } else {
            //default as text message
            viewHolder = new ChatTextMessageViewHolder(parent, viewType);
        }

        return viewHolder;
    }

    public int getItemViewType(ChatMessageBean messageBean){
        if (messageBean != null) {
            if (messageBean.getMessageData().getMessage().getMsgType() == MsgTypeEnum.custom){
                CustomAttachment attachment = (CustomAttachment) messageBean.getMessageData().getMessage().getAttachment();
                if (attachment != null){
                    return CustomAttachmentType.CustomStart + attachment.getType();
                }else {
                    return messageBean.getViewType();
                }
            }else {
                return messageBean.getViewType();
            }
        }
        return 0;
    }
}
