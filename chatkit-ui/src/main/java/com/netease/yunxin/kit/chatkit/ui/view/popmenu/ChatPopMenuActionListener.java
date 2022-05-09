/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.view.popmenu;

import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/**
 * message long click menu event listener
 */
public interface ChatPopMenuActionListener {
    void onCopy(ChatMessageBean messageInfo);

    void onReply(ChatMessageBean messageInfo);

    void onForward(ChatMessageBean messageInfo);

    void onSignal(ChatMessageBean messageInfo, boolean isCancel);

    void onMultiSelected(ChatMessageBean messageInfo);

    void onCollection(ChatMessageBean messageInfo);

    void onDelete(ChatMessageBean message);

    void onRecall(ChatMessageBean messageInfo);

}
