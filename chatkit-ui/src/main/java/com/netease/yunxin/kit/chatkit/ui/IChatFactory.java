package com.netease.yunxin.kit.chatkit.ui;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;

public interface IChatFactory {

    int getItemViewType(ChatMessageBean messageBean);

    ChatBaseMessageViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType);

}
