/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.qchatkit.ui.message.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.yunxin.kit.corekit.im.XKitImClient;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IMessageList;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IMessageLoadHandler;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IMessageOptionCallBack;
import com.netease.yunxin.kit.qchatkit.ui.message.viewholder.QChatMessageViewHolderFactory;

import java.util.List;

/**
 * listView  for ChatMessage
 */
public class QChatMessageListView extends RecyclerView implements IMessageList {

    private static final int SCROLL_DELAY_TIME = 200;

    QChatMessageAdapter messageAdapter;

    QChatMessageViewHolderFactory viewHolderFactory;

    IMessageLoadHandler loadHandler;

    IMessageOptionCallBack optionCallBack;

    LinearLayoutManager layoutManager;

    private boolean hasMoreForwardMessages = true;

    private boolean hasMoreNewerMessages = false;

    public QChatMessageListView(@NonNull Context context) {
        super(context);
        initView(null);
    }

    public QChatMessageListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public QChatMessageListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }


    private void initView(AttributeSet attrs) {
        initRecycler();
    }

    private void initRecycler() {
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        setLayoutManager(layoutManager);
        viewHolderFactory = new QChatMessageViewHolderFactory();
        messageAdapter = new QChatMessageAdapter(viewHolderFactory);
        setAdapter(messageAdapter);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            if (loadHandler != null
                    && getLayoutManager() != null) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
                int firstPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                int lastPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                if (firstPosition == 0 &&
                        ((lastPosition - firstPosition + 1) < messageAdapter.getItemCount())
                        && hasMoreForwardMessages) {
                    hasMoreForwardMessages = loadHandler.loadMoreForward(messageAdapter.getFirstMessage());
                } else if (isLastItemVisibleCompleted() && hasMoreNewerMessages) {
                    hasMoreNewerMessages = loadHandler.loadMoreBackground(messageAdapter.getlastMessage());
                }
            }
        }
    }

    private boolean isLastItemVisibleCompleted() {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();
        if (linearLayoutManager == null) {
            return false;
        }
        int lastPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
        int childCount = linearLayoutManager.getChildCount();
        int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
        return lastPosition >= firstPosition + childCount - 1;
    }

    public void setHasMoreForwardMessages(boolean hasMoreForwardMessages) {
        this.hasMoreForwardMessages = hasMoreForwardMessages;
    }

    public void setHasMoreNewerMessages(boolean hasMoreNewerMessages) {
        this.hasMoreNewerMessages = hasMoreNewerMessages;
    }

    public void scrollToEnd() {
        if (messageAdapter != null) {
            int itemCount = messageAdapter.getItemCount();
            if (itemCount > 0) {
                post(() -> smoothScrollToPosition(itemCount - 1));
            }
        }
    }


    @Override
    public void appendMessages(List<QChatMessageInfo> message) {
        if (messageAdapter != null) {
            messageAdapter.appendMessages(message);
            layoutManager.scrollToPositionWithOffset(messageAdapter.getItemCount() - 1, Integer.MIN_VALUE);
        }
        if (optionCallBack != null) {
            QChatMessageInfo lastMessage = findLastUnreadMessage(message);
            if (lastMessage != null) {
                optionCallBack.onRead(lastMessage);
            }
        }
    }

    private QChatMessageInfo findLastUnreadMessage(List<QChatMessageInfo> messages) {
        int size = messages.size();
        for (int i = size - 1; i >= 0; i--) {
            QChatMessageInfo messageInfo = messages.get(i);
            String myAccId = XKitImClient.account();
            if (!TextUtils.equals(myAccId, messageInfo.getFromAccount())) {
                return messageInfo;
            }
        }
        return null;
    }

    @Override
    public void appendMessage(QChatMessageInfo message) {
        if (messageAdapter != null) {
            messageAdapter.appendMessages(message);
        }
        scrollToEnd();
        String myAccId = XKitImClient.account();
        if (optionCallBack != null &&
                !TextUtils.equals(myAccId, message.getFromAccount())) {
            optionCallBack.onRead(message);
        }
    }

    @Override
    public void updateMessageStatus(QChatMessageInfo message) {
        if (messageAdapter != null) {
            messageAdapter.updateMessageStatus(message);
        }
    }

    @Override
    public void addMessagesForward(List<QChatMessageInfo> message) {
        if (messageAdapter != null) {
            messageAdapter.forwardMessages(message);
        }
    }

    @Override
    public void deleteMessage(QChatMessageInfo messageInfo) {
        if (messageAdapter != null) {
            messageAdapter.removeMessage(messageInfo);
        }
    }

    @Override
    public void revokeMessage(String messageId) {

    }


    @Override
    public void setLoadHandler(IMessageLoadHandler handler) {
        loadHandler = handler;
    }

    @Override
    public void setOptionCallback(IMessageOptionCallBack callback) {
        this.optionCallBack = callback;
        if (messageAdapter != null) {
            messageAdapter.setOptionCallBack(callback);
        }
    }
}
