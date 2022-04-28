package com.netease.yunxin.kit.chatkit.ui.view.interfaces;

import com.netease.nimlib.sdk.msg.model.MsgPinOption;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

import java.util.List;

/**
 * Message list event
 */
public interface IMessageData {

    void addMessageListForward(List<ChatMessageBean> messageList);

    void appendMessageList(List<ChatMessageBean> messageList);

    void appendMessage(ChatMessageBean message);

    void updateMessage(ChatMessageBean message);

    void deleteMessage(ChatMessageBean message);

    void revokeMessage(ChatMessageBean message);

    void addPinMessage(String uuid, MsgPinOption pinOption);

    void removePinMessage(String uuid);

    void clearMessageList();

    void setHasMoreNewerMessages(boolean hasMoreNewerMessages);

    void setHasMoreForwardMessages(boolean hasMoreForwardMessages);

    boolean hasMoreNewerMessages();

    boolean hasMoreForwardMessages();
}
