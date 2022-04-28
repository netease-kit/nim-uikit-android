package com.netease.yunxin.kit.chatkit.ui.model;

import com.netease.nimlib.sdk.msg.model.MsgPinOption;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;

import java.util.Objects;

/**
 * this bean for UI show message，
 * only IMMessageInfo will store in db
 */
public class ChatMessageBean {

    public ChatMessageBean() {

    }

    public ChatMessageBean(IMMessageInfo messageData) {
        this.messageData = messageData;
    }

    IMMessageInfo messageData;

    int viewType;

    boolean haveRead;

    float loadProgress;

    boolean isRevoked;

    public IMMessageInfo getMessageData() {
        return messageData;
    }

    public ChatMessageBean setMessageData(IMMessageInfo messageData) {
        this.messageData = messageData;
        return this;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }

    public void setHaveRead(boolean haveRead) {
        this.haveRead = haveRead;
    }

    public boolean isHaveRead() {
        return haveRead;
    }

    public int getViewType() {
        if (messageData != null) {
            return messageData.getMessage().getMsgType().getValue();
        }
        return viewType;
    }

    public ChatMessageBean setViewType(int viewType) {
        this.viewType = viewType;
        return this;
    }

    public float getLoadProgress() {
        return loadProgress;
    }

    public void setLoadProgress(float loadProgress) {
        this.loadProgress = loadProgress;
    }

    public void setPinAccid(MsgPinOption pinOption) {
        messageData.setPinOption(pinOption);
    }

    public String getPinAccid() {
        return messageData.getPinOption() == null ? null : messageData.getPinOption().getAccount();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessageBean that = (ChatMessageBean) o;
        return viewType == that.viewType && Objects.equals(messageData, that.messageData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageData, viewType);
    }
}
