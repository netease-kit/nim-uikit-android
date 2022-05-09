/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.view.message;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

/**
 * message item config
 */
public class MessageProperties {

    private Drawable receiveMessageBg;

    private Drawable selfMessageBg;

    private @ColorInt
    int userNickColor;

    private @ColorInt
    int messageTextColor;

    private int messageTextSize;

    public void setReceiveMessageBg(Drawable receiveMessageBg) {
        this.receiveMessageBg = receiveMessageBg;
    }

    public Drawable getReceiveMessageBg() {
        return receiveMessageBg;
    }

    public void setSelfMessageBg(Drawable selfMessageBg) {
        this.selfMessageBg = selfMessageBg;
    }

    public Drawable getSelfMessageBg() {
        return selfMessageBg;
    }

    public void setUserNickColor(@ColorInt int userNickColor) {
        this.userNickColor = userNickColor;
    }

    public @ColorInt
    int getUserNickColor() {
        return userNickColor;
    }

    public void setMessageTextSize(int messageTextSize) {
        this.messageTextSize = messageTextSize;
    }

    public int getMessageTextSize() {
        return messageTextSize;
    }

    public @ColorInt
    int getMessageTextColor() {
        return messageTextColor;
    }

    public void setMessageTextColor(@ColorInt int messageTextColor) {
        this.messageTextColor = messageTextColor;
    }
}
