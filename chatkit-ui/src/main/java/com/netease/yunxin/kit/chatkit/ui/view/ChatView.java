/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatViewLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.chatkit.ui.view.input.MessageBottomLayout;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageLoadHandler;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageProxy;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageReader;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageListView;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageViewHolderFactory;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;

import java.util.List;

/**
 * chat view contain all view about chat
 */
public class ChatView extends LinearLayout {

    ChatViewLayoutBinding binding;

    public ChatView(Context context) {
        super(context);
        init(null);
    }

    public ChatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ChatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        binding = ChatViewLayoutBinding.inflate(layoutInflater, this, true);
        binding.messageView.setOnListViewEventListener(new ChatMessageListView.OnListViewEventListener() {
            @Override
            public void onListViewStartScroll() {
                binding.inputView.collapse(true);
            }

            @Override
            public void onListViewTouched() {
                binding.inputView.collapse(true);
            }
        });
        binding.getRoot().setOnClickListener(v -> binding.inputView.collapse(true));
    }

    public BackTitleBar getTitleBar() {
        return binding.title;
    }

    public ChatMessageListView getMessageListView() {
        return binding.messageView;
    }

    public void setLoadHandler(IMessageLoadHandler loadHandler) {
        binding.messageView.setLoadHandler(loadHandler);
    }

    public void setMessageReader(IMessageReader messageReader) {
        binding.messageView.setMessageReader(messageReader);
    }

    public void setItemClickListener(IMessageItemClickListener itemClickListener) {
        binding.messageView.setItemClickListener(itemClickListener);
    }

    public void clearMessageList() {
        binding.messageView.clearMessageList();
    }

    public void addMessageListForward(List<ChatMessageBean> messageList) {
        binding.messageView.addMessageListForward(messageList);
    }

    public void appendMessageList(List<ChatMessageBean> messageList) {
        binding.messageView.appendMessageList(messageList);
    }

    public void setTypeState(boolean isTyping) {
        if (isTyping) {
            binding.tvNotification.setVisibility(VISIBLE);
            binding.tvNotification.setTextColor(getContext().getResources().getColor(R.color.color_333333));
            binding.tvNotification.setBackgroundResource(R.color.color_white);
            binding.tvNotification.setTextSize(10);
            binding.tvNotification.setText(R.string.chat_message_is_typing);
        } else {
            binding.tvNotification.setVisibility(GONE);
        }
    }

    public void setNetWorkState(boolean available) {
        if (available) {
            binding.tvNotification.setVisibility(GONE);
        } else {
            binding.tvNotification.setVisibility(VISIBLE);
            binding.tvNotification.setTextSize(14);
            binding.tvNotification.setText(R.string.network_error_tip);
            binding.tvNotification.setTextColor(getContext().getResources().getColor(R.color.color_fc596a));
            binding.tvNotification.setBackgroundResource(R.color.color_fee3e6);
        }
    }

    public void appendMessage(ChatMessageBean message) {
        binding.messageView.appendMessage(message);
    }

    public void updateMessage(ChatMessageBean message) {
        binding.messageView.updateMessage(message);
    }

    public void updateProgress(AttachmentProgress progress) {
        binding.messageView.updateAttachmentProgress(progress);
    }

    public void setMessageProxy(IMessageProxy messageProxy) {
        binding.inputView.init(messageProxy);
    }

    public MessageBottomLayout getInputView() {
        return binding.inputView;
    }

    public void setAitManager(AitManager manager) {
        manager.setAitTextChangeListener(binding.inputView);
        binding.inputView.setAitTextWatcher(manager);
    }

    public void setMessageViewHolderFactory(ChatMessageViewHolderFactory viewHolderFactory) {
        binding.messageView.setViewHolderFactory(viewHolderFactory);
    }

    public void setLayoutCustom(IChatViewCustom layoutCustom) {
        if (layoutCustom != null) {
            layoutCustom.customizeChatLayout(this);
        }
    }
}
