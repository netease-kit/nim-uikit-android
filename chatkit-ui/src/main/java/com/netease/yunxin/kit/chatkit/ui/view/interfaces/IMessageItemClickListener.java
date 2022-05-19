/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.view.interfaces;

import android.view.View;

import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/**
 * Message item click event listener
 */
public interface IMessageItemClickListener {
    boolean onMessageLongClick(View view, ChatMessageBean messageInfo);

    default void onMessageClick(View view, int position, ChatMessageBean messageInfo) {
    }

    void onUserIconClick(View view, ChatMessageBean messageInfo);

    void onSelfIconClick(View view);

    void onUserIconLongClick(View view, ChatMessageBean messageInfo);

    void onReEditRevokeMessage(View view, ChatMessageBean messageInfo);


    default void onReplyMessageClick(View view, String messageUuid) {
    }

    default void onSendFailBtnClick(View view, ChatMessageBean messageInfo) {
    }

    default void onTextSelected(View view, int position, ChatMessageBean messageInfo) {
    }
}
