/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.view.message;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.MsgPinOption;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageData;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageLoadHandler;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageReader;
import com.netease.yunxin.kit.chatkit.ui.view.message.adapter.ChatMessageAdapter;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.ChatActionFactory;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.ChatPopMenuActionListener;
import com.netease.yunxin.kit.common.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * recycler view for show message
 */
public class ChatMessageListView extends RecyclerView implements IMessageData {

    private IMessageItemClickListener itemClickListener;

    private ChatMessageAdapter messageAdapter;

    private IMessageLoadHandler loadHandler;

    private IMessageReader messageReader;

    private OnListViewEventListener onListViewEventListener;
    private GestureDetector gestureDetector;
    private boolean isScroll = false;

    private boolean hasMoreForwardMessages;

    private boolean hasMoreNewerMessages;

    public ChatMessageListView(@NonNull Context context) {
        super(context);
        initView(null);
    }

    public ChatMessageListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public ChatMessageListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        setFocusableInTouchMode(false);
        setFocusable(true);
        setClickable(true);
        initRecyclerView();
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (!isScroll) {
                    if (onListViewEventListener != null) {
                        onListViewEventListener.onListViewStartScroll();
                    }
                    isScroll = true;
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!isScroll) {
                    if (onListViewEventListener != null) {
                        onListViewEventListener.onListViewStartScroll();
                    }
                    isScroll = true;
                }
                return true;
            }
        });
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        setLayoutManager(layoutManager);
        setItemAnimator(null);
        messageAdapter = new ChatMessageAdapter();
        setAdapter(messageAdapter);
    }

    /**
     * should set before add data
     *
     * @param viewHolderFactory your factory
     */
    public void setViewHolderFactory(IChatFactory viewHolderFactory) {
        if (viewHolderFactory == null) {
            return;
        }
        if (messageAdapter != null) {
            messageAdapter.setViewHolderFactory(viewHolderFactory);
        }
    }

    public void setOnListViewEventListener(OnListViewEventListener onListViewEventListener) {
        this.onListViewEventListener = onListViewEventListener;
    }

    public IMessageItemClickListener getItemClickListener() {
        return itemClickListener;
    }

    public void setItemClickListener(IMessageItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        if (messageAdapter != null) {
            messageAdapter.setItemClickListener(itemClickListener);
        }
    }

    public void setPopActionListener(ChatPopMenuActionListener listener) {
        ChatActionFactory.getInstance().setActionListener(listener);
    }

    public void setMessageReader(IMessageReader messageReader) {
        this.messageReader = messageReader;
        if (messageAdapter != null) {
            messageAdapter.setMessageReader(messageReader);
        }
    }

    @Override
    public void clearMessageList() {
        messageAdapter.clearMessageList();
    }

    @Override
    public void addMessageListForward(List<ChatMessageBean> messageList) {
        if (messageAdapter != null) {
            messageAdapter.forwardMessages(messageList);
            if (messageReader != null && !messageList.isEmpty()
                    && messageList.get(0).getMessageData().getMessage().getSessionType() == SessionTypeEnum.P2P) {
                messageReader.messageRead(messageList.get(messageList.size() - 1).getMessageData());
            }
        }
    }

    public void setMessageProperties(MessageProperties messageProperties) {
        if (messageAdapter != null) {
            messageAdapter.setMessageProperties(messageProperties);
        }
    }

    @Override
    public void appendMessageList(List<ChatMessageBean> messageList) {
        if (messageAdapter != null) {
            messageAdapter.appendMessages(messageList);
            if (needScrollToBottom()) {
                scrollToEnd();
            }
            if (messageReader != null && !messageList.isEmpty()
                    && messageList.get(0).getMessageData().getMessage().getSessionType() == SessionTypeEnum.P2P) {
                messageReader.messageRead(messageList.get(messageList.size() - 1).getMessageData());
            }
        }
    }

    @Override
    public void appendMessage(ChatMessageBean message) {
        if (messageAdapter != null) {
            messageAdapter.appendMessage(message);
            if (needScrollToBottom()) {
                scrollToEnd();
            }
            if (messageReader != null && message != null
                    && message.getMessageData().getMessage().getDirect() == MsgDirectionEnum.In &&
                    message.getMessageData().getMessage().getSessionType() == SessionTypeEnum.P2P &&
                    message.getMessageData().getMessage().needMsgAck()) {
                messageReader.messageRead(message.getMessageData());
            }
        }
    }

    @Override
    public void updateMessage(ChatMessageBean message) {
        if (messageAdapter != null) {
            messageAdapter.updateMessageStatus(message);
        }
    }

    @Override
    public void revokeMessage(ChatMessageBean message) {
        if (messageAdapter != null) {
            messageAdapter.revokeMessage(message);
        }
    }

    @Override
    public void addPinMessage(String uuid, MsgPinOption pinOption) {
        if (messageAdapter != null) {
            messageAdapter.pinMsg(uuid, pinOption);
        }
    }

    @Override
    public void removePinMessage(String uuid) {
        if (messageAdapter != null) {
            messageAdapter.removeMessagePin(uuid);
        }
    }

    @Override
    public void deleteMessage(ChatMessageBean message) {
        if (messageAdapter != null) {
            messageAdapter.removeMessage(message);
        }
    }

    public void updateTeamInfo(Team teamInfo) {
        if (messageAdapter != null) {
            messageAdapter.setTeamInfo(teamInfo);
        }
    }

    public void setP2PReceipt(long receipt) {
        messageAdapter.setReceiptTime(receipt);
    }

    public void updateAttachmentProgress(AttachmentProgress progress) {
        messageAdapter.updateMessageProgress(progress);
    }

    public void setLoadHandler(IMessageLoadHandler loadHandler) {
        this.loadHandler = loadHandler;
    }

    public void scrollToEnd() {
        if (messageAdapter != null) {
            int itemCount = messageAdapter.getItemCount();
            if (itemCount > 0) {
                post(() -> scrollToPosition(itemCount - 1));
            }
        }
    }

    public ChatMessageBean searchMessage(String messageId) {
        if (messageAdapter != null) {
            return messageAdapter.searchMessage(messageId);
        }
        return null;
    }

    public int searchMessagePosition(String messageId) {
        if (messageAdapter != null) {
            return messageAdapter.searchMessagePosition(messageId);
        }
        return -1;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        gestureDetector.onTouchEvent(e);

        if (e.getAction() == MotionEvent.ACTION_CANCEL || e.getAction() == MotionEvent.ACTION_UP) {
            isScroll = false;
        }
        if (onListViewEventListener != null) {
            onListViewEventListener.onListViewTouched();
        }
        return super.onTouchEvent(e);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (Math.abs(oldh - h) >
                ScreenUtil.getStatusBarHeight(getContext()) + ScreenUtil.getNavBarHeight(getContext())) {
            if (hasMoreNewerMessages) {
                scrollBy(0, oldh - h);
            } else {
                scrollToEnd();
            }
        }
        super.onSizeChanged(w, h, oldw, oldh);
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
                if (firstPosition == 0 && hasMoreForwardMessages) {
                    loadHandler.loadMoreForward(messageAdapter.getFirstMessage());
                } else if (isLastItemVisibleCompleted() && hasMoreNewerMessages) {
                    loadHandler.loadMoreBackground(messageAdapter.getlastMessage());
                }
            }
            refreshTeamMessageReceipt();
        }
    }

    @Override
    public void setHasMoreNewerMessages(boolean hasMoreNewerMessages) {
        this.hasMoreNewerMessages = hasMoreNewerMessages;
    }

    @Override
    public void setHasMoreForwardMessages(boolean hasMoreForwardMessages) {
        this.hasMoreForwardMessages = hasMoreForwardMessages;
    }

    @Override
    public boolean hasMoreNewerMessages() {
        return hasMoreNewerMessages;
    }

    @Override
    public boolean hasMoreForwardMessages() {
        return hasMoreForwardMessages;
    }

    public void refreshTeamMessageReceipt() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        if (layoutManager != null && loadHandler != null) {
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            int lastVisible = layoutManager.findLastVisibleItemPosition();
            loadHandler.onVisibleItemChange(messageAdapter.getMessageList().subList(firstVisible, lastVisible + 1));
        }
    }

    public void scrollToMessage(String msgUuid) {
        if (messageAdapter != null) {
            int index = searchMessagePosition(msgUuid);
            if (index >= 0) {
                smoothScrollToPosition(index);
            }
        }
    }

    private boolean needScrollToBottom() {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();
        if (linearLayoutManager == null) {
            return false;
        }
        int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
        int count = linearLayoutManager.getChildCount();
        return firstPosition + count >= linearLayoutManager.getItemCount() - 1 && !hasMoreNewerMessages;
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

    public ArrayList<ChatMessageBean> filterMessagesByType(int typeValue) {
        ArrayList<ChatMessageBean> mediaMsgList = new ArrayList<>();
        for (ChatMessageBean msg : messageAdapter.getMessageList()) {
            if (msg.getViewType() == typeValue) {
                mediaMsgList.add(msg);
            }
        }
        return mediaMsgList;
    }

    public interface OnListViewEventListener {
        void onListViewStartScroll();

        void onListViewTouched();
    }
}
