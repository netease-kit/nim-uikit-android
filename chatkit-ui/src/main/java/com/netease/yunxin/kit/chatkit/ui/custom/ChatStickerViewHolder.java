package com.netease.yunxin.kit.chatkit.ui.custom;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageStickerViewBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.StickerManager;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;

public class ChatStickerViewHolder  extends ChatBaseMessageViewHolder {
    private final static String TAG = "ChatStickerViewHolder";

    private ChatMessageStickerViewBinding viewBinding;

    public ChatStickerViewHolder(@NonNull ViewGroup parent, int viewType) {
        super(parent, viewType);
    }

    @Override
    public void addContainer() {
        viewBinding = ChatMessageStickerViewBinding.inflate(LayoutInflater.from(getParent().getContext()),
                getContainer(),true);
    }

    @Override
    public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
        super.bindData(message, lastMessage);
        currentMessage = message;
        StickerAttachment attachment = (StickerAttachment) message.getMessageData().getMessage().getAttachment();
        if (attachment == null) {
            return;
        }

        Glide.with(getContainer().getContext())
                .load(StickerManager.getInstance().getStickerUri(attachment.getCatalog(), attachment.getChartLet()))
                .apply(new RequestOptions()
                        .error(com.netease.yunxin.kit.common.ui.R.drawable.ic_default_img_failed)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(viewBinding.thumbnail);
    }
}
