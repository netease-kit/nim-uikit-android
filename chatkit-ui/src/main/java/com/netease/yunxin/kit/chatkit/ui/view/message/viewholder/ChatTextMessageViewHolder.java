package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder;

import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.chatkit.ui.common.MessageUtil;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/**
 * view holder for Text message
 */
public class ChatTextMessageViewHolder extends ChatBaseMessageViewHolder {


    ChatMessageTextViewHolderBinding textBinding;

    public ChatTextMessageViewHolder(@NonNull ViewGroup parent, int viewType) {
        super(parent, viewType);
    }

    @Override
    public void addContainer() {
        textBinding = ChatMessageTextViewHolderBinding.inflate(LayoutInflater.from(getParent().getContext()),
                getContainer(), true);
    }

    @Override
    public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
        super.bindData(message, lastMessage);
        if (properties.getMessageTextSize() != 0) {
            textBinding.messageText.setTextSize(properties.getMessageTextSize());
        }
        if (properties.getMessageTextColor() != 0) {
            textBinding.messageText.setTextColor(properties.getMessageTextColor());
        }
        MessageUtil.identifyFaceExpression(textBinding.getRoot().getContext(), textBinding.messageText, message.getMessageData().getMessage().getContent(), ImageSpan.ALIGN_BOTTOM);
    }
}
